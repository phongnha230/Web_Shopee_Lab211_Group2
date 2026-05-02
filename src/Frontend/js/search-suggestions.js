/**
 * search-suggestions.js
 * Shopee-style search suggestion dropdown — shared across all pages.
 *
 * Requirements in the host HTML:
 *   - <div id="searchWrapper">            wrapper with relative positioning
 *   - <input id="globalSearchInput">      the text input
 *   - <button id="globalSearchButton">    the search button
 *   - <div id="searchSuggestionDropdown"> the dropdown container
 *   - <div id="suggestionTrendingHeader"> header shown when KEYWORD items exist
 *   - <div id="suggestionList">           items rendered here
 *   - window.API_BASE_URL must be defined before this script runs
 */
(function () {
    'use strict';

    function initSearchSuggestions() {
        const searchInput  = document.getElementById('globalSearchInput');
        const searchButton = document.getElementById('globalSearchButton');
        const wrapper      = document.getElementById('searchWrapper');
        const dropdown     = document.getElementById('searchSuggestionDropdown');
        const suggList     = document.getElementById('suggestionList');
        const trendHeader  = document.getElementById('suggestionTrendingHeader');

        if (!searchInput || !searchButton || !dropdown) return;

        let debounceTimer  = null;
        let currentKeyword = '';
        const API_BASE     = window.API_BASE_URL || 'http://localhost:8088/api';

        // Pre-fill from ?search= URL param if present
        const urlKw = new URLSearchParams(window.location.search).get('search');
        if (urlKw && !searchInput.value) searchInput.value = urlKw;

        // ── Navigate to search results ────────────────────────────────────────
        const goSearch = (keyword) => {
            const kw = (keyword || searchInput.value).trim();
            if (!kw) return;
            closeDropdown();
            window.location.href = `/category?search=${encodeURIComponent(kw)}`;
        };

        searchButton.addEventListener('click', () => goSearch());
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter')  goSearch();
            if (e.key === 'Escape') closeDropdown();
        });

        // Close on outside click
        document.addEventListener('click', (e) => {
            if (wrapper && !wrapper.contains(e.target)) closeDropdown();
        });

        const closeDropdown = () => dropdown.classList.add('hidden');
        const openDropdown  = () => dropdown.classList.remove('hidden');

        // ── Fetch with debounce ────────────────────────────────────────────────
        searchInput.addEventListener('input', () => {
            const kw = searchInput.value.trim();
            currentKeyword = kw;
            clearTimeout(debounceTimer);
            if (!kw) { closeDropdown(); return; }
            debounceTimer = setTimeout(() => fetchSuggestions(kw), 220);
        });

        searchInput.addEventListener('focus', () => {
            const kw = searchInput.value.trim();
            if (kw) fetchSuggestions(kw);
        });

        // ── Fetch & render ─────────────────────────────────────────────────────
        async function fetchSuggestions(keyword) {
            try {
                const res = await fetch(
                    `${API_BASE}/search/suggestions?keyword=${encodeURIComponent(keyword)}&limit=12`
                );
                if (!res.ok) return;
                const data = await res.json();
                if (keyword !== currentKeyword) return; // stale response guard
                renderSuggestions(data.suggestions || [], keyword);
            } catch (err) {
                console.warn('[SearchSuggestion] fetch error:', err);
            }
        }

        function renderSuggestions(items, keyword) {
            if (!items.length) { closeDropdown(); return; }

            const hasKeywords = items.some(i => i.type === 'KEYWORD');
            if (trendHeader) trendHeader.classList.toggle('hidden', !hasKeywords);

            suggList.innerHTML = items.map(item => buildItem(item, keyword)).join('');

            suggList.querySelectorAll('.sugg-item').forEach(el => {
                el.addEventListener('click', () => {
                    const { keyword: kw, id, type } = el.dataset;
                    if (type === 'PRODUCT' && id) {
                        closeDropdown();
                        window.location.href = `/product-detail?id=${id}`;
                    } else if (type === 'CATEGORY' && id) {
                        closeDropdown();
                        window.location.href = `/category?id=${id}`;
                    } else if (kw) {
                        searchInput.value = kw;
                        goSearch(kw);
                    }
                });
            });

            openDropdown();
        }

        // ── Build HTML for one suggestion item ─────────────────────────────────
        function buildItem(item, keyword) {
            const hl = (text) =>
                text.replace(
                    new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi'),
                    '<strong>$1</strong>'
                );
            const esc = (s) =>
                (s || '').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            const encodedId = (item.id == null ? '' : String(item.id)).replace(/"/g, '&quot;');
            const attr = `data-type="${item.type}" data-id="${encodedId}" data-keyword="${esc(item.label)}"`;
            const base = `sugg-item flex items-center px-4 py-2 hover:bg-gray-50 cursor-pointer border-b border-gray-50 last:border-0`;

            if (item.type === 'PRODUCT') {
                const price = item.minPrice
                    ? `<span class="text-[#AD3029] text-xs font-semibold ml-auto pl-3 whitespace-nowrap">₫${Number(item.minPrice).toLocaleString('vi-VN')}</span>`
                    : '';
                const sold = item.sold > 0
                    ? `<span class="text-gray-400 text-[11px] ml-1">Đã bán ${item.sold >= 1000 ? (item.sold / 1000).toFixed(1) + 'k' : item.sold}</span>`
                    : '';
                const thumb = item.imageUrl
                    ? `<img src="${item.imageUrl}" class="w-9 h-9 object-cover rounded flex-shrink-0 mr-3" onerror="this.style.display='none'">`
                    : `<span class="w-9 h-9 flex-shrink-0 mr-3 flex items-center justify-center text-gray-300"><i class="fas fa-box text-lg"></i></span>`;
                return `<div class="${base}" ${attr}>${thumb}<div class="flex-1 min-w-0"><div class="text-sm text-gray-800 truncate">${hl(item.label)}</div><div class="flex items-center">${sold}</div></div>${price}</div>`;
            }

            if (item.type === 'CATEGORY') {
                return `<div class="${base}" ${attr}><span class="w-9 h-9 flex-shrink-0 mr-3 flex items-center justify-center text-[#AD3029]"><i class="fas fa-th-large text-base"></i></span><div class="flex-1 min-w-0"><div class="text-sm text-gray-800 truncate">${hl(item.label)}</div><div class="text-xs text-gray-400">Danh mục</div></div><i class="fas fa-chevron-right text-gray-300 text-xs ml-2"></i></div>`;
            }

            if (item.type === 'KEYWORD') {
                return `<div class="${base} border-b-0" ${attr}><span class="w-9 h-9 flex-shrink-0 mr-3 flex items-center justify-center text-gray-400"><i class="fas fa-search text-sm"></i></span><span class="text-sm text-gray-700 truncate flex-1">${hl(item.label)}</span></div>`;
            }

            if (item.type === 'ATTRIBUTE') {
                const icon  = item.attributeType === 'color' ? 'fa-palette' : 'fa-ruler-combined';
                const label = item.attributeType === 'color' ? 'Màu sắc' : 'Kích cỡ';
                return `<div class="${base} border-b-0" ${attr}><span class="w-9 h-9 flex-shrink-0 mr-3 flex items-center justify-center text-gray-400"><i class="fas ${icon} text-sm"></i></span><span class="text-sm text-gray-700 flex-1">${hl(item.label)}</span><span class="text-[11px] text-gray-400 bg-gray-100 px-2 py-0.5 rounded ml-2">${label}</span></div>`;
            }

            return '';
        }
    }

    // Auto-init on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSearchSuggestions);
    } else {
        initSearchSuggestions();
    }
})();
