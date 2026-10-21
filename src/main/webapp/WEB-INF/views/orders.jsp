<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.List,java.util.Map,java.util.ArrayList,java.math.BigDecimal,java.math.RoundingMode,java.sql.Timestamp,java.text.SimpleDateFormat" %>

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

    private String formatDate(Object value) {
        if (!(value instanceof Timestamp)) {
            return "Date unavailable";
        }

        return new SimpleDateFormat("dd MMM yyyy, hh:mm a")
                .format((Timestamp) value);
    }

    private String statusClass(String status) {
        if (status == null) {
            return "status-default";
        }

        switch (status.toUpperCase()) {
            case "PLACED":
                return "status-placed";
            case "CONFIRMED":
                return "status-confirmed";
            case "SHIPPED":
                return "status-shipped";
            case "DELIVERED":
                return "status-delivered";
            case "CANCELLED":
                return "status-cancelled";
            case "RETURNED":
                return "status-returned";
            default:
                return "status-default";
        }
    }
%>

<%
    String contextPath = request.getContextPath();

    List<Map<String, Object>> orders =
        (List<Map<String, Object>>) request.getAttribute("orders");

    if (orders == null) {
        orders = new ArrayList<>();
    }

    String ordersError =
        (String) request.getAttribute("ordersError");
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
          href="<%= contextPath %>/assets/css/orders.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="orders-page">

        <div class="orders-container">

            <!-- Page heading -->

            <section class="orders-heading">
                <div>
                    <h1>My Orders</h1>
                    <p>
                        Track your purchases and view your order history.
                    </p>
                </div>

                <span class="orders-count">
                    <%= orders.size() %>
                    <%= orders.size() == 1 ? "order" : "orders" %>
                </span>
            </section>

            <!-- Database error -->

            <% if (ordersError != null) { %>

                <div class="orders-alert" role="alert">
                    <%= escapeHtml(ordersError) %>
                </div>

            <% } else if (orders.isEmpty()) { %>

                <!-- Empty order history -->

                <section class="orders-empty">

                    <div class="orders-empty-icon"
                         aria-hidden="true">
                        &#128722;
                    </div>

                    <h2>No orders yet</h2>

                    <p>
                        You haven't placed any orders yet.
                        Explore our collection and find something you love.
                    </p>

                    <a href="<%= contextPath %>/products"
                       class="orders-shop-button">
                        Start Shopping
                    </a>

                </section>

            <% } else { %>

                <!-- Order list -->

                <section class="orders-list"
                         aria-label="Your order history">

                    <% for (Map<String, Object> order : orders) {

                        String orderNumber =
                            String.valueOf(order.get("orderNumber"));

                        String orderStatus =
                            String.valueOf(order.get("orderStatus"));

                        Object createdAt =
                            order.get("createdAt");

                        Object itemCountValue =
                            order.get("itemCount");

                        int itemCount =
                            itemCountValue instanceof Number
                                ? ((Number) itemCountValue).intValue()
                                : 0;

                        Object totalAmount =
                            order.get("totalAmount");

                        String badgeClass =
                            statusClass(orderStatus);
                    %>

                        <article class="order-card">

                            <!-- Order header -->

                            <div class="order-card-header">

                                <div class="order-reference">
                                    <span class="order-label">
                                        Order number
                                    </span>

                                    <strong>
                                        <%= escapeHtml(orderNumber) %>
                                    </strong>
                                </div>

                                <span class="order-status <%= badgeClass %>">
                                    <%= escapeHtml(orderStatus) %>
                                </span>

                            </div>

                            <!-- Order information -->

                            <div class="order-card-details">

                                <div class="order-detail">
                                    <span class="order-label">
                                        Order date
                                    </span>

                                    <strong>
                                        <%= escapeHtml(formatDate(createdAt)) %>
                                    </strong>
                                </div>

                                <div class="order-detail">
                                    <span class="order-label">
                                        Items
                                    </span>

                                    <strong>
                                        <%= itemCount %>
                                        <%= itemCount == 1 ? "item" : "items" %>
                                    </strong>
                                </div>

                                <div class="order-detail">
                                    <span class="order-label">
                                        Total amount
                                    </span>

                                    <strong class="order-total">
                                        ₹<%= formatPrice(totalAmount) %>
                                    </strong>
                                </div>

                            </div>

                            <!-- Payment information -->

                            <div class="order-card-footer">
                                <span>
                                    Your order information is available above.
                                </span>
                            </div>

                        </article>

                    <% } %>

                </section>

            <% } %>

            <!-- Navigation -->

            <div class="orders-navigation">

                <a href="<%= contextPath %>/products"
                   class="orders-back-link">
                    Continue Shopping
                </a>

                <a href="<%= contextPath %>/home"
                   class="orders-home-link">
                    Back to Home
                </a>

            </div>

        </div>

    </main>

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>