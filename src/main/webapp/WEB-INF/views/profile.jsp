<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="com.fashionstore.model.User,java.sql.Timestamp,java.text.SimpleDateFormat" %>

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

    private String displayValue(String value) {
        return value == null ? "" : value;
    }

    private String formatProfileDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "Not available";
        }

        return new SimpleDateFormat("dd MMM yyyy")
                .format(timestamp);
    }
%>

<%
    String contextPath = request.getContextPath();

    User profileUser =
        (User) request.getAttribute("profileUser");

    String profileError =
        (String) request.getAttribute("profileError");

    String profileSuccess =
        (String) request.getAttribute("profileSuccess");

    if (profileUser == null) {
        profileUser = new User();
    }

    String fullName = displayValue(profileUser.getFullName());
    String email = displayValue(profileUser.getEmail());
    String phone = displayValue(profileUser.getPhone());
    String address = displayValue(profileUser.getAddress());

    long userId = profileUser.getUserId();

    Timestamp createdAt = profileUser.getCreatedAt();
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>My Profile | Fashion Store</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/style.css">

    <link rel="stylesheet"
          href="<%= contextPath %>/assets/css/profile.css">
</head>

<body>

    <!-- Website Header -->

    <jsp:include page="/WEB-INF/views/partials/header.jsp" />

    <main class="profile-page">

        <div class="profile-container">

            <!-- Page Heading -->

            <section class="profile-page-heading">

                <div>
                    <h1>My Profile</h1>

                    <p>
                        Manage your personal information and delivery details.
                    </p>
                </div>

            </section>

            <!-- Success Message -->

            <% if (profileSuccess != null) { %>

                <div class="profile-alert profile-alert-success"
                     role="status">

                    <%= escapeHtml(profileSuccess) %>

                </div>

            <% } %>

            <!-- Error Message -->

            <% if (profileError != null) { %>

                <div class="profile-alert profile-alert-error"
                     role="alert">

                    <%= escapeHtml(profileError) %>

                </div>

            <% } %>

            <div class="profile-layout">

                <!-- Profile Information Form -->

                <section class="profile-card">

                    <div class="profile-card-heading">

                        <div class="profile-avatar"
                             aria-hidden="true">

                            <%= escapeHtml(
                                fullName.isBlank()
                                    ? "U"
                                    : fullName.substring(0, 1)
                                        .toUpperCase(java.util.Locale.ROOT)
                            ) %>

                        </div>

                        <div>

                            <h2>
                                <%= escapeHtml(
                                    fullName.isBlank()
                                        ? "Your Profile"
                                        : fullName
                                ) %>
                            </h2>

                            <p>Edit your information below.</p>

                        </div>

                    </div>

                    <div class="profile-card-divider"></div>

                    <form action="<%= contextPath %>/profile"
                          method="post"
                          class="profile-form">

                        <!-- Full Name -->

                        <div class="profile-form-group">

                            <label for="fullName">
                                Full Name <span class="required">*</span>
                            </label>

                            <input
                                type="text"
                                id="fullName"
                                name="fullName"
                                value="<%= escapeHtml(fullName) %>"
                                placeholder="Enter your full name"
                                maxlength="100"
                                autocomplete="name"
                                required>

                        </div>

                        <!-- Email -->

                        <div class="profile-form-group">

                            <label for="email">
                                Email Address <span class="required">*</span>
                            </label>

                            <input
                                type="email"
                                id="email"
                                name="email"
                                value="<%= escapeHtml(email) %>"
                                placeholder="Enter your email address"
                                maxlength="150"
                                autocomplete="email"
                                required>

                            <small>
                                Your email address must be unique to your account.
                            </small>

                        </div>

                        <!-- Phone Number -->

                        <div class="profile-form-group">

                            <label for="phone">
                                Phone Number
                            </label>

                            <input
                                type="tel"
                                id="phone"
                                name="phone"
                                value="<%= escapeHtml(phone) %>"
                                placeholder="Enter your phone number"
                                maxlength="20"
                                autocomplete="tel">

                        </div>

                        <!-- Shipping Address -->

                        <div class="profile-form-group">

                            <label for="address">
                                Shipping Address
                            </label>

                            <textarea
                                id="address"
                                name="address"
                                rows="4"
                                maxlength="500"
                                placeholder="Enter your delivery address"
                                autocomplete="street-address"><%= escapeHtml(address) %></textarea>

                        </div>

                        <!-- Actions -->

                        <div class="profile-form-actions">

                            <button type="submit"
                                    class="profile-save-button">
                                Save Changes
                            </button>

                            <a href="<%= contextPath %>/home"
                               class="profile-cancel-button">
                                Cancel
                            </a>

                        </div>

                    </form>

                </section>

                <!-- Account Information -->

                <aside class="profile-card profile-account-card">

                    <h2>Account Information</h2>

                    <p class="profile-account-description">
                        Details associated with your Fashion Store account.
                    </p>

                    <div class="profile-account-detail">

                        <span>Customer ID</span>

                        <strong>
                            <%= userId > 0 ? String.valueOf(userId) : "Not available" %>
                        </strong>

                    </div>

                    <div class="profile-account-detail">

                        <span>Member Since</span>

                        <strong>
                            <%= escapeHtml(formatProfileDate(createdAt)) %>
                        </strong>

                    </div>

                    <div class="profile-account-detail">

                        <span>Account Email</span>

                        <strong class="profile-account-email">
                            <%= escapeHtml(email) %>
                        </strong>

                    </div>

                    <div class="profile-account-divider"></div>

                    <a href="<%= contextPath %>/orders"
                       class="profile-quick-link">

                        <span>My Orders</span>

                        <span aria-hidden="true">&rarr;</span>

                    </a>

                    <a href="<%= contextPath %>/cart"
                       class="profile-quick-link">

                        <span>My Cart</span>

                        <span aria-hidden="true">&rarr;</span>

                    </a>

                </aside>

            </div>

        </div>

    </main>

    <!-- Website Footer -->

    <jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>