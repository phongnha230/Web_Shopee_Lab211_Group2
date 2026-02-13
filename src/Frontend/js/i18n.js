const I18n = {
    currentLang: 'vi', // Default to Vietnamese

    init() {
        const savedLang = localStorage.getItem('goshop_lang');
        if (savedLang) {
            this.currentLang = savedLang;
        }
        this.applyLanguage(this.currentLang);
        this.updateToggleButton();
    },

    toggleLanguage() {
        this.currentLang = this.currentLang === 'en' ? 'vi' : 'en';
        localStorage.setItem('goshop_lang', this.currentLang);
        this.applyLanguage(this.currentLang);
        this.updateToggleButton();
        window.location.reload(); // Reload to refresh simple UI states if needed
    },

    applyLanguage(lang) {
        const t = translations[lang];
        if (!t) return;

        // Find all elements with data-i18n attribute
        const elements = document.querySelectorAll('[data-i18n]');
        elements.forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (t[key]) {
                if (el.tagName === 'INPUT' && el.getAttribute('placeholder')) {
                    el.placeholder = t[key];
                } else {
                    el.innerText = t[key];
                }
            }
        });
    },

    updateToggleButton() {
        const btn = document.getElementById('lang-toggle');
        if (btn) {
            // Logic: Button shows the LANGUAGE YOU CAN SWITCH TO.
            // If current is 'vi', show 'English' (and UK flag).
            // If current is 'en', show 'Tiếng Việt' (and VN flag).

            const isEnglishCurrently = this.currentLang === 'en';
            const nextLangCode = isEnglishCurrently ? 'vi' : 'en';
            const nextLangText = isEnglishCurrently ? 'Tiếng Việt' : 'English';

            // Flag codes for flagcdn: 'vn' for Vietnam, 'gb' for United Kingdom
            const flagCode = isEnglishCurrently ? 'vn' : 'gb';

            // Using slightly larger flag size for better visibility (w20 or 24x18)
            const flagUrl = `https://flagcdn.com/24x18/${flagCode}.png`;

            btn.innerHTML = `<img src="${flagUrl}" alt="${nextLangText}" class="inline-block mr-1.5 w-5 h-auto object-cover border border-gray-200"> <span class="uppercase">${nextLangText}</span>`;
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    I18n.init();

    const toggleBtn = document.getElementById('lang-toggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', (e) => {
            e.preventDefault();
            I18n.toggleLanguage();
        });
    }
});
