package com.fashionstore.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fashionstore.model.User;
import com.fashionstore.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-orders")
public class OrderHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String ORDERS_PAGE =
            "/WEB-INF/views/my-orders.jsp";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");

        // Check whether the customer is logged in.
        HttpSession session = request.getSession(false);

        User user = null;

        if (session != null) {
            Object sessionUser = session.getAttribute("loggedInUser");

            if (sessionUser instanceof User) {
                user = (User) sessionUser;
            }
        }

        // A customer must log in before viewing their orders.
        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        List<Map<String, Object>> orders = new ArrayList<>();

        String sql =
                "SELECT "
              + "o.order_id, "
              + "o.order_number, "
              + "o.order_status, "
              + "o.created_at, "
              + "o.shipping_name, "
              + "o.shipping_phone, "
              + "o.shipping_address, "
              + "o.subtotal, "
              + "o.discount_amount, "
              + "o.shipping_fee, "
              + "o.total_amount, "
              + "oi.order_item_id, "
              + "oi.product_name, "
              + "oi.size, "
              + "oi.color, "
              + "oi.unit_price, "
              + "oi.quantity, "
              + "oi.line_total "
              + "FROM orders o "
              + "LEFT JOIN order_items oi "
              + "ON o.order_id = oi.order_id "
              + "WHERE o.user_id = ? "
              + "ORDER BY o.created_at DESC, "
              + "o.order_id DESC, "
              + "oi.order_item_id ASC";

        Map<Long, Map<String, Object>> ordersById =
                new LinkedHashMap<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Only retrieve orders belonging to this customer.
            statement.setLong(1, user.getUserId());

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    long orderId = rs.getLong("order_id");

                    Map<String, Object> order =
                            ordersById.get(orderId);

                    // Create each order once.
                    if (order == null) {

                        order = new LinkedHashMap<>();

                        order.put("orderId", orderId);
                        order.put(
                                "orderNumber",
                                rs.getString("order_number"));
                        order.put(
                                "orderStatus",
                                rs.getString("order_status"));

                        Timestamp createdAt =
                                rs.getTimestamp("created_at");

                        String formattedDate = "";

                        if (createdAt != null) {
                            formattedDate =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy, hh:mm a",
                                            Locale.ENGLISH)
                                    .format(createdAt);
                        }

                        order.put("createdAt", formattedDate);

                        order.put(
                                "shippingName",
                                rs.getString("shipping_name"));
                        order.put(
                                "shippingPhone",
                                rs.getString("shipping_phone"));
                        order.put(
                                "shippingAddress",
                                rs.getString("shipping_address"));

                        order.put(
                                "subtotal",
                                rs.getBigDecimal("subtotal"));
                        order.put(
                                "discountAmount",
                                rs.getBigDecimal("discount_amount"));
                        order.put(
                                "shippingFee",
                                rs.getBigDecimal("shipping_fee"));
                        order.put(
                                "totalAmount",
                                rs.getBigDecimal("total_amount"));

                        order.put(
                                "items",
                                new ArrayList<Map<String, Object>>());

                        ordersById.put(orderId, order);
                    }

                    // Add the purchased products to this order.
                    long itemId = rs.getLong("order_item_id");

                    if (!rs.wasNull()) {

                        Map<String, Object> item =
                                new LinkedHashMap<>();

                        item.put(
                                "productName",
                                rs.getString("product_name"));
                        item.put("size", rs.getString("size"));
                        item.put("color", rs.getString("color"));
                        item.put(
                                "unitPrice",
                                rs.getBigDecimal("unit_price"));
                        item.put(
                                "quantity",
                                rs.getInt("quantity"));
                        item.put(
                                "lineTotal",
                                rs.getBigDecimal("line_total"));

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> items =
                                (List<Map<String, Object>>)
                                order.get("items");

                        items.add(item);
                    }
                }
            }

            orders.addAll(ordersById.values());

            request.setAttribute("orders", orders);

        } catch (SQLException e) {

            log("Unable to load customer order history.", e);

            request.setAttribute(
                    "orderLoadError",
                    "We could not load your orders right now. "
                  + "Please try again.");

            request.setAttribute("orders", orders);
        }

        // Open the JSP through the servlet.
        request.getRequestDispatcher(ORDERS_PAGE)
               .forward(request, response);
    }
}