
<footer class="site-footer">

    <div class="footer-main">

        <div class="footer-brand-section">

            <a class="footer-brand"
               href="<%= request.getContextPath() %>/home">

                <span class="footer-brand-mark">F</span>
                <span>FASHION STORE</span>

            </a>

            <p class="footer-description">
                Discover your style with fashion essentials,
                everyday favourites, and new seasonal collections.
            </p>

        </div>

        <div class="footer-links-section">

            <h3>Explore</h3>

            <a href="<%= request.getContextPath() %>/home">
                Home
            </a>

            <a href="<%= request.getContextPath() %>/home?department=men">
                Men's Fashion
            </a>

            <a href="<%= request.getContextPath() %>/home?department=women">
                Women's Fashion
            </a>

            <a href="<%= request.getContextPath() %>/home?department=kids">
                Kids' Fashion
            </a>

        </div>

        <div class="footer-links-section">

            <h3>Your Account</h3>

            <a href="<%= request.getContextPath() %>/login">
                Login
            </a>

            <a href="<%= request.getContextPath() %>/register">
                Create Account
            </a>

            <a href="<%= request.getContextPath() %>/cart">
                Cart
            </a>

            <a href="<%= request.getContextPath() %>/my-orders">
                My Orders
            </a>

        </div>

    </div>

    <div class="footer-bottom">
        <p>&copy; 2026 Fashion Store. All rights reserved.</p>
        <p>Made for your everyday style.</p>
    </div>

</footer>
