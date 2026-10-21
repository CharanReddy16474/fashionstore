
package com.fashionstore.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.implementation.UserDAOImpl;
import com.fashionstore.model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/register.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = getValue(request, "fullName");
        String email = getValue(request, "email")
                .toLowerCase(Locale.ROOT);
        String phone = getValue(request, "phone");
        String address = getValue(request, "address");

        // Passwords are not trimmed because spaces may be intentional.
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        // Validate required fields.
        if (fullName.isBlank()
                || email.isBlank()
                || password == null
                || password.isBlank()
                || confirmPassword == null
                || confirmPassword.isBlank()) {

            showError(
                    request,
                    response,
                    "Please complete all required fields."
            );
            return;
        }

        // Validate field lengths.
        if (fullName.length() > 100
                || email.length() > 150
                || phone.length() > 20
                || address.length() > 500) {

            showError(
                    request,
                    response,
                    "One or more fields are too long."
            );
            return;
        }

        // Validate the email format.
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(
                    request,
                    response,
                    "Please enter a valid email address."
            );
            return;
        }

        // Require a minimum password length.
        if (password.length() < 8) {
            showError(
                    request,
                    response,
                    "Password must contain at least 8 characters."
            );
            return;
        }

        // BCrypt 0.4 supports passwords up to 72 UTF-8 bytes.
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            showError(
                    request,
                    response,
                    "Please use a password of at most 72 UTF-8 bytes."
            );
            return;
        }

        // Verify the password confirmation.
        if (!password.equals(confirmPassword)) {
            showError(
                    request,
                    response,
                    "Passwords do not match."
            );
            return;
        }

        try {
            UserDAO userDAO = new UserDAOImpl();

            // Prevent registering the same email again.
            if (userDAO.existsByEmail(email)) {
                showError(
                        request,
                        response,
                        "This email is already registered. Please log in."
                );
                return;
            }

            // Hash the password before storing it.
            String passwordHash = BCrypt.hashpw(
                    password,
                    BCrypt.gensalt(12)
            );

            // Create the user model.
            User user = new User();

            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone.isBlank() ? null : phone);
            user.setAddress(address.isBlank() ? null : address);
            user.setPasswordHash(passwordHash);

            // Save the user through the existing DAO.
            long userId = userDAO.createUser(user);

            if (userId <= 0) {
                showError(
                        request,
                        response,
                        "Registration failed. Please try again."
                );
                return;
            }

            // Registration succeeded. Redirect to login.
            response.sendRedirect(
                    request.getContextPath()
                            + "/login?registered=1"
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to register the user in the database.",
                    e
            );
        }
    }

    private String getValue(
            HttpServletRequest request,
            String parameterName) {

        String value = request.getParameter(parameterName);

        return value == null ? "" : value.trim();
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", message);

        request.getRequestDispatcher(
                "/WEB-INF/views/register.jsp"
        ).forward(request, response);
    }
}
