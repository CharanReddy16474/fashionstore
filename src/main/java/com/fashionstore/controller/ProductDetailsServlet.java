
package com.fashionstore.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fashionstore.dao.implementation.BrandDAOImpl;
import com.fashionstore.dao.implementation.ProductDAOImpl;
import com.fashionstore.dao.implementation.ProductImageDAOImpl;
import com.fashionstore.dao.implementation.ProductReviewDAOImpl;
import com.fashionstore.dao.implementation.ProductVariantDAOImpl;

import com.fashionstore.model.Brand;
import com.fashionstore.model.Product;
import com.fashionstore.model.ProductImage;
import com.fashionstore.model.ProductReview;
import com.fashionstore.model.ProductVariant;

@WebServlet("/product-details")
public class ProductDetailsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Read the product ID from the URL.
        String idParameter = request.getParameter("id");

        long productId;

        try {
            if (idParameter == null || idParameter.isBlank()) {
                throw new NumberFormatException("Missing product ID");
            }

            productId = Long.parseLong(idParameter);

            if (productId <= 0) {
                throw new NumberFormatException("Invalid product ID");
            }

        } catch (NumberFormatException e) {
            showNotFound(request, response);
            return;
        }

        try {
            // Create DAO objects.
            ProductDAOImpl productDAO = new ProductDAOImpl();
            BrandDAOImpl brandDAO = new BrandDAOImpl();
            ProductImageDAOImpl imageDAO = new ProductImageDAOImpl();
            ProductVariantDAOImpl variantDAO =
                    new ProductVariantDAOImpl();
            ProductReviewDAOImpl reviewDAO =
                    new ProductReviewDAOImpl();

            // 1. Find the active product.
            Product product = productDAO
                    .findActiveById(productId)
                    .orElse(null);

            if (product == null) {
                showNotFound(request, response);
                return;
            }

            // 2. Find the product brand.
            String brandName = "Fashion Store";

            if (product.getBrandId() != null) {
                Brand brand = brandDAO
                        .findById(product.getBrandId())
                        .orElse(null);

                if (brand != null && brand.getBrandName() != null) {
                    brandName = brand.getBrandName();
                }
            }

            // 3. Load product images.
            List<ProductImage> productImages =
                    imageDAO.findByProductId(productId);

            if (productImages == null) {
                productImages = new ArrayList<>();
            }

            // 4. Load available size and colour variants.
            List<ProductVariant> variants =
                    variantDAO.findByProductId(productId);

            if (variants == null) {
                variants = new ArrayList<>();
            }

            // 5. Load the latest page of product reviews.
            List<ProductReview> reviews =
                    reviewDAO.findByProductId(productId, 0, 10);

            if (reviews == null) {
                reviews = new ArrayList<>();
            }

            // 6. Load the rating summary.
            double averageRating =
                    reviewDAO.getAverageRating(productId);

            long reviewCount =
                    reviewDAO.countByProductId(productId);

            // 7. Pass the data to the JSP.
            request.setAttribute("product", product);
            request.setAttribute("brandName", brandName);
            request.setAttribute("productImages", productImages);
            request.setAttribute("variants", variants);
            request.setAttribute("reviews", reviews);
            request.setAttribute("averageRating", averageRating);
            request.setAttribute("reviewCount", reviewCount);

            // 8. Display the product details page.
            request.getRequestDispatcher(
                    "/WEB-INF/views/product-details.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load product details from the database.",
                    e
            );
        }
    }

    private void showNotFound(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        request.setAttribute("product", null);
        request.setAttribute("brandName", "Fashion Store");
        request.setAttribute("productImages", new ArrayList<>());
        request.setAttribute("variants", new ArrayList<>());
        request.setAttribute("reviews", new ArrayList<>());
        request.setAttribute("averageRating", 0.0);
        request.setAttribute("reviewCount", 0L);

        request.getRequestDispatcher(
                "/WEB-INF/views/product-details.jsp"
        ).forward(request, response);
    }
}
