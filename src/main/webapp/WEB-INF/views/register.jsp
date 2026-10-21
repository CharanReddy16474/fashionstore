<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Create Account | Fashion Store</title>

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>

<body>

    <main class="auth-container">
        <section class="auth-card">

            <a class="auth-logo"
                href="${pageContext.request.contextPath}/home">
                FASHION STORE
            </a>

            <h1>Create Account</h1>
            <p class="auth-subtitle">
                Create your account to start shopping.
            </p>

            <%-- Display registration errors, if any --%>
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

            <form action="${pageContext.request.contextPath}/register"
                method="post">

                <div class="form-group">
                    <label for="fullName">Full Name</label>
                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        placeholder="Enter your full name"
                        autocomplete="name"
                        required>
                </div>

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
                    <label for="phone">Phone Number</label>
                    <input
                        type="tel"
                        id="phone"
                        name="phone"
                        placeholder="Enter your phone number"
                        autocomplete="tel">
                </div>

                <div class="form-group">
                    <label for="address">Address</label>
                    <textarea
                        id="address"
                        name="address"
                        placeholder="Enter your address"
                        autocomplete="street-address"
                        rows="3"></textarea>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Create a password"
                        autocomplete="new-password"
                        minlength="8"
                        required>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        placeholder="Re-enter your password"
                        autocomplete="new-password"
                        required>
                </div>

                <button type="submit" class="auth-button">
                    Create Account
                </button>

            </form>

            <p class="auth-switch">
                Already have an account?
                <a href="${pageContext.request.contextPath}/login">
                    Login
                </a>
            </p>

        </section>
    </main>

</body>
</html>