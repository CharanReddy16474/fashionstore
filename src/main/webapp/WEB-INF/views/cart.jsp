<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.List,java.util.Map,java.util.ArrayList,java.math.BigDecimal,java.math.RoundingMode" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private String safeImageUrl(String value, String contextPath) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String url = value.trim();

        if (url.startsWith("https://") || url.startsWith("http://")) {
            return url;
        }

        if (url.startsWith("/")) {
            return contextPath + url;
        }

        if (!url.contains(":") && !url.startsWith("//")) {
            return contextPath + "/" + url;
        }

        return "";
    }

    private String formatPrice(Object value) {
        if (!(value instanceof BigDecimal)) {
            return "0.00";
        }

        return ((BigDecimal) value)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
%>

<%
    String contextPath = request.getContextPath();

    List<Map<String, Object>> cartItemDetails =
        (List<Map<String, Object>>) request.getAttribute("cartItemDetails");

    if (cartItemDetails == null) {
        cartItemDetails = new ArrayList<>();
    }

    BigDecimal cartSubtotal =
        request.getAttribute("cartSubtotal") instanceof BigDecimal
            ? (BigDecimal) request.getAttribute("cartSubtotal")
            : BigDecimal.ZERO;

    Object cartQuantityValue = request.getAttribute("cartQuantity");

    int cartQuantity = cartQuantityValue instanceof Number
        ? ((Number) cartQuantityValue).intValue()
        : 0;

    String successMessage =
        (String) request.getAttribute("successMessage");

    String errorMessage =
        (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>My Cart | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/cart.css">

    <style>
        .cart-checkout-button {
            display: block;
            width: 100%;
            box-sizing: border-box;
            margin-top: 18px;
            padding: 14px 18px;
            border: 1px solid #222;
            border-radius: 4px;
            background-color: #222;
            color: #fff;
            text-align: center;
            text-decoration: none;
            font-size: 14px;
            font-weight: 600;
            transition: background-color 0.2s ease;
        }

        .cart-checkout-button:hover {
            background-color: #444;
            color: #fff;
        }

        .cart-checkout-button:focus-visible {
            outline: 2px solid #222;
            outline-offset: 3px;
        }
    </style>
</head>

<body>

    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="cart-page">

        <div class="cart-container">

            <!-- Cart heading -->

            <div class="cart-heading">

                <div>
                    <h1>My Cart</h1>
                    <p>Review your selected products before checkout.</p>
                </div>

                <span class="cart-item-count">
                    <%= cartQuantity %>
                    <%= cartQuantity == 1 ? "item" : "items" %>
                </span>

            </div>

            <!-- Success message -->

            <% if (successMessage != null) { %>

                <div class="cart-alert cart-success" role="status">
                    <%= escapeHtml(successMessage) %>
                </div>

            <% } %>

            <!-- Error message -->

            <% if (errorMessage != null) { %>

                <div class="cart-alert cart-error" role="alert">
                    <%= escapeHtml(errorMessage) %>
                </div>

            <% } %>

            <!-- Empty cart -->

            <% if (cartItemDetails.isEmpty()) { %>

                <section class="empty-cart">

                    <div class="empty-cart-icon" aria-hidden="true">
                        ♧
                    </div>

                    <h2>Your cart is empty</h2>

                    <p>
                        You haven't added any products to your cart yet.
                    </p>

                    <a class="continue-shopping"
                       href="<%= contextPath %>/home">
                        Continue Shopping
                    </a>

                </section>

            <% } else { %>

                <div class="cart-layout">

                    <!-- Cart products -->

                    <section class="cart-items"
                             aria-label="Products in your cart">

                        <% for (Map<String, Object> item : cartItemDetails) {

                            String productName =
                                String.valueOf(item.get("productName"));

                            String size =
                                String.valueOf(item.get("size"));

                            String color =
                                String.valueOf(item.get("color"));

                            String imageUrl =
                                safeImageUrl(
                                    (String) item.get("imageUrl"),
                                    contextPath
                                );

                            long cartItemId =
                                ((Number) item.get("cartItemId")).longValue();

                            long productId =
                                ((Number) item.get("productId")).longValue();

                            int quantity =
                                ((Number) item.get("quantity")).intValue();

                            int stockQuantity =
                                ((Number) item.get("stockQuantity")).intValue();

                            BigDecimal unitPrice =
                                item.get("unitPrice") instanceof BigDecimal
                                    ? (BigDecimal) item.get("unitPrice")
                                    : BigDecimal.ZERO;

                            BigDecimal lineTotal =
                                item.get("lineTotal") instanceof BigDecimal
                                    ? (BigDecimal) item.get("lineTotal")
                                    : BigDecimal.ZERO;

                            boolean available =
                                Boolean.TRUE.equals(item.get("available"));
                        %>

                            <article class="cart-item">

                                <!-- Product image -->

                                <a class="cart-product-image"
                                   href="<%= contextPath %>/product-details?id=<%= productId %>">

                                    <% if (!imageUrl.isEmpty()) { %>

                                        <img
                                            src="<%= escapeHtml(imageUrl) %>"
                                            alt="<%= escapeHtml(productName) %>"
                                            loading="lazy">

                                    <% } else { %>

                                        <div class="cart-image-placeholder">
                                            No image available
                                        </div>

                                    <% } %>

                                </a>

                                <!-- Product information -->

                                <div class="cart-product-info">

                                    <h2>
                                        <a href="<%= contextPath %>/product-details?id=<%= productId %>">
                                            <%= escapeHtml(productName) %>
                                        </a>
                                    </h2>

                                    <p class="cart-product-price">
                                        ₹<%= formatPrice(unitPrice) %>
                                    </p>

                                    <p class="cart-product-variant">
                                        Size: <%= escapeHtml(size) %>
                                        <span class="variant-separator">|</span>
                                        Colour: <%= escapeHtml(color) %>
                                    </p>

                                    <!-- Availability -->

                                    <% if (!available) { %>

                                        <p class="cart-stock-warning">
                                            This item is unavailable or its
                                            quantity exceeds available stock.
                                            Update its quantity or remove it.
                                        </p>

                                    <% } else { %>

                                        <p class="cart-stock-available">
                                            Available
                                        </p>

                                    <% } %>

                                    <!-- Update and remove actions -->

                                    <div class="cart-item-actions">

                                        <form
                                            action="<%= contextPath %>/cart/update"
                                            method="post"
                                            class="cart-update-form">

                                            <input
                                                type="hidden"
                                                name="cartItemId"
                                                value="<%= cartItemId %>">

                                            <label for="quantity-<%= cartItemId %>">
                                                Quantity
                                            </label>

                                            <input
                                                type="number"
                                                id="quantity-<%= cartItemId %>"
                                                name="quantity"
                                                value="<%= quantity %>"
                                                min="1"
                                                required>

                                            <button
                                                type="submit"
                                                class="cart-update-button">
                                                Update
                                            </button>

                                        </form>

                                        <form
                                            action="<%= contextPath %>/cart/remove"
                                            method="post"
                                            class="cart-remove-form">

                                            <input
                                                type="hidden"
                                                name="cartItemId"
                                                value="<%= cartItemId %>">

                                            <button
                                                type="submit"
                                                class="cart-remove-button">
                                                Remove
                                            </button>

                                        </form>

                                    </div>

                                    <!-- Line total -->

                                    <p class="cart-line-total">
                                        Item total:
                                        <strong>
                                            ₹<%= formatPrice(lineTotal) %>
                                        </strong>
                                    </p>

                                    <% if (stockQuantity > 0) { %>

                                        <p class="cart-stock-note">
                                            Stock available: <%= stockQuantity %>
                                        </p>

                                    <% } %>

                                </div>

                            </article>

                        <% } %>

                        <!-- Clear entire cart -->

                        <form
                            action="<%= contextPath %>/cart/clear"
                            method="post"
                            class="clear-cart-form"
                            onsubmit="return confirm('Remove all items from your cart?');">

                            <button
                                type="submit"
                                class="clear-cart-button">
                                Clear Entire Cart
                            </button>

                        </form>

                    </section>

                    <!-- Order summary -->

                    <aside class="cart-summary">

                        <h2>Order Summary</h2>

                        <div class="summary-row">
                            <span>Total quantity</span>
                            <span><%= cartQuantity %></span>
                        </div>

                        <div class="summary-row">
                            <span>Subtotal</span>

                            <strong>
                                ₹<%= formatPrice(cartSubtotal) %>
                            </strong>
                        </div>

                        <p class="cart-summary-note">
                            Shipping charges and final order total will be
                            calculated during checkout.
                        </p>

                        <!-- NEW: Proceed to Checkout button -->

                        <a href="<%= contextPath %>/checkout"
                           class="cart-checkout-button">
                            Proceed to Checkout
                        </a>

                        <!-- Continue shopping -->

                        <a href="<%= contextPath %>/home"
                           class="continue-shopping summary-continue">
                            Continue Shopping
                        </a>

                    </aside>

                </div>

            <% } %>

        </div>

    </main>

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>