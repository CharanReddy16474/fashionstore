package com.fashionstore.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fashionstore.model.User;
import com.fashionstore.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
    "/cart",
    "/cart/add",
    "/cart/update",
    "/cart/remove",
    "/cart/clear"
})
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CART_PAGE =
            "/WEB-INF/views/cart.jsp";

    // =====================================================
    // 1. VIEW CART
    // URL: GET /cart
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login"
            );
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<Map<String, Object>> cartItemDetails =
                new ArrayList<>();

        BigDecimal cartSubtotal = BigDecimal.ZERO;
        int cartQuantity = 0;
        Long cartId = null;

        try (Connection connection = DBConnection.getConnection()) {

            cartId = findCartId(connection, user.getUserId());

            if (cartId != null) {

                String sql =
                    "SELECT ci.cart_item_id, ci.variant_id, ci.quantity, "
                  + "pv.product_id, pv.sku, pv.size, pv.color, "
                  + "pv.stock_quantity, "
                  + "p.product_name, p.description, p.price, "
                  + "p.discount_price, p.is_active, "
                  + "(SELECT pi.image_url "
                  + " FROM product_images pi "
                  + " WHERE pi.product_id = p.product_id "
                  + " ORDER BY pi.is_primary DESC, "
                  + " pi.display_order ASC, pi.image_id ASC "
                  + " LIMIT 1) AS image_url "
                  + "FROM cart_items ci "
                  + "JOIN product_variants pv "
                  + " ON ci.variant_id = pv.variant_id "
                  + "JOIN products p "
                  + " ON pv.product_id = p.product_id "
                  + "WHERE ci.cart_id = ? "
                  + "ORDER BY ci.updated_at DESC";

                try (PreparedStatement statement =
                            connection.prepareStatement(sql)) {

                    statement.setLong(1, cartId);

                    try (ResultSet rs = statement.executeQuery()) {

                        while (rs.next()) {

                            long cartItemId =
                                    rs.getLong("cart_item_id");

                            long variantId =
                                    rs.getLong("variant_id");

                            long productId =
                                    rs.getLong("product_id");

                            int quantity =
                                    rs.getInt("quantity");

                            int stockQuantity =
                                    rs.getInt("stock_quantity");

                            BigDecimal price =
                                    rs.getBigDecimal("price");

                            BigDecimal discountPrice =
                                    rs.getBigDecimal("discount_price");

                            BigDecimal unitPrice =
                                    discountPrice != null
                                    ? discountPrice
                                    : price;

                            if (unitPrice == null) {
                                unitPrice = BigDecimal.ZERO;
                            }

                            BigDecimal lineTotal =
                                    unitPrice.multiply(
                                        BigDecimal.valueOf(quantity)
                                    );

                            boolean active =
                                    rs.getBoolean("is_active");

                            boolean available =
                                    active
                                    && stockQuantity > 0
                                    && quantity <= stockQuantity;

                            String imageUrl =
                                    rs.getString("image_url");

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put("cartItemId", cartItemId);
                            item.put("variantId", variantId);
                            item.put("productId", productId);
                            item.put(
                                "productName",
                                rs.getString("product_name")
                            );
                            item.put(
                                "description",
                                rs.getString("description")
                            );
                            item.put("sku", rs.getString("sku"));
                            item.put("size", rs.getString("size"));
                            item.put("color", rs.getString("color"));
                            item.put("quantity", quantity);
                            item.put("stockQuantity", stockQuantity);
                            item.put(
                                "imageUrl",
                                imageUrl == null ? "" : imageUrl
                            );
                            item.put("unitPrice", unitPrice);
                            item.put("lineTotal", lineTotal);
                            item.put("available", available);

                            cartItemDetails.add(item);

                            cartQuantity += quantity;

                            if (available) {
                                cartSubtotal =
                                    cartSubtotal.add(lineTotal);
                            }
                        }
                    }
                }
            }

            request.setAttribute("cartId", cartId);
            request.setAttribute(
                "cartItemDetails", cartItemDetails
            );
            request.setAttribute("cartSubtotal", cartSubtotal);
            request.setAttribute("cartQuantity", cartQuantity);

            showFlashMessage(request);

            request.getRequestDispatcher(CART_PAGE)
                   .forward(request, response);

        } catch (SQLException e) {

            log("Error while loading the shopping cart.", e);

            request.setAttribute(
                "errorMessage",
                "Unable to load your cart. Please try again."
            );

            request.setAttribute(
                "cartItemDetails",
                new ArrayList<Map<String, Object>>()
            );
            request.setAttribute(
                "cartSubtotal", BigDecimal.ZERO
            );
            request.setAttribute("cartQuantity", 0);
            request.setAttribute("cartId", null);

            request.getRequestDispatcher(CART_PAGE)
                   .forward(request, response);
        }
    }

    // =====================================================
    // 2. ADD, UPDATE, REMOVE OR CLEAR CART
    // All modifications use POST requests.
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login"
            );
            return;
        }

        String path = request.getServletPath();
        String result;

        try {
            switch (path) {

                case "/cart/add":
                    result = addToCart(request, user);
                    break;

                case "/cart/update":
                    result = updateCart(request, user);
                    break;

                case "/cart/remove":
                    result = removeCartItem(request, user);
                    break;

                case "/cart/clear":
                    result = clearCart(user);
                    break;

                default:
                    response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                    );
                    return;
            }

        } catch (SQLException e) {

            log("Database error while processing cart operation.", e);
            result = "database-error";
        }

        redirectToCart(request, response, result);
    }

    // =====================================================
    // 3. ADD TO CART
    // Required form fields: variantId, quantity
    // =====================================================

    private String addToCart(HttpServletRequest request, User user)
            throws SQLException {

        long variantId = parsePositiveLong(
            request.getParameter("variantId")
        );

        int quantity = parsePositiveInt(
            request.getParameter("quantity")
        );

        if (variantId <= 0) {
            return "invalid-variant";
        }

        if (quantity <= 0) {
            return "invalid-quantity";
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                long cartId = getOrCreateCartId(
                    connection, user.getUserId()
                );

                // Lock the variant row while checking availability.
                String stockSql =
                    "SELECT pv.stock_quantity, p.is_active "
                  + "FROM product_variants pv "
                  + "JOIN products p ON p.product_id = pv.product_id "
                  + "WHERE pv.variant_id = ? "
                  + "FOR UPDATE";

                int stockQuantity;
                boolean active;

                try (PreparedStatement statement =
                            connection.prepareStatement(stockSql)) {

                    statement.setLong(1, variantId);

                    try (ResultSet rs = statement.executeQuery()) {

                        if (!rs.next()) {
                            connection.rollback();
                            return "invalid-variant";
                        }

                        stockQuantity = rs.getInt("stock_quantity");
                        active = rs.getBoolean("is_active");
                    }
                }

                if (!active) {
                    connection.rollback();
                    return "product-unavailable";
                }

                if (stockQuantity <= 0) {
                    connection.rollback();
                    return "out-of-stock";
                }

                // Check whether this variant is already in the cart.
                int existingQuantity = 0;

                String findSql =
                    "SELECT quantity FROM cart_items "
                  + "WHERE cart_id = ? AND variant_id = ? "
                  + "FOR UPDATE";

                try (PreparedStatement statement =
                            connection.prepareStatement(findSql)) {

                    statement.setLong(1, cartId);
                    statement.setLong(2, variantId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            existingQuantity = rs.getInt("quantity");
                        }
                    }
                }

                long requestedTotal =
                        (long) existingQuantity + quantity;

                if (requestedTotal > stockQuantity) {
                    connection.rollback();
                    return "insufficient-stock";
                }

                if (existingQuantity > 0) {

                    String updateSql =
                        "UPDATE cart_items "
                      + "SET quantity = ?, updated_at = CURRENT_TIMESTAMP "
                      + "WHERE cart_id = ? AND variant_id = ?";

                    try (PreparedStatement statement =
                                connection.prepareStatement(updateSql)) {

                        statement.setInt(1, (int) requestedTotal);
                        statement.setLong(2, cartId);
                        statement.setLong(3, variantId);
                        statement.executeUpdate();
                    }

                } else {

                    String insertSql =
                        "INSERT INTO cart_items "
                      + "(cart_id, variant_id, quantity) "
                      + "VALUES (?, ?, ?)";

                    try (PreparedStatement statement =
                                connection.prepareStatement(insertSql)) {

                        statement.setLong(1, cartId);
                        statement.setLong(2, variantId);
                        statement.setInt(3, quantity);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return "added";

            } catch (SQLException e) {

                rollbackQuietly(connection);
                throw e;
            }
        }
    }

    // =====================================================
    // 4. UPDATE CART QUANTITY
    // Required form fields: cartItemId, quantity
    // =====================================================

    private String updateCart(HttpServletRequest request, User user)
            throws SQLException {

        long cartItemId = parsePositiveLong(
            request.getParameter("cartItemId")
        );

        int quantity = parsePositiveInt(
            request.getParameter("quantity")
        );

        if (cartItemId <= 0) {
            return "item-not-found";
        }

        if (quantity <= 0) {
            return "invalid-quantity";
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Long cartId = findCartId(
                    connection, user.getUserId()
                );

                if (cartId == null) {
                    connection.rollback();
                    return "item-not-found";
                }

                String checkSql =
                    "SELECT pv.stock_quantity, p.is_active "
                  + "FROM cart_items ci "
                  + "JOIN product_variants pv "
                  + " ON ci.variant_id = pv.variant_id "
                  + "JOIN products p "
                  + " ON p.product_id = pv.product_id "
                  + "WHERE ci.cart_item_id = ? "
                  + "AND ci.cart_id = ? "
                  + "FOR UPDATE";

                int stockQuantity;
                boolean active;

                try (PreparedStatement statement =
                            connection.prepareStatement(checkSql)) {

                    statement.setLong(1, cartItemId);
                    statement.setLong(2, cartId);

                    try (ResultSet rs = statement.executeQuery()) {

                        if (!rs.next()) {
                            connection.rollback();
                            return "item-not-found";
                        }

                        stockQuantity = rs.getInt("stock_quantity");
                        active = rs.getBoolean("is_active");
                    }
                }

                if (!active) {
                    connection.rollback();
                    return "product-unavailable";
                }

                if (quantity > stockQuantity) {
                    connection.rollback();
                    return "insufficient-stock";
                }

                String updateSql =
                    "UPDATE cart_items "
                  + "SET quantity = ?, updated_at = CURRENT_TIMESTAMP "
                  + "WHERE cart_item_id = ? AND cart_id = ?";

                try (PreparedStatement statement =
                            connection.prepareStatement(updateSql)) {

                    statement.setInt(1, quantity);
                    statement.setLong(2, cartItemId);
                    statement.setLong(3, cartId);
                    statement.executeUpdate();
                }

                connection.commit();
                return "updated";

            } catch (SQLException e) {

                rollbackQuietly(connection);
                throw e;
            }
        }
    }

    // =====================================================
    // 5. REMOVE ONE ITEM FROM CART
    // Required form field: cartItemId
    // =====================================================

    private String removeCartItem(HttpServletRequest request, User user)
            throws SQLException {

        long cartItemId = parsePositiveLong(
            request.getParameter("cartItemId")
        );

        if (cartItemId <= 0) {
            return "item-not-found";
        }

        try (Connection connection = DBConnection.getConnection()) {

            Long cartId = findCartId(
                connection, user.getUserId()
            );

            if (cartId == null) {
                return "item-not-found";
            }

            String sql =
                "DELETE FROM cart_items "
              + "WHERE cart_item_id = ? AND cart_id = ?";

            try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {

                statement.setLong(1, cartItemId);
                statement.setLong(2, cartId);

                int deleted = statement.executeUpdate();

                return deleted > 0
                        ? "removed"
                        : "item-not-found";
            }
        }
    }

    // =====================================================
    // 6. CLEAR ENTIRE CART
    // =====================================================

    private String clearCart(User user) throws SQLException {

        try (Connection connection = DBConnection.getConnection()) {

            Long cartId = findCartId(
                connection, user.getUserId()
            );

            if (cartId == null) {
                return "cleared";
            }

            String sql =
                "DELETE FROM cart_items WHERE cart_id = ?";

            try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {

                statement.setLong(1, cartId);
                statement.executeUpdate();
            }

            return "cleared";
        }
    }

    // =====================================================
    // 7. DATABASE HELPERS
    // =====================================================

    private Long findCartId(Connection connection, long userId)
            throws SQLException {

        String sql =
            "SELECT cart_id FROM carts WHERE user_id = ?";

        try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong("cart_id");
                }
            }
        }

        return null;
    }

    private long getOrCreateCartId(Connection connection, long userId)
            throws SQLException {

        String insertSql =
            "INSERT IGNORE INTO carts (user_id) VALUES (?)";

        try (PreparedStatement statement =
                    connection.prepareStatement(insertSql)) {

            statement.setLong(1, userId);
            statement.executeUpdate();
        }

        Long cartId = findCartId(connection, userId);

        if (cartId == null) {
            throw new SQLException(
                "Unable to find or create the customer's cart."
            );
        }

        return cartId;
    }

    private User getLoggedInUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userObject = session.getAttribute("loggedInUser");

        if (userObject instanceof User) {
            return (User) userObject;
        }

        return null;
    }

    private long parsePositiveLong(String value) {

        if (value == null || value.isBlank()) {
            return -1;
        }

        try {
            long number = Long.parseLong(value.trim());
            return number > 0 ? number : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parsePositiveInt(String value) {

        if (value == null || value.isBlank()) {
            return -1;
        }

        try {
            int number = Integer.parseInt(value.trim());
            return number > 0 ? number : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void rollbackQuietly(Connection connection) {

        try {
            connection.rollback();
        } catch (SQLException e) {
            log("Unable to roll back the cart transaction.", e);
        }
    }

    // =====================================================
    // 8. REDIRECT AFTER CART OPERATIONS
    // =====================================================

    private void redirectToCart(HttpServletRequest request,
                                HttpServletResponse response,
                                String result)
            throws IOException {

        boolean success =
                "added".equals(result)
                || "updated".equals(result)
                || "removed".equals(result)
                || "cleared".equals(result);

        String parameter = success ? "message" : "error";

        response.sendRedirect(
            request.getContextPath()
            + "/cart?"
            + parameter
            + "="
            + result
        );
    }

    private void showFlashMessage(HttpServletRequest request) {

        String message = request.getParameter("message");
        String error = request.getParameter("error");

        if ("added".equals(message)) {
            request.setAttribute(
                "successMessage",
                "Product added to your cart."
            );
        } else if ("updated".equals(message)) {
            request.setAttribute(
                "successMessage",
                "Cart quantity updated."
            );
        } else if ("removed".equals(message)) {
            request.setAttribute(
                "successMessage",
                "Product removed from your cart."
            );
        } else if ("cleared".equals(message)) {
            request.setAttribute(
                "successMessage",
                "Your cart has been cleared."
            );
        }

        if ("invalid-variant".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "Please select a valid product variant."
            );
        } else if ("invalid-quantity".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "Please enter a valid quantity."
            );
        } else if ("insufficient-stock".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "The requested quantity exceeds available stock."
            );
        } else if ("out-of-stock".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "This product is currently out of stock."
            );
        } else if ("product-unavailable".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "This product is currently unavailable."
            );
        } else if ("item-not-found".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "The selected cart item could not be found."
            );
        } else if ("database-error".equals(error)) {
            request.setAttribute(
                "errorMessage",
                "A database error occurred. Please try again."
            );
        }
    }
}