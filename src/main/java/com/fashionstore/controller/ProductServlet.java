
package com.fashionstore.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fashionstore.dao.implementation.BrandDAOImpl;
import com.fashionstore.dao.implementation.CategoryDAOImpl;
import com.fashionstore.dao.implementation.ProductDAOImpl;
import com.fashionstore.dao.implementation.ProductImageDAOImpl;

import com.fashionstore.model.Brand;
import com.fashionstore.model.Category;
import com.fashionstore.model.Product;
import com.fashionstore.util.DBConnection;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
            BrandDAOImpl brandDAO = new BrandDAOImpl();
            ProductDAOImpl productDAO = new ProductDAOImpl();
            ProductImageDAOImpl imageDAO = new ProductImageDAOImpl();

            // Load active catalog data.
            List<Category> categories = categoryDAO.findAllActive();
            List<Brand> brands = brandDAO.findAllActive();

            List<Product> products =
                    new ArrayList<>(productDAO.findAllActive());

            // Read the selected department and search query.
            String selectedCategory = resolveCategorySlug(request);
            String searchQuery = resolveSearchQuery(request);

            boolean hasSearch =
                    searchQuery != null && !searchQuery.isBlank();

            /*
             * Apply filters only when a department or search term
             * has been provided.
             */
            if (selectedCategory != null || hasSearch) {

                List<Long> matchingIds = findProductIdsByFilters(
                        selectedCategory,
                        hasSearch ? searchQuery.trim() : null
                );

                Set<Long> matchingIdSet = new HashSet<>(matchingIds);

                products.removeIf(product ->
                        !matchingIdSet.contains(product.getProductId())
                );
            }

            // Create a brand ID -> brand name map for the JSP.
            Map<Integer, String> brandNameById = new HashMap<>();

            for (Brand brand : brands) {
                brandNameById.put(
                        brand.getBrandId(),
                        brand.getBrandName()
                );
            }

            /*
             * PERFORMANCE IMPROVEMENT:
             * Load images in bulk instead of querying the database
             * separately for every product.
             */
            List<Long> displayedProductIds =
                    new ArrayList<>(products.size());

            for (Product product : products) {
                displayedProductIds.add(product.getProductId());
            }

            Map<Long, String> productImageByProductId =
                    imageDAO.findFirstUsableImageUrlByProductIds(
                            displayedProductIds
                    );

            // Provide attributes used by the product JSP.
            request.setAttribute("categories", categories);
            request.setAttribute("brands", brands);
            request.setAttribute("products", products);
            request.setAttribute("brandNameById", brandNameById);

            request.setAttribute(
                    "productImageByProductId",
                    productImageByProductId
            );

            request.setAttribute(
                    "searchQuery",
                    hasSearch ? searchQuery.trim() : ""
            );

            request.setAttribute("searchApplied", hasSearch);
            request.setAttribute("searchResultCount", products.size());

            request.setAttribute("department", selectedCategory);
            request.setAttribute("selectedCategory", selectedCategory);

            request.getRequestDispatcher(
                    "/WEB-INF/views/products.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            log("Unable to load the Fashion Store product catalog.", e);

            throw new ServletException(
                    "Unable to load the Fashion Store product catalog.",
                    e
            );
        }
    }

    /**
     * Resolve department names from supported URL parameters.
     */
    private String resolveCategorySlug(HttpServletRequest request) {

        String rawCategory = request.getParameter("department");

        if (rawCategory == null || rawCategory.isBlank()) {
            rawCategory = request.getParameter("category");
        }

        if (rawCategory == null || rawCategory.isBlank()) {
            rawCategory = request.getParameter("categorySlug");
        }

        if (rawCategory == null || rawCategory.isBlank()) {
            return null;
        }

        String category = rawCategory
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');

        switch (category) {

            case "men":
            case "mens":
            case "men's":
            case "mens-fashion":
                return "men";

            case "women":
            case "womens":
            case "women's":
            case "womens-fashion":
                return "women";

            case "kid":
            case "kids":
            case "kids-fashion":
                return "kids";

            case "accessories":
            case "footwear":
            case "footwear-accessories":
                return "accessories";

            default:
                return null;
        }
    }

    /**
     * Read common search parameter names.
     */
    private String resolveSearchQuery(HttpServletRequest request) {

        String query = request.getParameter("q");

        if (query == null || query.isBlank()) {
            query = request.getParameter("search");
        }

        if (query == null || query.isBlank()) {
            query = request.getParameter("keyword");
        }

        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim();
    }

    /**
     * Return product IDs matching the department and search query.
     */
    private List<Long> findProductIdsByFilters(
            String categorySlug,
            String searchQuery) throws SQLException {

        List<Long> productIds = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(
                "SELECT DISTINCT p.product_id " +
                "FROM products p " +
                "JOIN categories c ON c.category_id = p.category_id " +
                "LEFT JOIN brands b ON b.brand_id = p.brand_id " +
                "WHERE p.is_active = TRUE " +
                "AND c.is_active = TRUE "
        );

        List<String> parameters = new ArrayList<>();

        // Department filter.
        if (categorySlug != null) {

            if ("accessories".equals(categorySlug)) {

                sql.append(
                        "AND c.category_slug IN (?, ?, ?) "
                );

                parameters.add("accessories");
                parameters.add("footwear-accessories");
                parameters.add("footwear");

            } else {

                String subcategoryPattern;

                switch (categorySlug) {

                    case "men":
                        subcategoryPattern = "mens-%";
                        break;

                    case "women":
                        subcategoryPattern = "womens-%";
                        break;

                    case "kids":
                        subcategoryPattern = "kids-%";
                        break;

                    default:
                        return productIds;
                }

                sql.append(
                        "AND (c.category_slug = ? " +
                        "OR c.category_slug LIKE ?) "
                );

                parameters.add(categorySlug);
                parameters.add(subcategoryPattern);
            }
        }

        // Search filter.
        if (searchQuery != null && !searchQuery.isBlank()) {

            sql.append(
                    "AND (" +
                    "LOWER(p.product_name) LIKE ? " +
                    "OR LOWER(COALESCE(p.description, '')) LIKE ? " +
                    "OR LOWER(COALESCE(b.brand_name, '')) LIKE ? " +
                    "OR LOWER(COALESCE(c.category_name, '')) LIKE ? " +
                    "OR LOWER(COALESCE(c.category_slug, '')) LIKE ? " +
                    "OR CAST(p.product_id AS CHAR) LIKE ? " +
                    "OR EXISTS (" +
                        "SELECT 1 FROM product_variants pv " +
                        "WHERE pv.product_id = p.product_id " +
                        "AND (" +
                            "LOWER(COALESCE(pv.size, '')) LIKE ? " +
                            "OR LOWER(COALESCE(pv.color, '')) LIKE ? " +
                            "OR LOWER(COALESCE(pv.sku, '')) LIKE ?" +
                        ")" +
                    ")" +
                    ") "
            );

            String searchPattern =
                    "%" + searchQuery.toLowerCase(Locale.ROOT) + "%";

            for (int i = 0; i < 9; i++) {
                parameters.add(searchPattern);
            }
        }

        sql.append("ORDER BY p.product_id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setString(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    productIds.add(resultSet.getLong("product_id"));
                }
            }
        }

        return productIds;
    }
}
