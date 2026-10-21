<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Login | Fashion Store</title>

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>

<body>

    <main class="auth-container">
        <section class="auth-card">

            <!-- Fashion Store Logo -->
            <a class="auth-logo"
               href="${pageContext.request.contextPath}/home">
                FASHION STORE
            </a>

            <h1>Welcome Back</h1>

            <p class="auth-subtitle">
                Log in to your Fashion Store account.
            </p>

            <!-- Error Message -->
            <%
                String errorMessage =
                    (String) request.getAttribute("errorMessage");

                if (errorMessage != null && !errorMessage.isBlank()) {
            %>
                <p class="error-message">
                    <%= errorMessage.replace("&", "&amp;")
                                    .replace("<", "&lt;")
                                    .replace(">", "&gt;")
                                    .replace("\"", "&quot;")
                                    .replace("'", "&#39;") %>
                </p>
            <%
                }
            %>

            <!-- Success Message -->
            <%
                String successMessage =
                    (String) request.getAttribute("successMessage");

                if (successMessage != null
                        && !successMessage.isBlank()) {
            %>
                <p class="success-message">
                    <%= successMessage.replace("&", "&amp;")
                                      .replace("<", "&lt;")
                                      .replace(">", "&gt;")
                                      .replace("\"", "&quot;")
                                      .replace("'", "&#39;") %>
                </p>
            <%
                }
            %>

            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/login"
                  method="post">

                <div class="form-group">
                    <label for="email">Email Address</label>

                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Enter your email address"
                        autocomplete="email"
                        required>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>

                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Enter your password"
                        autocomplete="current-password"
                        required>
                </div>

                <button type="submit" class="auth-button">
                    Login
                </button>

            </form>

            <p class="auth-switch">
                Don't have an account?
                <a href="${pageContext.request.contextPath}/register">
                    Create Account
                </a>
            </p>

            <p class="auth-switch">
                <a href="${pageContext.request.contextPath}/home">
                    Back to Home
                </a>
            </p>

        </section>
    </main>

</body>
</html>