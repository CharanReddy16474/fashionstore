
package com.fashionstore.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.fashionstore.model.User;
import com.fashionstore.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
    "/checkout",
    "/checkout/place-order",
    "/checkout/success"
})
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CHECKOUT_PAGE =
            "/WEB-INF/views/checkout.jsp";

    private static final String CONFIRMATION_PAGE =
            "/WEB-INF/views/order-confirmation.jsp";

    // =====================================================
    // GET: DISPLAY CHECKOUT OR ORDER CONFIRMATION
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        if ("/checkout/success".equals(path)) {
            showOrderConfirmation(request, response);
            return;
        }

        setEmptyCheckoutAttributes(request);

        request.setAttribute("customerName", user.getFullName());
        request.setAttribute("customerPhone", user.getPhone());
        request.setAttribute("customerAddress", user.getAddress());

        try {
            loadCheckoutDetails(request, user.getUserId());

        } catch (SQLException exception) {
            log("Unable to load checkout details.", exception);

            request.setAttribute(
                    "checkoutError",
                    "Unable to load checkout details. Please try again.");

            request.setAttribute("canCheckout", false);
        }

        applyCheckoutMessage(request);

        request.getRequestDispatcher(CHECKOUT_PAGE)
                .forward(request, response);
    }

    // =====================================================
    // POST: PLACE ORDER
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!"/checkout/place-order".equals(
                request.getServletPath())) {

            response.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String shippingName = trim(
                request.getParameter("shippingName"));

        String shippingPhone = trim(
                request.getParameter("shippingPhone"));

        String shippingAddress = trim(
                request.getParameter("shippingAddress"));

        String paymentMethod = trim(
                request.getParameter("paymentMethod"));

        if (shippingName == null || shippingName.isBlank()
                || shippingName.length() > 100
                || shippingPhone == null || shippingPhone.isBlank()
                || shippingPhone.length() > 20
                || shippingAddress == null
                || shippingAddress.isBlank()
                || shippingAddress.length() > 500) {

            redirectWithError(
                    request, response, "invalid-details");
            return;
        }

        // These are the supported choices in this checkout demo.
        if (!"COD".equals(paymentMethod)
                && !"UPI".equals(paymentMethod)
                && !"CARD".equals(paymentMethod)) {

            redirectWithError(
                    request, response, "unsupported-payment");
            return;
        }

        try {
            PlaceOrderResult result = placeOrder(
                    user.getUserId(),
                    shippingName,
                    shippingPhone,
                    shippingAddress,
                    paymentMethod);

            if (!result.success) {
                redirectWithError(
                        request, response, result.errorCode);
                return;
            }

            HttpSession session = request.getSession();

            session.setAttribute(
                    "checkoutOrderNumber", result.orderNumber);

            session.setAttribute(
                    "checkoutOrderTotal", result.totalAmount);

            session.setAttribute(
                    "checkoutPaymentMethod", paymentMethod);

            response.sendRedirect(
                    request.getContextPath() + "/checkout/success");

        } catch (SQLException exception) {
            log("Database error while placing order.", exception);

            redirectWithError(
                    request, response, "database-error");
        }
    }

    // =====================================================
    // LOAD THE CUSTOMER'S CART FOR CHECKOUT
    // =====================================================

    private void loadCheckoutDetails(
            HttpServletRequest request,
            long userId)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection()) {

            Long cartId = findCartId(connection, userId);

            List<Map<String, Object>> items = new ArrayList<>();

            BigDecimal subtotal = BigDecimal.ZERO;
            int totalQuantity = 0;
            boolean allItemsAvailable = true;

            if (cartId != null) {

                String sql =
                    "SELECT ci.cart_item_id, ci.variant_id, ci.quantity, "
                  + "pv.product_id, pv.size, pv.color, pv.stock_quantity, "
                  + "p.product_name, p.is_active, p.price, p.discount_price, "
                  + "(SELECT pi.image_url FROM product_images pi "
                  + " WHERE pi.product_id = p.product_id "
                  + " ORDER BY pi.is_primary DESC, pi.display_order ASC, "
                  + " pi.image_id ASC LIMIT 1) AS image_url "
                  + "FROM cart_items ci "
                  + "JOIN product_variants pv "
                  + " ON pv.variant_id = ci.variant_id "
                  + "JOIN products p ON p.product_id = pv.product_id "
                  + "WHERE ci.cart_id = ? "
                  + "ORDER BY ci.cart_item_id";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setLong(1, cartId);

                    try (ResultSet rs = statement.executeQuery()) {

                        while (rs.next()) {

                            int quantity = rs.getInt("quantity");
                            int stock = rs.getInt("stock_quantity");

                            BigDecimal originalPrice =
                                    rs.getBigDecimal("price");

                            BigDecimal discountPrice =
                                    rs.getBigDecimal("discount_price");

                            BigDecimal unitPrice =
                                    discountPrice != null
                                            ? discountPrice
                                            : originalPrice;

                            if (unitPrice == null) {
                                unitPrice = BigDecimal.ZERO;
                            }

                            BigDecimal lineTotal =
                                    unitPrice.multiply(
                                            BigDecimal.valueOf(quantity));

                            boolean available =
                                    rs.getBoolean("is_active")
                                    && stock > 0
                                    && quantity <= stock;

                            if (!available) {
                                allItemsAvailable = false;
                            }

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put("cartItemId",
                                    rs.getLong("cart_item_id"));
                            item.put("variantId",
                                    rs.getLong("variant_id"));
                            item.put("productId",
                                    rs.getLong("product_id"));
                            item.put("productName",
                                    rs.getString("product_name"));
                            item.put("size", rs.getString("size"));
                            item.put("color", rs.getString("color"));
                            item.put("quantity", quantity);
                            item.put("stockQuantity", stock);
                            item.put("imageUrl",
                                    rs.getString("image_url"));
                            item.put("unitPrice", unitPrice);
                            item.put("lineTotal", lineTotal);
                            item.put("available", available);

                            items.add(item);

                            totalQuantity += quantity;
                            subtotal = subtotal.add(lineTotal);
                        }
                    }
                }
            }

            BigDecimal shippingFee = BigDecimal.ZERO;
            BigDecimal totalAmount = subtotal.add(shippingFee);

            request.setAttribute("cartId", cartId);
            request.setAttribute("checkoutItems", items);
            request.setAttribute("subtotal", subtotal);
            request.setAttribute("shippingFee", shippingFee);
            request.setAttribute("totalAmount", totalAmount);
            request.setAttribute("cartQuantity", totalQuantity);

            boolean canCheckout =
                    !items.isEmpty() && allItemsAvailable;

            request.setAttribute("canCheckout", canCheckout);

            if (items.isEmpty()) {
                request.setAttribute(
                        "checkoutError",
                        "Your cart is empty. Add products before checkout.");

            } else if (!allItemsAvailable) {
                request.setAttribute(
                        "checkoutError",
                        "Some products are unavailable or exceed available "
                      + "stock. Return to your cart and update quantities.");
            }
        }
    }

    // =====================================================
    // PLACE ORDER USING A DATABASE TRANSACTION
    // =====================================================

    private PlaceOrderResult placeOrder(
            long userId,
            String shippingName,
            String shippingPhone,
            String shippingAddress,
            String paymentMethod)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Long cartId = findCartId(connection, userId);

                if (cartId == null) {
                    connection.rollback();
                    return PlaceOrderResult.failure("empty-cart");
                }

                List<OrderLine> lines =
                        loadOrderLinesForUpdate(connection, cartId);

                if (lines.isEmpty()) {
                    connection.rollback();
                    return PlaceOrderResult.failure("empty-cart");
                }

                BigDecimal subtotal = BigDecimal.ZERO;

                // Check stock and activity immediately before ordering.
                for (OrderLine line : lines) {

                    if (!line.active) {
                        connection.rollback();

                        return PlaceOrderResult.failure(
                                "product-unavailable");
                    }

                    if (line.stockQuantity <= 0
                            || line.quantity > line.stockQuantity) {

                        connection.rollback();

                        return PlaceOrderResult.failure(
                                "insufficient-stock");
                    }

                    subtotal = subtotal.add(
                            line.unitPrice.multiply(
                                    BigDecimal.valueOf(line.quantity)));
                }

                BigDecimal discountAmount = BigDecimal.ZERO;
                BigDecimal shippingFee = BigDecimal.ZERO;

                BigDecimal totalAmount = subtotal
                        .subtract(discountAmount)
                        .add(shippingFee);

                String orderNumber =
                        "FS-" + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 20)
                                .toUpperCase(Locale.ROOT);

                long orderId;

                String orderSql =
                    "INSERT INTO orders "
                  + "(order_number, user_id, shipping_name, shipping_phone, "
                  + "shipping_address, subtotal, discount_amount, "
                  + "shipping_fee, total_amount, order_status) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PLACED')";

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     orderSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, orderNumber);
                    statement.setLong(2, userId);
                    statement.setString(3, shippingName);
                    statement.setString(4, shippingPhone);
                    statement.setString(5, shippingAddress);
                    statement.setBigDecimal(6, subtotal);
                    statement.setBigDecimal(7, discountAmount);
                    statement.setBigDecimal(8, shippingFee);
                    statement.setBigDecimal(9, totalAmount);

                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not create the order ID.");
                        }

                        orderId = keys.getLong(1);
                    }
                }

                String orderItemSql =
                    "INSERT INTO order_items "
                  + "(order_id, variant_id, product_name, size, color, "
                  + "unit_price, quantity, line_total) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                String stockSql =
                    "UPDATE product_variants "
                  + "SET stock_quantity = stock_quantity - ? "
                  + "WHERE variant_id = ? AND stock_quantity >= ?";

                try (PreparedStatement itemStatement =
                             connection.prepareStatement(orderItemSql);
                     PreparedStatement stockStatement =
                             connection.prepareStatement(stockSql)) {

                    for (OrderLine line : lines) {

                        BigDecimal lineTotal =
                                line.unitPrice.multiply(
                                        BigDecimal.valueOf(line.quantity));

                        itemStatement.setLong(1, orderId);
                        itemStatement.setLong(2, line.variantId);
                        itemStatement.setString(3, line.productName);
                        itemStatement.setString(4, line.size);
                        itemStatement.setString(5, line.color);
                        itemStatement.setBigDecimal(6, line.unitPrice);
                        itemStatement.setInt(7, line.quantity);
                        itemStatement.setBigDecimal(8, lineTotal);
                        itemStatement.executeUpdate();

                        stockStatement.setInt(1, line.quantity);
                        stockStatement.setLong(2, line.variantId);
                        stockStatement.setInt(3, line.quantity);

                        if (stockStatement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Stock changed while the order was "
                                  + "being processed.");
                        }
                    }
                }

                /*
                 * This demo records the selected method as PENDING.
                 * UPI and Card are NOT processed through a real gateway.
                 */
                String paymentSql =
                    "INSERT INTO payments "
                  + "(order_id, payment_method, amount, payment_status) "
                  + "VALUES (?, ?, ?, 'PENDING')";

                try (PreparedStatement statement =
                             connection.prepareStatement(paymentSql)) {

                    statement.setLong(1, orderId);
                    statement.setString(2, paymentMethod);
                    statement.setBigDecimal(3, totalAmount);
                    statement.executeUpdate();
                }

                // Clear this cart after the order has been created.
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM cart_items WHERE cart_id = ?")) {

                    statement.setLong(1, cartId);
                    statement.executeUpdate();
                }

                connection.commit();

                return PlaceOrderResult.success(
                        orderNumber, totalAmount);

            } catch (SQLException exception) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    exception.addSuppressed(rollbackError);
                }

                throw exception;
            }
        }
    }

    // =====================================================
    // LOAD AND LOCK THE CUSTOMER'S CART LINES
    // =====================================================

    private List<OrderLine> loadOrderLinesForUpdate(
            Connection connection,
            long cartId)
            throws SQLException {

        List<OrderLine> lines = new ArrayList<>();

        String sql =
            "SELECT ci.variant_id, ci.quantity, "
          + "pv.product_id, pv.size, pv.color, pv.stock_quantity, "
          + "p.product_name, p.is_active, "
          + "COALESCE(p.discount_price, p.price) AS unit_price "
          + "FROM cart_items ci "
          + "JOIN product_variants pv "
          + " ON pv.variant_id = ci.variant_id "
          + "JOIN products p ON p.product_id = pv.product_id "
          + "WHERE ci.cart_id = ? "
          + "ORDER BY ci.cart_item_id FOR UPDATE";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    OrderLine line = new OrderLine();

                    line.variantId = rs.getLong("variant_id");
                    line.productId = rs.getLong("product_id");
                    line.quantity = rs.getInt("quantity");
                    line.stockQuantity =
                            rs.getInt("stock_quantity");
                    line.productName =
                            rs.getString("product_name");
                    line.size = rs.getString("size");
                    line.color = rs.getString("color");
                    line.unitPrice =
                            rs.getBigDecimal("unit_price");
                    line.active = rs.getBoolean("is_active");

                    if (line.unitPrice == null) {
                        line.unitPrice = BigDecimal.ZERO;
                    }

                    lines.add(line);
                }
            }
        }

        return lines;
    }

    // =====================================================
    // FIND THE CUSTOMER'S CART
    // =====================================================

    private Long findCartId(
            Connection connection,
            long userId)
            throws SQLException {

        String sql = "SELECT cart_id FROM carts WHERE user_id = ?";

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

    // =====================================================
    // ORDER CONFIRMATION
    // =====================================================

    private void showOrderConfirmation(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("checkoutOrderNumber") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/home");
            return;
        }

        request.setAttribute(
                "orderNumber",
                session.getAttribute("checkoutOrderNumber"));

        request.setAttribute(
                "orderTotal",
                session.getAttribute("checkoutOrderTotal"));

        String paymentMethod =
                (String) session.getAttribute("checkoutPaymentMethod");

        request.setAttribute(
                "paymentMethod",
                paymentMethod == null ? "COD" : paymentMethod);

        request.setAttribute("paymentStatus", "PENDING");

        session.removeAttribute("checkoutOrderNumber");
        session.removeAttribute("checkoutOrderTotal");
        session.removeAttribute("checkoutPaymentMethod");

        request.getRequestDispatcher(CONFIRMATION_PAGE)
                .forward(request, response);
    }

    // =====================================================
    // SESSION AND REQUEST HELPERS
    // =====================================================

    private User getLoggedInUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("loggedInUser");

        return value instanceof User ? (User) value : null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void setEmptyCheckoutAttributes(
            HttpServletRequest request) {

        request.setAttribute(
                "checkoutItems",
                new ArrayList<Map<String, Object>>());

        request.setAttribute("subtotal", BigDecimal.ZERO);
        request.setAttribute("shippingFee", BigDecimal.ZERO);
        request.setAttribute("totalAmount", BigDecimal.ZERO);
        request.setAttribute("cartQuantity", 0);
        request.setAttribute("canCheckout", false);
    }

    private void applyCheckoutMessage(
            HttpServletRequest request) {

        String error = request.getParameter("error");

        if (error == null) {
            return;
        }

        switch (error) {

            case "invalid-details":
                request.setAttribute(
                        "checkoutError",
                        "Please enter a valid name, phone number, "
                      + "and shipping address.");
                break;

            case "unsupported-payment":
                request.setAttribute(
                        "checkoutError",
                        "Please select Cash on Delivery, UPI, or Card.");
                break;

            case "empty-cart":
                request.setAttribute(
                        "checkoutError",
                        "Your cart is empty. Add products before checkout.");
                break;

            case "insufficient-stock":
                request.setAttribute(
                        "checkoutError",
                        "A product's available stock changed. "
                      + "Please review your cart.");
                break;

            case "product-unavailable":
                request.setAttribute(
                        "checkoutError",
                        "A product in your cart is no longer available.");
                break;

            case "database-error":
                request.setAttribute(
                        "checkoutError",
                        "Unable to place your order right now. "
                      + "Please try again.");
                break;

            default:
                request.setAttribute(
                        "checkoutError",
                        "Unable to continue checkout. Please try again.");
        }
    }

    private void redirectWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/checkout?error="
                + error);
    }

    // =====================================================
    // INTERNAL DATA CLASSES
    // =====================================================

    private static class OrderLine {
        long variantId;
        long productId;
        String productName;
        String size;
        String color;
        int quantity;
        int stockQuantity;
        BigDecimal unitPrice;
        boolean active;
    }

    private static class PlaceOrderResult {

        boolean success;
        String errorCode;
        String orderNumber;
        BigDecimal totalAmount;

        private PlaceOrderResult(
                boolean success,
                String errorCode,
                String orderNumber,
                BigDecimal totalAmount) {

            this.success = success;
            this.errorCode = errorCode;
            this.orderNumber = orderNumber;
            this.totalAmount = totalAmount;
        }

        static PlaceOrderResult failure(String errorCode) {
            return new PlaceOrderResult(
                    false, errorCode, null, null);
        }

        static PlaceOrderResult success(
                String orderNumber,
                BigDecimal totalAmount) {

            return new PlaceOrderResult(
                    true, null, orderNumber, totalAmount);
        }
    }
}
