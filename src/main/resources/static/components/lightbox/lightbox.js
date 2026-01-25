
const LIGHTBOX = {
    view: async () => {
        return await loadHTML('lightbox');
    },

    init: async () => {
        const $overlay = $('#lightbox-overlay');

        // Close events
        $overlay.find('.close-btn').off().on('click', LIGHTBOX.hide);

        $overlay.off('click').on('click', (e) => {
            if (e.target.id === 'lightbox-overlay') {
                LIGHTBOX.hide();
            }
        });

        $(document).on('keydown', (e) => {
            if (e.key === "Escape" && !$overlay.hasClass('hidden')) {
                LIGHTBOX.hide();
            }
        });

        // Global Listener for .desc-img
        $('body').off('click', '.desc-img').on('click', '.desc-img', function (e) {
            e.preventDefault();
            e.stopPropagation();

            const $el = $(this);
            const src = $el.attr('src');

            // Versuche Beschreibung zu ermitteln
            // 1. data-description Attribut
            // 2. alt Attribut
            // 3. Wenn Bild in einer Card ist, suchen wir vllt nach Text in der Nähe?
            //    Für jetzt: Standard auf alt/title.

            let desc = $el.data('description') || $el.attr('alt') || "";
            let title = $el.data('title') || "";

            // Fallback: Wenn das Bild ein Profilbild ist (suspicious person), 
            // könnten wir versuchen, Text aus dem Profil-Container zu holen, falls vorhanden.
            // Der User wünscht "Motiv-Beschreibung". Oft ist das `alt` Tag dafür gut geeignet.

            LIGHTBOX.show(src, title, desc);
        });
    },

    show: (src, title, text) => {
        const $img = $('#lightbox-img');
        const $title = $('#lightbox-title');
        const $desc = $('#lightbox-desc');

        $img.attr('src', src);
        $title.text(title);
        $desc.text(text);

        $('#lightbox-overlay').removeClass('hidden');
        $('body').css('overflow', 'hidden');
    },

    hide: () => {
        $('#lightbox-overlay').addClass('hidden');
        setTimeout(() => {
            $('#lightbox-img').attr('src', '');
        }, 300); // Wait for transition
        $('body').css('overflow', '');
    }
};
