package com.fashionstore.util;

import com.fashionstore.dao.*;
import com.fashionstore.dao.implementation.*;
import com.fashionstore.model.*;

public class FashionStoreDaoTest {

    private static int passed = 0;
    private static int failed = 0;

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }

    public static void main(String[] args) {

        System.out.println("==================================");
        System.out.println("   FASHION STORE DAO TESTING");
        System.out.println("==================================");

        // 1. USER DAO
        test("UserDAO", () -> {
            User user = new UserDAOImpl()
                    .findByEmail("rahul@example.com")
                    .orElseThrow(() ->
                            new Exception("Sample customer not found"));

            System.out.println("Customer: " + user.getFullName());
        });

        // 2. CATEGORY DAO
        test("CategoryDAO", () -> {
            var categories = new CategoryDAOImpl()
                    .findTopLevelActive();

            System.out.println("Main categories: " + categories.size());

            if (categories.isEmpty()) {
                throw new Exception("No categories found");
            }
        });

        // 3. BRAND DAO
        test("BrandDAO", () -> {
            var brands = new BrandDAOImpl().findAllActive();

            System.out.println("Active brands: " + brands.size());

            if (brands.isEmpty()) {
                throw new Exception("No brands found");
            }
        });

        // 4. PRODUCT DAO
        test("ProductDAO", () -> {
            var products = new ProductDAOImpl().findAllActive();

            System.out.println("Active products: " + products.size());

            var product = new ProductDAOImpl()
                    .findActiveById(101)
                    .orElseThrow(() ->
                            new Exception("Product 101 not found"));

            System.out.println("Product: " + product.getProductName());
            System.out.println("Price: " + product.getPrice());

            if (products.isEmpty()) {
                throw new Exception("No products found");
            }
        });

        // 5. PRODUCT VARIANT DAO
        test("ProductVariantDAO", () -> {
            var variants = new ProductVariantDAOImpl()
                    .findByProductId(101);

            System.out.println("Variants: " + variants.size());

            if (variants.isEmpty()) {
                throw new Exception("No variants found");
            }
        });

        // 6. PRODUCT IMAGE DAO
        test("ProductImageDAO", () -> {
            var images = new ProductImageDAOImpl()
                    .findByProductId(101);

            System.out.println("Product images: " + images.size());

            if (images.isEmpty()) {
                throw new Exception("No product images found");
            }
        });

        // 7. PRODUCT REVIEW DAO
        test("ProductReviewDAO", () -> {
            var dao = new ProductReviewDAOImpl();

            long count = dao.countByProductId(101);
            double rating = dao.getAverageRating(101);

            System.out.println("Review count: " + count);
            System.out.println("Average rating: " + rating);

            if (count == 0) {
                throw new Exception("No reviews found");
            }
        });

        // 8. CART DAO
        test("CartDAO", () -> {
            var cart = new CartDAOImpl()
                    .findByUserId(1)
                    .orElseThrow(() ->
                            new Exception("Customer cart not found"));

            System.out.println("Cart ID: " + cart.getCartId());
        });

        // 9. CART ITEM DAO
        test("CartItemDAO", () -> {
            var items = new CartItemDAOImpl().findByCartId(1);

            System.out.println("Cart items: " + items.size());

            if (items.isEmpty()) {
                throw new Exception("No cart items found");
            }
        });

        // 10. ORDER DAO
        test("OrderDAO", () -> {
            var orders = new OrderDAOImpl().findByUserId(1);

            System.out.println("Customer orders: " + orders.size());

            if (orders.isEmpty()) {
                throw new Exception("No orders found");
            }
        });

        // 11. ORDER ITEM DAO
        test("OrderItemDAO", () -> {
            var order = new OrderDAOImpl()
                    .findByOrderNumber("FS-DEMO-0001")
                    .orElseThrow(() ->
                            new Exception("Sample order not found"));

            var items = new OrderItemDAOImpl()
                    .findByOrderId(order.getOrderId());

            System.out.println("Items in order: " + items.size());

            if (items.isEmpty()) {
                throw new Exception("No order items found");
            }
        });

        // 12. PAYMENT DAO
        test("PaymentDAO", () -> {
            var order = new OrderDAOImpl()
                    .findByOrderNumber("FS-DEMO-0001")
                    .orElseThrow(() ->
                            new Exception("Sample order not found"));

            var payments = new PaymentDAOImpl()
                    .findByOrderId(order.getOrderId());

            System.out.println("Payment records: " + payments.size());

            if (payments.isEmpty()) {
                throw new Exception("No payments found");
            }
        });

        System.out.println();
        System.out.println("==================================");
        System.out.println("PASSED: " + passed);
        System.out.println("FAILED: " + failed);
        System.out.println("==================================");
    }

    private static void test(
            String name, TestAction action) {

        System.out.println();
        System.out.println("Testing " + name + "...");

        try {
            action.run();
            passed++;
            System.out.println("[PASS] " + name);

        } catch (Exception e) {
            failed++;
            System.out.println("[FAIL] " + name);
            System.out.println("Reason: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}