<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="com.fashionstore.model.Product" %>
<%@ page import="com.fashionstore.model.ProductImage" %>
<%@ page import="com.fashionstore.model.ProductVariant" %>

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

    private int calculateDiscount(
            BigDecimal original,
            BigDecimal discounted) {

        if (original == null || discounted == null
                || original.compareTo(BigDecimal.ZERO) <= 0
                || discounted.compareTo(original) >= 0) {
            return 0;
        }

        return original.subtract(discounted)
                .multiply(new BigDecimal("100"))
                .divide(original, 0, RoundingMode.HALF_UP)
                .intValue();
    }
%>

<%
    String contextPath = request.getContextPath();

    Product product =
            (Product) request.getAttribute("product");

    List<ProductImage> productImages =
            (List<ProductImage>) request.getAttribute("productImages");

    List<ProductVariant> variants =
            (List<ProductVariant>) request.getAttribute("variants");

    String brandName =
            (String) request.getAttribute("brandName");

    if (productImages == null) {
        productImages = new ArrayList<>();
    }

    if (variants == null) {
        variants = new ArrayList<>();
    }

    if (brandName == null || brandName.isBlank()) {
        brandName = "Fashion Store";
    }

    BigDecimal originalPrice =
            product == null ? null : product.getPrice();

    BigDecimal sellingPrice =
            product == null ? null
                    : (product.getDiscountPrice() != null
                        ? product.getDiscountPrice()
                        : product.getPrice());

    int discountPercent = calculateDiscount(
            originalPrice,
            product == null ? null : product.getDiscountPrice()
    );

    boolean inStock = false;

    for (ProductVariant variant : variants) {
        if (variant != null && variant.getStockQuantity() > 0) {
            inStock = true;
            break;
        }
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Product Details | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/product-details.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="product-details-page">

        <!-- BREADCRUMB -->
        <nav class="product-breadcrumb" aria-label="Breadcrumb">

            <a href="<%= contextPath %>/home">Home</a>

            <span aria-hidden="true">/</span>

            <a href="<%= contextPath %>/products">Products</a>

            <span aria-hidden="true">/</span>

            <span class="breadcrumb-current">
                <%= product == null
                        ? "Product Details"
                        : escapeHtml(product.getProductName()) %>
            </span>

        </nav>

        <% if (product == null) { %>

            <!-- PRODUCT NOT FOUND -->
            <section class="product-not-found">

                <div class="product-not-found-icon"
                     aria-hidden="true">!</div>

                <h1>Product not found</h1>

                <p>
                    This product is unavailable or the product link is invalid.
                </p>

                <a class="product-primary-button"
                   href="<%= contextPath %>/products">
                    Browse Products
                </a>

            </section>

        <% } else { %>

            <!-- PRODUCT DETAILS -->
            <section class="product-details-layout">

                <!-- PRODUCT IMAGE GALLERY -->
                <div class="product-gallery"
                     aria-label="Product images">

                    <% if (productImages.isEmpty()) { %>

                        <div class="product-image-placeholder">
                            <span>F</span>
                            <p>FASHION STORE</p>
                        </div>

                    <% } else { %>

                        <% for (ProductImage image : productImages) { %>

                            <% if (image != null
                                    && image.getImageUrl() != null
                                    && !image.getImageUrl().isBlank()) { %>

                                <div class="product-gallery-image">

                                    <img
                                        src="<%= escapeHtml(image.getImageUrl()) %>"
                                        alt="<%= escapeHtml(product.getProductName()) %>"
                                        loading="lazy"
                                        onerror="this.parentElement.hidden=true;">

                                </div>

                            <% } %>

                        <% } %>

                    <% } %>

                </div>

                <!-- PRODUCT INFORMATION -->
                <div class="product-information">

                    <p class="product-detail-brand">
                        <%= escapeHtml(brandName) %>
                    </p>

                    <h1 class="product-detail-name">
                        <%= escapeHtml(product.getProductName()) %>
                    </h1>

                    <p class="product-detail-description">
                        <%= product.getDescription() == null
                                || product.getDescription().isBlank()
                                ? "Discover this fashion essential for your everyday wardrobe."
                                : escapeHtml(product.getDescription()) %>
                    </p>

                    <!-- PRICE -->
                    <div class="product-detail-price">

                        <% if (sellingPrice != null) { %>

                            <span class="detail-current-price">
                                ₹<%= sellingPrice.setScale(
                                        2, RoundingMode.HALF_UP
                                    ).toPlainString() %>
                            </span>

                        <% } %>

                        <% if (discountPercent > 0
                                && originalPrice != null) { %>

                            <span class="detail-original-price">
                                ₹<%= originalPrice.setScale(
                                        2, RoundingMode.HALF_UP
                                    ).toPlainString() %>
                            </span>

                            <span class="detail-discount-percent">
                                <%= discountPercent %>% OFF
                            </span>

                        <% } %>

                    </div>

                    <p class="tax-note">
                        Inclusive of applicable taxes
                    </p>

                    <!-- SIZE, COLOUR AND ADD TO CART -->
                    <div class="variant-section">

                        <h2>Select Size &amp; Colour</h2>

                        <% if (!variants.isEmpty()) { %>

                            <form
                                action="<%= contextPath %>/cart/add"
                                method="post"
                                class="add-to-cart-form">

                                <input
                                    type="hidden"
                                    name="productId"
                                    value="<%= product.getProductId() %>">

                                <label for="variantId">
                                    Available options
                                </label>

                                <select
                                    name="variantId"
                                    id="variantId"
                                    required>

                                    <option value="">
                                        Select size and colour
                                    </option>

                                    <% for (ProductVariant variant : variants) { %>

                                        <% if (variant != null) { %>

                                            <option
                                                value="<%= variant.getVariantId() %>"
                                                <%= variant.getStockQuantity() <= 0
                                                        ? "disabled" : "" %>>

                                                <%= escapeHtml(
                                                    (variant.getSize() == null
                                                        || variant.getSize().isBlank()
                                                            ? "One Size"
                                                            : variant.getSize())
                                                    + " / "
                                                    + (variant.getColor() == null
                                                        || variant.getColor().isBlank()
                                                            ? "Standard"
                                                            : variant.getColor())
                                                    + (variant.getStockQuantity() <= 0
                                                        ? " - Out of Stock"
                                                        : " - Stock: "
                                                            + variant.getStockQuantity())
                                                ) %>

                                            </option>

                                        <% } %>

                                    <% } %>

                                </select>

                                <label for="quantity">
                                    Quantity
                                </label>

                                <input
                                    type="number"
                                    id="quantity"
                                    name="quantity"
                                    value="1"
                                    min="1"
                                    max="99"
                                    required>

                                <button
                                    type="submit"
                                    class="product-primary-button"
                                    <%= inStock ? "" : "disabled" %>>

                                    Add to Cart

                                </button>

                            </form>

                        <% } else { %>

                            <p class="product-stock-message">
                                Size and colour options are currently unavailable.
                            </p>

                        <% } %>

                    </div>

                    <!-- STOCK STATUS -->
                    <div class="product-stock-information">

                        <% if (inStock) { %>

                            <span
                                class="stock-indicator stock-available"
                                aria-hidden="true"></span>

                            <span>Available for purchase</span>

                        <% } else { %>

                            <span
                                class="stock-indicator stock-unavailable"
                                aria-hidden="true"></span>

                            <span>Currently out of stock</span>

                        <% } %>

                    </div>

                </div>

            </section>

        <% } %>

    </main>

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>