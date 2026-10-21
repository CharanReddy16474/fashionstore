<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
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

    private String formatMoney(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();
        }

        return "0.00";
    }
%>

<%
    String contextPath = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> orders =
            (List<Map<String, Object>>)
            request.getAttribute("orders");

    String orderLoadError =
            (String) request.getAttribute("orderLoadError");

    if (orders == null) {
        orders = new java.util.ArrayList<>();
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>My Orders | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/my-orders.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="my-orders-page">

        <div class="my-orders-container">

            <div class="my-orders-heading">

                <div>
                    <p class="orders-eyebrow">YOUR ACCOUNT</p>

                    <h1>My Orders</h1>

                    <p>
                        View your purchases, order details,
                        and current order status.
                    </p>
                </div>

                <a class="orders-shopping-button"
                   href="<%= contextPath %>/products">
                    Continue Shopping
                </a>

            </div>

            <% if (orderLoadError != null) { %>

                <div class="orders-alert" role="alert">
                    <%= escapeHtml(orderLoadError) %>
                </div>

            <% } else if (orders.isEmpty()) { %>

                <section class="orders-empty">

                    <div class="orders-empty-icon"
                         aria-hidden="true">
                        &#128722;
                    </div>

                    <h2>No orders yet</h2>

                    <p>
                        You have not placed any orders.
                        Start shopping to see your purchases here.
                    </p>

                    <a class="orders-primary-button"
                       href="<%= contextPath %>/products">
                        Explore Products
                    </a>

                </section>

            <% } else { %>

                <div class="orders-list">

                    <% for (Map<String, Object> order : orders) {

                        String orderNumber =
                                String.valueOf(
                                    order.get("orderNumber"));

                        String orderStatus =
                                String.valueOf(
                                    order.get("orderStatus"));

                        String createdAt =
                                String.valueOf(
                                    order.get("createdAt"));

                        String shippingName =
                                String.valueOf(
                                    order.get("shippingName"));

                        String shippingAddress =
                                String.valueOf(
                                    order.get("shippingAddress"));

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> items =
                                (List<Map<String, Object>>)
                                order.get("items");
                    %>

                        <section class="order-card">

                            <div class="order-card-header">

                                <div class="order-main-info">

                                    <p class="order-label">
                                        ORDER NUMBER
                                    </p>

                                    <h2>
                                        <%= escapeHtml(orderNumber) %>
                                    </h2>

                                    <p class="order-date">
                                        Placed on
                                        <%= escapeHtml(createdAt) %>
                                    </p>

                                </div>

                                <div class="order-status">
                                    <%= escapeHtml(orderStatus) %>
                                </div>

                            </div>

                            <div class="order-items">

                                <% for (Map<String, Object> item : items) { %>

                                    <div class="order-item">

                                        <div class="order-item-details">

                                            <h3>
                                                <%= escapeHtml(
                                                    String.valueOf(
                                                        item.get("productName"))) %>
                                            </h3>

                                            <p>
                                                Size:
                                                <%= escapeHtml(
                                                    String.valueOf(
                                                        item.get("size"))) %>

                                                <span class="item-separator">
                                                    &bull;
                                                </span>

                                                Colour:
                                                <%= escapeHtml(
                                                    String.valueOf(
                                                        item.get("color"))) %>
                                            </p>

                                            <p>
                                                Quantity:
                                                <%= item.get("quantity") %>
                                            </p>

                                        </div>

                                        <div class="order-item-price">

                                            <p>
                                                ₹<%= formatMoney(
                                                    item.get("unitPrice")) %>
                                                each
                                            </p>

                                            <strong>
                                                ₹<%= formatMoney(
                                                    item.get("lineTotal")) %>
                                            </strong>

                                        </div>

                                    </div>

                                <% } %>

                            </div>

                            <div class="order-card-footer">

                                <div class="shipping-details">

                                    <h3>Delivery Address</h3>

                                    <p>
                                        <%= escapeHtml(shippingName) %>
                                    </p>

                                    <p class="shipping-address">
                                        <%= escapeHtml(shippingAddress) %>
                                    </p>

                                </div>

                                <div class="order-total">

                                    <span>Order Total</span>

                                    <strong>
                                        ₹<%= formatMoney(
                                            order.get("totalAmount")) %>
                                    </strong>

                                </div>

                            </div>

                        </section>

                    <% } %>

                </div>

            <% } %>

        </div>

    </main>

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>