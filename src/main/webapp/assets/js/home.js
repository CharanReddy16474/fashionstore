
/* =========================================
   FASHION STORE
   Search and Product Sorting
   File: home.js
   ========================================= */

document.addEventListener("DOMContentLoaded", function () {

    const productGrid = document.getElementById("productGrid");

    if (!productGrid) {
        return;
    }

    const products = Array.from(
        productGrid.querySelectorAll(".product-card")
    );

    const searchInput = document.getElementById("catalogSearch");
    const searchForm = document.getElementById("catalogSearchForm");
    const sortSelect = document.getElementById("sortProducts");
    const countLabel = document.getElementById("catalogItemCount");
    const resultsCount = document.getElementById("resultsCount");
    const noResults = document.getElementById("noResults");
    const resetSearch = document.getElementById("resetSearch");

    const urlParams = new URLSearchParams(window.location.search);

    const initialQuery = (urlParams.get("q") || "").trim();
    const department = (urlParams.get("department") || "")
        .trim()
        .toLowerCase();

    // Preserve the original database order for Recommended sorting.
    const originalOrder = new Map();

    products.forEach(function (product, index) {
        originalOrder.set(product, index);
    });

    // Restore the search term after a page reload.
    if (searchInput && initialQuery) {
        searchInput.value = initialQuery;
    }

    // Match departments without confusing "men" with "women".
    function matchesDepartment(product, selectedDepartment) {

        if (!selectedDepartment) {
            return true;
        }

        const text = product.dataset.department || "";

        const patterns = {
            men: /\bmen(?:'s)?\b/i,
            women: /\bwomen(?:'s)?\b/i,
            kids: /\bkids?(?:'s)?\b/i
        };

        const pattern = patterns[selectedDepartment];

        return pattern ? pattern.test(text) : true;
    }

    // Search products and update the result count.
    function applySearch() {

        const query = searchInput
            ? searchInput.value.trim().toLowerCase()
            : "";

        let visibleCount = 0;

        products.forEach(function (product) {

            const searchableText =
                (product.dataset.search || "").toLowerCase();

            const matchesQuery =
                query === "" || searchableText.includes(query);

            const matchesCategory =
                matchesDepartment(product, department);

            const visible = matchesQuery && matchesCategory;

            product.hidden = !visible;

            if (visible) {
                visibleCount++;
            }
        });

        if (countLabel) {
            countLabel.textContent = visibleCount;
        }

        if (resultsCount) {
            resultsCount.replaceChildren(
                document.createTextNode("Showing ")
            );

            const strong = document.createElement("strong");
            strong.textContent = visibleCount;

            resultsCount.append(
                strong,
                document.createTextNode(" products")
            );
        }

        if (noResults) {
            noResults.hidden = visibleCount > 0;
        }

        sortProducts();
    }

    // Sort products by recommendation, price or name.
    function sortProducts() {

        const sortType = sortSelect
            ? sortSelect.value
            : "featured";

        const sortedProducts = products.slice();

        sortedProducts.sort(function (a, b) {

            const priceA = Number(a.dataset.price || 0);
            const priceB = Number(b.dataset.price || 0);

            const nameA = a.dataset.search || "";
            const nameB = b.dataset.search || "";

            if (sortType === "price-low") {
                return priceA - priceB;
            }

            if (sortType === "price-high") {
                return priceB - priceA;
            }

            if (sortType === "name") {
                return nameA.localeCompare(nameB);
            }

            return originalOrder.get(a) - originalOrder.get(b);
        });

        // Reorder cards while preserving the empty-results message.
        sortedProducts.forEach(function (product) {
            productGrid.insertBefore(product, noResults);
        });
    }

    // Live search as the customer types.
    if (searchInput) {
        searchInput.addEventListener("input", applySearch);
    }

    // Submit non-empty searches through the existing /home URL.
    if (searchForm && searchInput) {

        searchForm.addEventListener("submit", function (event) {

            if (searchInput.value.trim() === "") {
                event.preventDefault();
                searchInput.focus();
            }
        });
    }

    // Sorting dropdown.
    if (sortSelect) {
        sortSelect.addEventListener("change", sortProducts);
    }

    // Clear the search.
    if (resetSearch) {

        resetSearch.addEventListener("click", function () {

            if (searchInput) {
                searchInput.value = "";
            }

            applySearch();
        });
    }

    // Initial page display.
    applySearch();

});
