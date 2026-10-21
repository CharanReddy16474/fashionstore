
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
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
%>

<%
    String contextPath = request.getContextPath();

    Object orderNumberAttribute =
            request.getAttribute("orderNumber");

    Object orderTotalAttribute =
            request.getAttribute("orderTotal");

    String orderNumber = orderNumberAttribute == null
            ? ""
            : orderNumberAttribute.toString();

    BigDecimal orderTotal =
            orderTotalAttribute instanceof BigDecimal
                ? (BigDecimal) orderTotalAttribute
                : BigDecimal.ZERO;

    orderTotal = orderTotal.setScale(2, RoundingMode.HALF_UP);

    String paymentMethod =
            (String) request.getAttribute("paymentMethod");

    if (paymentMethod == null) {
        paymentMethod = "COD";
    }

    String paymentLabel;
    String paymentStatusText;
    String paymentNote;

    switch (paymentMethod) {
        case "UPI":
            paymentLabel = "UPI";
            paymentStatusText = "Pending";
            paymentNote =
                "No UPI payment has been collected. A payment gateway "
              + "must be integrated before an actual UPI payment can occur.";
            break;

        case "CARD":
            paymentLabel = "Credit / Debit Card";
            paymentStatusText = "Pending";
            paymentNote =
                "No card payment has been collected. A payment gateway "
              + "must be integrated before an actual card payment can occur.";
            break;

        default:
            paymentLabel = "Cash on Delivery";
            paymentStatusText = "Pending — pay on delivery";
            paymentNote =
                "Please pay the order amount when your order is delivered.";
            break;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Order Confirmation | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/order-confirmation.css">
</head>

<body>

    <!-- Use dynamic include to prevent duplicate JSP variables -->
    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="order-confirmation-page">

        <section class="order-confirmation-card">

            <div class="order-success-icon" aria-hidden="true">
                &#10003;
            </div>

            <h1>Thank You for Your Order!</h1>

            <p class="order-confirmation-message">
                Your order has been recorded successfully.
                Thank you for shopping with Fashion Store.
            </p>

            <div class="order-confirmation-details">

                <div class="confirmation-detail-row">
                    <span>Order Number</span>
                    <strong>
                        <%= escapeHtml(orderNumber) %>
                    </strong>
                </div>

                <div class="confirmation-detail-row">
                    <span>Total Amount</span>
                    <strong class="confirmation-total">
                        ₹<%= orderTotal.toPlainString() %>
                    </strong>
                </div>

                <div class="confirmation-detail-row">
                    <span>Payment Method</span>
                    <strong>
                        <%= escapeHtml(paymentLabel) %>
                    </strong>
                </div>

                <div class="confirmation-detail-row">
                    <span>Payment Status</span>
                    <strong class="confirmation-payment-status">
                        <%= escapeHtml(paymentStatusText) %>
                    </strong>
                </div>

            </div>

            <p class="order-confirmation-note">
                <%= escapeHtml(paymentNote) %>
            </p>

            <div class="order-confirmation-actions">

                <a class="confirmation-primary-button"
                   href="<%= contextPath %>/products">
                    Continue Shopping
                </a>

                <a class="confirmation-secondary-button"
                   href="<%= contextPath %>/home">
                    Back to Home
                </a>

            </div>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
