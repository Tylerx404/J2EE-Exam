document.addEventListener("DOMContentLoaded", () => {
    const searchForm = document.getElementById("doctor-search-form");
    const resultsContainer = document.getElementById("doctor-results");

    if (!searchForm || !resultsContainer) {
        return;
    }

    const ajaxBaseUrl = searchForm.dataset.ajaxUrl;
    const keywordInput = searchForm.querySelector('input[name="keyword"]');

    async function loadResults(url, pushState) {
        resultsContainer.classList.add("is-loading");
        try {
            const response = await fetch(url, {
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            });
            if (!response.ok) {
                return;
            }

            const html = await response.text();
            resultsContainer.innerHTML = html;

            if (pushState) {
                const pageUrl = new URL(url, window.location.origin);
                const keyword = pageUrl.searchParams.get("keyword") || "";
                const page = pageUrl.searchParams.get("page") || "0";
                const browserUrl = new URL("/home", window.location.origin);
                if (keyword) {
                    browserUrl.searchParams.set("keyword", keyword);
                }
                if (page !== "0") {
                    browserUrl.searchParams.set("page", page);
                }
                window.history.replaceState({}, "", browserUrl);
            }
        }
        finally {
            resultsContainer.classList.remove("is-loading");
        }
    }

    searchForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const params = new URLSearchParams(new FormData(searchForm));
        params.set("page", "0");
        loadResults(`${ajaxBaseUrl}?${params.toString()}`, true);
    });

    resultsContainer.addEventListener("click", (event) => {
        const link = event.target.closest(".ajax-page-link");
        if (!link || link.classList.contains("disabled")) {
            return;
        }

        event.preventDefault();
        const linkUrl = new URL(link.href);
        const params = new URLSearchParams(linkUrl.search);
        if (keywordInput && !params.has("keyword")) {
            params.set("keyword", keywordInput.value.trim());
        }
        loadResults(`${ajaxBaseUrl}?${params.toString()}`, true);
    });
});
