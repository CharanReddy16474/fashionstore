
package com.fashionstore.controller;

import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.implementation.UserDAOImpl;
import com.fashionstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAOImpl();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = getSessionUser(request, response);

        if (sessionUser == null) {
            return;
        }

        try {
            Optional<User> result =
                    userDAO.findById(sessionUser.getUserId());

            if (result.isEmpty()) {
                invalidateSession(request);
                response.sendRedirect(
                        request.getContextPath() + "/login");
                return;
            }

            User user = result.get();

            // Never keep the password hash in the session.
            user.setPasswordHash(null);

            request.getSession(false)
                    .setAttribute("loggedInUser", user);

            request.setAttribute("profileUser", user);

            if ("1".equals(request.getParameter("updated"))) {
                request.setAttribute(
                        "profileSuccess",
                        "Your profile has been updated successfully.");
            }

            request.getRequestDispatcher(
                    "/WEB-INF/views/profile.jsp")
                    .forward(request, response);

        } catch (SQLException exception) {
            log("Unable to load customer profile.", exception);

            request.setAttribute("profileUser", sessionUser);
            request.setAttribute(
                    "profileError",
                    "Unable to load your profile. Please try again.");

            request.getRequestDispatcher(
                    "/WEB-INF/views/profile.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User sessionUser = getSessionUser(request, response);

        if (sessionUser == null) {
            return;
        }

        String fullName = getParameter(request, "fullName");
        String email = getParameter(request, "email")
                .toLowerCase(Locale.ROOT);
        String phone = getParameter(request, "phone");
        String address = getParameter(request, "address");

        User formUser = new User();
        formUser.setUserId(sessionUser.getUserId());
        formUser.setFullName(fullName);
        formUser.setEmail(email);
        formUser.setPhone(phone.isBlank() ? null : phone);
        formUser.setAddress(address.isBlank() ? null : address);

        // Validate required fields and maximum lengths.
        if (fullName.isBlank() || email.isBlank()) {
            showError(request, response, formUser,
                    "Full name and email address are required.");
            return;
        }

        if (fullName.length() > 100
                || email.length() > 150
                || phone.length() > 20
                || address.length() > 500) {

            showError(request, response, formUser,
                    "One or more fields exceed the allowed length.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(request, response, formUser,
                    "Please enter a valid email address.");
            return;
        }

        try {
            // Retrieve the current database record.
            Optional<User> result =
                    userDAO.findById(sessionUser.getUserId());

            if (result.isEmpty()) {
                invalidateSession(request);
                response.sendRedirect(
                        request.getContextPath() + "/login");
                return;
            }

            User currentUser = result.get();

            boolean emailChanged =
                    currentUser.getEmail() == null
                    || !email.equalsIgnoreCase(currentUser.getEmail());

            // Prevent changing the email to another customer's email.
            if (emailChanged && userDAO.existsByEmail(email)) {
                showError(request, response, formUser,
                        "That email address is already registered.");
                return;
            }

            // Update name, phone, and address.
            // The user ID comes from the authenticated session,
            // never from a form field.
            boolean profileUpdated = userDAO.updateProfile(formUser);

            if (!profileUpdated) {
                showError(request, response, formUser,
                        "Your profile could not be updated. Please try again.");
                return;
            }

            // Email is updated separately by your existing DAO.
            if (emailChanged
                    && !userDAO.updateEmail(
                            currentUser.getUserId(), email)) {

                showError(request, response, formUser,
                        "Your email could not be updated. Please try again.");
                return;
            }

            // Reload the saved record to refresh the session.
            User updatedUser = userDAO
                    .findById(currentUser.getUserId())
                    .orElse(null);

            if (updatedUser == null) {
                showError(request, response, formUser,
                        "Your profile was saved, but could not be reloaded.");
                return;
            }

            // Do not store the password hash in the session.
            updatedUser.setPasswordHash(null);

            request.getSession(false)
                    .setAttribute("loggedInUser", updatedUser);

            // Redirect after POST to prevent accidental resubmission.
            response.sendRedirect(
                    request.getContextPath() + "/profile?updated=1");

        } catch (SQLException exception) {
            log("Unable to update customer profile.", exception);

            showError(request, response, formUser,
                    "Unable to update your profile. Please check your details and try again.");
        }
    }

    private User getSessionUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("loggedInUser")
                        instanceof User)) {

            response.sendRedirect(
                    request.getContextPath() + "/login");

            return null;
        }

        return (User) session.getAttribute("loggedInUser");
    }

    private String getParameter(
            HttpServletRequest request,
            String name) {

        String value = request.getParameter(name);

        return value == null ? "" : value.trim();
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            User formUser,
            String message)
            throws ServletException, IOException {

        request.setAttribute("profileUser", formUser);
        request.setAttribute("profileError", message);

        request.getRequestDispatcher(
                "/WEB-INF/views/profile.jsp")
                .forward(request, response);
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}
