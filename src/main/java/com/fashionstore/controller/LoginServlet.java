package com.fashionstore.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import org.mindrot.jbcrypt.BCrypt;

import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.implementation.UserDAOImpl;
import com.fashionstore.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if ("1".equals(request.getParameter("registered"))) {
            request.setAttribute(
                "successMessage",
                "Account created successfully. Please log in."
            );
        }

        request.getRequestDispatcher(
            "/WEB-INF/views/login.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {

            request.setAttribute(
                "errorMessage",
                "Please enter your email and password."
            );

            request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
            ).forward(request, response);

            return;
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        try {
            // findByEmail() returns Optional<User>.
            // orElse(null) returns the User if found, otherwise null.
            User user = userDAO.findByEmail(email).orElse(null);

            if (user == null
                    || user.getPasswordHash() == null
                    || user.getPasswordHash().isBlank()
                    || !BCrypt.checkpw(
                        password,
                        user.getPasswordHash())) {

                request.setAttribute(
                    "errorMessage",
                    "Invalid email or password."
                );

                request.getRequestDispatcher(
                    "/WEB-INF/views/login.jsp"
                ).forward(request, response);

                return;
            }

            // Create a session after successful authentication.
            HttpSession session = request.getSession(true);
            request.changeSessionId();

            // Do not store the password hash in the session.
            user.setPasswordHash(null);

            session.setAttribute("loggedInUser", user);

            response.sendRedirect(
                request.getContextPath() + "/home"
            );

        } catch (SQLException e) {
            log("Database error during login.", e);

            request.setAttribute(
                "errorMessage",
                "Unable to log in right now. Please try again."
            );

            request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
            ).forward(request, response);
        }
    }
}