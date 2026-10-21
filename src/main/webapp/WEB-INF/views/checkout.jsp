
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>

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

    private String formatPrice(Object value) {
        if (!(value instanceof BigDecimal)) {
            return "0.00";
        }

        return ((BigDecimal) value)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
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
%>

<%
    String contextPath = request.getContextPath();

    List<Map<String, Object>> checkoutItems =
        (List<Map<String, Object>>) request.getAttribute("checkoutItems");

    if (checkoutItems == null) {
        checkoutItems = new ArrayList<>();
    }

    BigDecimal subtotal =
        request.getAttribute("subtotal") instanceof BigDecimal
            ? (BigDecimal) request.getAttribute("subtotal")
            : BigDecimal.ZERO;

    BigDecimal shippingFee =
        request.getAttribute("shippingFee") instanceof BigDecimal
            ? (BigDecimal) request.getAttribute("shippingFee")
            : BigDecimal.ZERO;

    BigDecimal totalAmount =
        request.getAttribute("totalAmount") instanceof BigDecimal
            ? (BigDecimal) request.getAttribute("totalAmount")
            : BigDecimal.ZERO;

    Object quantityValue = request.getAttribute("cartQuantity");

    int cartQuantity = quantityValue instanceof Number
        ? ((Number) quantityValue).intValue()
        : 0;

    boolean canCheckout =
        Boolean.TRUE.equals(request.getAttribute("canCheckout"));

    String customerName =
        (String) request.getAttribute("customerName");

    String customerPhone =
        (String) request.getAttribute("customerPhone");

    String customerAddress =
        (String) request.getAttribute("customerAddress");

    String checkoutError =
        (String) request.getAttribute("checkoutError");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Checkout | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/checkout.css">
</head>

<body>

    <!-- Dynamic include prevents duplicate JSP variables -->
    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="checkout-page">

        <div class="checkout-container">

            <div class="checkout-heading">
                <h1>Checkout</h1>
                <p>Confirm your delivery details and review your order.</p>
            </div>

            <% if (checkoutError != null) { %>
                <div class="checkout-alert checkout-error" role="alert">
                    <%= escapeHtml(checkoutError) %>
                </div>
            <% } %>

            <div class="checkout-layout">

                <!-- DELIVERY DETAILS AND PAYMENT -->
                <section class="checkout-main">

                    <div class="checkout-card">

                        <div class="checkout-section-heading">
                            <span class="checkout-step">1</span>
                            <div>
                                <h2>Delivery Details</h2>
                                <p>Where should we deliver your order?</p>
                            </div>
                        </div>

                        <form
                            id="checkoutForm"
                            action="<%= contextPath %>/checkout/place-order"
                            method="post">

                            <div class="checkout-form-group">
                                <label for="shippingName">
                                    Recipient's Full Name
                                </label>

                                <input
                                    type="text"
                                    id="shippingName"
                                    name="shippingName"
                                    value="<%= escapeHtml(customerName) %>"
                                    maxlength="100"
                                    autocomplete="name"
                                    placeholder="Enter recipient's full name"
                                    required>
                            </div>

                            <div class="checkout-form-group">
                                <label for="shippingPhone">
                                    Contact Phone Number
                                </label>

                                <input
                                    type="tel"
                                    id="shippingPhone"
                                    name="shippingPhone"
                                    value="<%= escapeHtml(customerPhone) %>"
                                    maxlength="20"
                                    autocomplete="tel"
                                    placeholder="Enter contact phone number"
                                    required>
                            </div>

                            <div class="checkout-form-group">
                                <label for="shippingAddress">
                                    Complete Delivery Address
                                </label>

                                <textarea
                                    id="shippingAddress"
                                    name="shippingAddress"
                                    rows="4"
                                    maxlength="500"
                                    autocomplete="street-address"
                                    placeholder="House number, street, area, city, state and PIN code"
                                    required><%= escapeHtml(customerAddress) %></textarea>
                            </div>

                            <!-- PAYMENT OPTIONS -->

                            <div class="checkout-payment-section">

                                <div class="checkout-section-heading">
                                    <span class="checkout-step">2</span>
                                    <div>
                                        <h2>Payment Method</h2>
                                        <p>Choose how you want to pay.</p>
                                    </div>
                                </div>

                                <div class="checkout-payment-options">

                                    <!-- Cash on Delivery -->

                                    <label class="payment-option">

                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="COD"
                                            checked
                                            required>

                                        <span class="payment-option-content">
                                            <strong>Cash on Delivery (COD)</strong>
                                            <span>
                                                Pay when your order is delivered.
                                            </span>
                                        </span>

                                    </label>

                                    <!-- UPI -->

                                    <label class="payment-option">

                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="UPI"
                                            required>

                                        <span class="payment-option-content">
                                            <strong>UPI</strong>
                                            <span>
                                                UPI payment option. A payment
                                                gateway is required to collect
                                                an actual payment.
                                            </span>
                                        </span>

                                    </label>

                                    <!-- Credit or Debit Card -->

                                    <label class="payment-option">

                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="CARD"
                                            required>

                                        <span class="payment-option-content">
                                            <strong>Credit / Debit Card</strong>
                                            <span>
                                                Card payment option. A payment
                                                gateway is required to collect
                                                an actual payment.
                                            </span>
                                        </span>

                                    </label>

                                </div>

                                <p class="payment-note">
                                    Demo notice: COD, UPI, and Card selections
                                    create an order with payment status PENDING.
                                    UPI and Card payments are not processed and
                                    no money is collected in this version.
                                </p>

                            </div>

                            <button
                                type="submit"
                                class="place-order-button"
                                <%= canCheckout ? "" : "disabled" %>>
                                Place Order
                            </button>

                            <% if (!canCheckout) { %>
                                <p class="checkout-disabled-note">
                                    Add available products to your cart and
                                    resolve any stock issues before placing
                                    an order.
                                </p>
                            <% } %>

                        </form>

                    </div>

                </section>

                <!-- ORDER SUMMARY -->

                <aside class="checkout-summary">

                    <div class="checkout-card">

                        <h2>Order Summary</h2>

                        <p class="checkout-item-count">
                            <%= cartQuantity %>
                            <%= cartQuantity == 1 ? "item" : "items" %>
                        </p>

                        <div class="checkout-product-list">

                            <% for (Map<String, Object> item : checkoutItems) {

                                String productName =
                                    String.valueOf(item.get("productName"));

                                String size =
                                    String.valueOf(item.get("size"));

                                String color =
                                    String.valueOf(item.get("color"));

                                String imageUrl = safeImageUrl(
                                    (String) item.get("imageUrl"),
                                    contextPath
                                );

                                int quantity =
                                    ((Number) item.get("quantity")).intValue();

                                BigDecimal lineTotal =
                                    item.get("lineTotal") instanceof BigDecimal
                                        ? (BigDecimal) item.get("lineTotal")
                                        : BigDecimal.ZERO;
                            %>

                                <article class="checkout-product">

                                    <div class="checkout-product-image">

                                        <% if (!imageUrl.isEmpty()) { %>

                                            <img
                                                src="<%= escapeHtml(imageUrl) %>"
                                                alt="<%= escapeHtml(productName) %>"
                                                loading="lazy">

                                        <% } else { %>

                                            <div class="checkout-image-placeholder">
                                                No image
                                            </div>

                                        <% } %>

                                    </div>

                                    <div class="checkout-product-details">

                                        <h3>
                                            <%= escapeHtml(productName) %>
                                        </h3>

                                        <p>
                                            Size: <%= escapeHtml(size) %>
                                        </p>

                                        <p>
                                            Colour: <%= escapeHtml(color) %>
                                        </p>

                                        <p>
                                            Quantity: <%= quantity %>
                                        </p>

                                        <strong>
                                            ₹<%= formatPrice(lineTotal) %>
                                        </strong>

                                    </div>

                                </article>

                            <% } %>

                        </div>

                        <div class="checkout-price-breakdown">

                            <div class="checkout-price-row">
                                <span>Subtotal</span>
                                <span>₹<%= formatPrice(subtotal) %></span>
                            </div>

                            <div class="checkout-price-row">
                                <span>Shipping</span>

                                <% if (shippingFee.compareTo(
                                        BigDecimal.ZERO) == 0) { %>

                                    <span class="free-shipping">FREE</span>

                                <% } else { %>

                                    <span>
                                        ₹<%= formatPrice(shippingFee) %>
                                    </span>

                                <% } %>
                            </div>

                            <div class="checkout-price-row checkout-total-row">
                                <strong>Total Amount</strong>

                                <strong>
                                    ₹<%= formatPrice(totalAmount) %>
                                </strong>
                            </div>

                        </div>

                        <a class="back-to-cart"
                           href="<%= contextPath %>/cart">
                            Return to Cart
                        </a>

                    </div>

                </aside>

            </div>

        </div>

    </main>

    <!-- Dynamic include avoids JSP variable conflicts -->
    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
