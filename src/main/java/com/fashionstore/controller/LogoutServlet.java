package com.fashionstore.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Prevent the browser from reusing cached private pages.
        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate");

        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Get the current session without creating a new one.
        HttpSession session = request.getSession(false);

        // Invalidate the session if one exists.
        if (session != null) {
            session.invalidate();
        }

        // Redirect the customer to the homepage.
        response.sendRedirect(
                request.getContextPath() + "/home");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Logout is performed through POST, not GET.
        response.sendRedirect(
                request.getContextPath() + "/home");
    }
}