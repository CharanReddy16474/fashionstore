
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="com.fashionstore.model.Category" %>
<%@ page import="com.fashionstore.model.Brand" %>
<%@ page import="com.fashionstore.model.Product" %>

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

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>All Products | Fashion Store</title>

    <!-- Shared and catalog styles -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/home.css">
</head>

<body>

    <!-- Shared Header -->
    <%@ include file="partials/header.jsp" %>

    <%
        List<Category> categories =
                (List<Category>) request.getAttribute("categories");

        List<Brand> brands =
                (List<Brand>) request.getAttribute("brands");

        List<Product> products =
                (List<Product>) request.getAttribute("products");

        Map<Long, String> productImages =
                (Map<Long, String>) request.getAttribute(
                        "productImageByProductId");

        Map<Integer, String> brandNames =
                (Map<Integer, String>) request.getAttribute(
                        "brandNameById");

        if (categories == null) categories = new ArrayList<>();
        if (brands == null) brands = new ArrayList<>();
        if (products == null) products = new ArrayList<>();
        if (productImages == null) productImages = new HashMap<>();
        if (brandNames == null) brandNames = new HashMap<>();

        Map<Integer, Category> categoryById = new HashMap<>();

        for (Category category : categories) {
            categoryById.put(category.getCategoryId(), category);
        }
    %>

    <main class="catalog-layout">

        <section class="catalog-main">

            <!-- Breadcrumb -->
            <div class="breadcrumb">
                <a href="<%= request.getContextPath() %>/home">Home</a>
                <span aria-hidden="true">/</span>
                <span class="breadcrumb-current">All Products</span>
            </div>

            <!-- Page heading -->
            <div class="catalog-title-row">
                <h1>All Products</h1>

                <span class="catalog-item-count">
                    (<span id="catalogItemCount"><%= products.size() %></span> items)
                </span>
            </div>

            <p class="catalog-subtitle">
                Explore our collection of fashion, clothing and accessories.
            </p>

            <!-- Product sorting -->
            <div class="catalog-toolbar">

                <p class="results-count" id="resultsCount">
                    Showing <strong><%= products.size() %></strong> products
                </p>

                <div class="sort-control">
                    <label for="sortProducts">Sort by:</label>

                    <select id="sortProducts">
                        <option value="featured">Recommended</option>
                        <option value="price-low">Price: Low to High</option>
                        <option value="price-high">Price: High to Low</option>
                        <option value="name">Name: A to Z</option>
                    </select>
                </div>

            </div>

            <!-- Product cards -->
            <div class="product-grid" id="productGrid">

                <%
                    int productIndex = 0;

                    for (Product product : products) {

                        Category productCategory =
                                categoryById.get(product.getCategoryId());

                        String categoryName = productCategory == null
                                ? ""
                                : productCategory.getCategoryName();

                        String parentName = "";

                        if (productCategory != null
                                && productCategory.getParentCategoryId() != null) {

                            Category parentCategory = categoryById.get(
                                    productCategory.getParentCategoryId());

                            if (parentCategory != null) {
                                parentName = parentCategory.getCategoryName();
                            }
                        }

                        Integer brandId = product.getBrandId();

                        String brandName = brandId == null
                                ? "Fashion Store"
                                : brandNames.get(brandId);

                        if (brandName == null || brandName.isBlank()) {
                            brandName = "Fashion Store";
                        }

                        String imageUrl =
                                productImages.get(product.getProductId());

                        boolean hasImage =
                                imageUrl != null && !imageUrl.isBlank();

                        BigDecimal originalPrice = product.getPrice();

                        BigDecimal sellingPrice =
                                product.getDiscountPrice() != null
                                ? product.getDiscountPrice()
                                : originalPrice;

                        int discount = calculateDiscount(
                                originalPrice,
                                product.getDiscountPrice());

                        String priceValue = sellingPrice == null
                                ? "0"
                                : sellingPrice.toPlainString();

                        String searchableText =
                                product.getProductName() + " "
                                + brandName + " "
                                + categoryName + " "
                                + parentName;
                %>

                    <article class="product-card"
                             data-order="<%= productIndex %>"
                             data-product-id="<%= product.getProductId() %>"
                             data-price="<%= priceValue %>"
                             data-search="<%= escapeHtml(searchableText.toLowerCase(Locale.ROOT)) %>"
                             data-department="<%= escapeHtml((categoryName + " " + parentName).toLowerCase(Locale.ROOT)) %>">

                        <a class="product-image-link"
                           href="<%= request.getContextPath() %>/product-details?id=<%= product.getProductId() %>">

                            <div class="product-media">

                                <% if (hasImage) { %>

                                    <img class="product-image"
                                         src="<%= escapeHtml(imageUrl) %>"
                                         alt="<%= escapeHtml(product.getProductName()) %>"
                                         loading="lazy"
                                         onerror="this.hidden=true;this.nextElementSibling.hidden=false;">

                                    <div class="product-placeholder" hidden>
                                        <span class="product-placeholder-symbol">F</span>
                                        <span class="product-placeholder-label">FASHION STORE</span>
                                    </div>

                                <% } else { %>

                                    <div class="product-placeholder">
                                        <span class="product-placeholder-symbol">F</span>
                                        <span class="product-placeholder-label">FASHION STORE</span>
                                    </div>

                                <% } %>

                                <% if (discount > 0) { %>

                                    <span class="product-badge">
                                        <%= discount %>% OFF
                                    </span>

                                <% } %>

                            </div>

                        </a>

                        <div class="product-info">

                            <div class="product-brand">
                                <%= escapeHtml(brandName) %>
                            </div>

                            <div class="product-name"
                                 title="<%= escapeHtml(product.getProductName()) %>">
                                <%= escapeHtml(product.getProductName()) %>
                            </div>

                            <div class="product-price">

                                <% if (sellingPrice != null) { %>

                                    <span class="current-price">
                                        ₹<%= sellingPrice.toPlainString() %>
                                    </span>

                                <% } %>

                                <% if (discount > 0 && originalPrice != null) { %>

                                    <span class="original-price">
                                        ₹<%= originalPrice.toPlainString() %>
                                    </span>

                                    <span class="discount-percent">
                                        (<%= discount %>% OFF)
                                    </span>

                                <% } %>

                            </div>

                        </div>

                    </article>

                <%
                        productIndex++;
                    }
                %>

                <!-- Empty or no-search-results message -->
                <div class="no-results"
                     id="noResults"
                     <%= products.isEmpty() ? "" : "hidden" %>>

                    <h2>No products found</h2>

                    <p>
                        No active products are available, or no products
                        match your search.
                    </p>

                    <button type="button" id="resetSearch">
                        Clear search
                    </button>

                </div>

            </div>

        </section>

    </main>

    <!-- Shared Footer -->
    <%@ include file="partials/footer.jsp" %>

    <!-- Search and sorting -->
    <script src="<%= request.getContextPath() %>/assets/js/home.js" defer></script>

</body>
</html>
