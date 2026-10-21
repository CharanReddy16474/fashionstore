
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.fashionstore.model.User" %>

<%!
    private String escapeHeaderHtml(String value) {
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

    Object headerUserObject = session.getAttribute("loggedInUser");
    boolean headerLoggedIn = headerUserObject instanceof User;

    String headerUserName = "";

    if (headerLoggedIn) {
        User headerUser = (User) headerUserObject;
        headerUserName = headerUser.getFullName();

        if (headerUserName == null || headerUserName.isBlank()) {
            headerUserName = "Customer";
        }
    }

    String department = request.getParameter("department");

    Object departmentAttribute = request.getAttribute("department");
    if (departmentAttribute instanceof String) {
        department = (String) departmentAttribute;
    }

    if (department == null) {
        department = "";
    }

    String normalizedDepartment = department.toLowerCase();

    boolean menActive = normalizedDepartment.equals("men")
            || normalizedDepartment.startsWith("mens-");

    boolean womenActive = normalizedDepartment.equals("women")
            || normalizedDepartment.startsWith("womens-");

    boolean kidsActive = normalizedDepartment.equals("kids")
            || normalizedDepartment.startsWith("kids-");

    String requestUri = request.getRequestURI();

    boolean homeActive = requestUri != null
            && requestUri.endsWith("/home")
            && !menActive && !womenActive && !kidsActive;

    String searchQuery = request.getParameter("q");

    if (searchQuery == null) {
        Object queryAttribute = request.getAttribute("searchQuery");

        if (queryAttribute instanceof String) {
            searchQuery = (String) queryAttribute;
        } else {
            searchQuery = "";
        }
    }
%>

<link rel="stylesheet"
      href="<%= contextPath %>/assets/css/profile-menu.css">

<style>
    /* Keep the original Fashion Store image visible at the far left. */
    .site-header .brand-logo {
        display: inline-flex;
        flex: 0 0 58px;
        width: 58px;
        height: 58px;
        align-items: center;
        justify-content: center;
        margin: 0;
        padding: 0;
        text-decoration: none;
    }

    .site-header .brand-logo-image {
        display: block !important;
        width: 58px !important;
        height: 58px !important;
        max-width: none !important;
        object-fit: contain;
        object-position: center;
        flex: 0 0 58px;
        visibility: visible !important;
        opacity: 1 !important;
    }

    .profile-menu {
        position: relative;
    }

    .profile-menu-details {
        position: relative;
    }

    .profile-menu-details > summary {
        list-style: none;
        cursor: pointer;
    }

    .profile-menu-details > summary::-webkit-details-marker {
        display: none;
    }

    .profile-dropdown {
        display: none;
        position: absolute;
        top: calc(100% + 12px);
        right: 0;
        z-index: 2000;
        width: 235px;
        padding: 18px;
        background: #fff;
        border: 1px solid #ececf0;
        border-radius: 10px;
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
        color: #282c3f;
    }

    .profile-menu-details[open] .profile-dropdown {
        display: block;
    }

    .profile-dropdown-heading {
        margin: 0 0 5px;
        font-size: 15px;
        font-weight: 700;
    }

    .profile-dropdown-message {
        margin: 0 0 14px;
        color: #777985;
        font-size: 12px;
        line-height: 1.5;
    }

    .profile-dropdown a,
    .profile-dropdown button {
        display: block;
        width: 100%;
        padding: 10px 0;
        border: 0;
        background: transparent;
        color: #282c3f;
        font: inherit;
        font-size: 13px;
        text-align: left;
        text-decoration: none;
        cursor: pointer;
    }

    .profile-dropdown a:hover,
    .profile-dropdown button:hover {
        color: #ff3f6c;
    }

    .profile-dropdown-divider {
        height: 1px;
        margin: 7px 0;
        background: #eeeeF2;
    }

    .header-search {
        min-width: 0;
    }

    .header-search .search-submit {
        display: inline-flex;
        flex: 0 0 auto;
        align-items: center;
        justify-content: center;
        padding: 0;
        border: 0;
        background: transparent;
        color: inherit;
        cursor: pointer;
    }

    .header-search .search-submit svg {
        display: block;
        width: 19px;
        height: 19px;
    }

    @media (max-width: 600px) {
        .site-header .brand-logo {
            flex-basis: 42px;
            width: 42px;
            height: 42px;
        }

        .site-header .brand-logo-image {
            width: 42px !important;
            height: 42px !important;
            flex-basis: 42px;
        }

        .profile-dropdown {
            width: 215px;
        }
    }
</style>

<header class="site-header">
    <div class="header-inner">

        <!-- YOUR ORIGINAL FASHION STORE LOGO: FAR LEFT -->
        <a class="brand-logo"
           href="<%= contextPath %>/home"
           aria-label="Fashion Store Home">

            <img
                src="<%= contextPath %>/assets/images/FS_icon.png"
                alt="Fashion Store Logo"
                class="brand-logo-image">
        </a>

        <!-- MAIN NAVIGATION -->
        <nav class="main-nav" aria-label="Main navigation">

            <a class="nav-link<%= homeActive ? " active" : "" %>"
               href="<%= contextPath %>/home">
                Home
            </a>

            <a class="nav-link<%= menActive ? " active" : "" %>"
               href="<%= contextPath %>/products?department=men">
                Men's
            </a>

            <a class="nav-link<%= womenActive ? " active" : "" %>"
               href="<%= contextPath %>/products?department=women">
                Women's
            </a>

            <a class="nav-link<%= kidsActive ? " active" : "" %>"
               href="<%= contextPath %>/products?department=kids">
                Kids
            </a>

        </nav>

        <!-- PRODUCT SEARCH -->
        <form class="header-search"
              id="catalogSearchForm"
              action="<%= contextPath %>/products"
              method="get">

            <button class="search-submit"
                    type="submit"
                    aria-label="Search products">

                <svg class="search-icon"
                     viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.8"
                     stroke-linecap="round"
                     stroke-linejoin="round"
                     aria-hidden="true">

                    <circle cx="10.8" cy="10.8" r="6.8"></circle>
                    <path d="M16 16l4.5 4.5"></path>

                </svg>
            </button>

            <input
                type="search"
                id="catalogSearch"
                name="q"
                placeholder="Search products, brands and more"
                value="<%= escapeHeaderHtml(searchQuery) %>"
                autocomplete="off"
                aria-label="Search products, brands and more">
        </form>

        <!-- PROFILE AND CART -->
        <div class="header-actions">

            <div class="profile-menu">
                <details class="profile-menu-details">

                    <summary class="header-action profile-menu-toggle"
                             aria-label="Profile">

                        <!-- Profile/person icon -->
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.6"
                             stroke-linecap="round"
                             stroke-linejoin="round"
                             aria-hidden="true">

                            <circle cx="12" cy="8" r="4"></circle>
                            <path d="M4.5 21c0-4.1 3.35-7 7.5-7s7.5 2.9 7.5 7"></path>

                        </svg>

                        <span>Profile</span>
                    </summary>

                    <div class="profile-dropdown">

                        <% if (headerLoggedIn) { %>

                            <p class="profile-dropdown-heading">
                                Welcome, <%= escapeHeaderHtml(headerUserName) %>
                            </p>

                            <p class="profile-dropdown-message">
                                Manage your Fashion Store account.
                            </p>

                            <a href="<%= contextPath %>/profile">
                                My Profile
                            </a>

                            <a href="<%= contextPath %>/orders">
                                My Orders
                            </a>

                            <a href="<%= contextPath %>/cart">
                                Cart
                            </a>

                            <div class="profile-dropdown-divider"></div>

                            <form action="<%= contextPath %>/logout"
                                  method="post">

                                <button type="submit">Logout</button>
                            </form>

                        <% } else { %>

                            <p class="profile-dropdown-heading">
                                Welcome to Fashion Store
                            </p>

                            <p class="profile-dropdown-message">
                                Login to view your account.
                            </p>

                            <a href="<%= contextPath %>/login">
                                Login
                            </a>

                            <a href="<%= contextPath %>/register">
                                Register
                            </a>

                        <% } %>

                    </div>
                </details>
            </div>

            <!-- Shopping cart icon; no wishlist or extra bag icon -->
            <a class="header-action"
               href="<%= contextPath %>/cart"
               aria-label="Shopping Cart">

                <svg viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="1.6"
                     stroke-linecap="round"
                     stroke-linejoin="round"
                     aria-hidden="true">

                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="19" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>

                </svg>

                <span>Cart</span>
            </a>

        </div>
    </div>
</header>
