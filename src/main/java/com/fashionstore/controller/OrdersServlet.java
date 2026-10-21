package com.fashionstore.controller;

import com.fashionstore.model.User;
import com.fashionstore.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Step 1: Check whether the customer is logged in.

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("loggedInUser") instanceof User)) {

            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");
        long userId = user.getUserId();

        // Step 2: Create a list to hold the customer's orders.

        List<Map<String, Object>> orders = new ArrayList<>();

        String sql = """
            SELECT
                o.order_id,
                o.order_number,
                o.order_status,
                o.subtotal,
                o.discount_amount,
                o.shipping_fee,
                o.total_amount,
                o.created_at,
                (
                    SELECT COUNT(*)
                    FROM order_items oi
                    WHERE oi.order_id = o.order_id
                ) AS item_count
            FROM orders o
            WHERE o.user_id = ?
            ORDER BY o.created_at DESC, o.order_id DESC
            """;

        // Step 3: Retrieve only the logged-in customer's orders.

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Map<String, Object> order = new HashMap<>();

                    order.put(
                        "orderId",
                        resultSet.getLong("order_id")
                    );

                    order.put(
                        "orderNumber",
                        resultSet.getString("order_number")
                    );

                    order.put(
                        "orderStatus",
                        resultSet.getString("order_status")
                    );

                    order.put(
                        "subtotal",
                        resultSet.getBigDecimal("subtotal")
                    );

                    order.put(
                        "discountAmount",
                        resultSet.getBigDecimal("discount_amount")
                    );

                    order.put(
                        "shippingFee",
                        resultSet.getBigDecimal("shipping_fee")
                    );

                    order.put(
                        "totalAmount",
                        resultSet.getBigDecimal("total_amount")
                    );

                    order.put(
                        "createdAt",
                        resultSet.getTimestamp("created_at")
                    );

                    order.put(
                        "itemCount",
                        resultSet.getInt("item_count")
                    );

                    orders.add(order);
                }
            }

            request.setAttribute("orders", orders);

        } catch (SQLException exception) {

            getServletContext().log(
                "Unable to load order history for user ID " + userId,
                exception
            );

            request.setAttribute(
                "orders",
                new ArrayList<Map<String, Object>>()
            );

            request.setAttribute(
                "ordersError",
                "Unable to load your orders right now. Please try again."
            );
        }

        // Step 4: Open the My Orders page.

        request.getRequestDispatcher(
            "/WEB-INF/views/orders.jsp"
        ).forward(request, response);
    }
}