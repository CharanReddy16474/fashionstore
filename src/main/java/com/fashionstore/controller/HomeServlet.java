
package com.fashionstore.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.fashionstore.model.ProductImage;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
            BrandDAOImpl brandDAO = new BrandDAOImpl();
            ProductDAOImpl productDAO = new ProductDAOImpl();
            ProductImageDAOImpl imageDAO = new ProductImageDAOImpl();

            // Load active catalog records.
            List<Category> categories = categoryDAO.findAllActive();
            List<Brand> brands = brandDAO.findAllActive();
            List<Product> products = productDAO.findAllActive();

            // Create a lookup map for brand names.
            Map<Integer, String> brandNameById = new HashMap<>();

            for (Brand brand : brands) {
                brandNameById.put(
                        brand.getBrandId(),
                        brand.getBrandName()
                );
            }

            // Find a primary image for each product.
            Map<Long, String> productImageByProductId =
                    new HashMap<>();

            for (Product product : products) {

                long productId = product.getProductId();

                ProductImage primaryImage = imageDAO
                        .findPrimaryByProductId(productId)
                        .orElse(null);

                if (primaryImage != null
                        && primaryImage.getImageUrl() != null
                        && !primaryImage.getImageUrl().isBlank()) {

                    productImageByProductId.put(
                            productId,
                            primaryImage.getImageUrl()
                    );

                } else {

                    List<ProductImage> images =
                            imageDAO.findByProductId(productId);

                    for (ProductImage image : images) {

                        if (image.getImageUrl() != null
                                && !image.getImageUrl().isBlank()) {

                            productImageByProductId.put(
                                    productId,
                                    image.getImageUrl()
                            );

                            break;
                        }
                    }
                }
            }

            // Send the data to index.jsp.
            request.setAttribute("categories", categories);
            request.setAttribute("brands", brands);
            request.setAttribute("products", products);

            request.setAttribute(
                    "brandNameById",
                    brandNameById
            );

            request.setAttribute(
                    "productImageByProductId",
                    productImageByProductId
            );

            request.setAttribute(
                    "searchQuery",
                    request.getParameter("q")
            );

            request.setAttribute(
                    "department",
                    request.getParameter("department")
            );

            // Render homepage through the Servlet.
            request.getRequestDispatcher(
                    "/WEB-INF/views/index.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load Fashion Store catalog data.",
                    e
            );
        }
    }
}
