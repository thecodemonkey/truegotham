let CURRENT_TRANSLATIONS = null;

const TRANSLATION = {
  init: async () => {
    let lng = TRANSLATION.getBrowserLang();
    if (!lng) lng = 'en';

    let e = $(`.lang .lng:contains(${lng})`).get();
    if (!e) e = $(".lang .lng:contains('en')").get();

    await TRANSLATION.onLangSwitch({
      currentTarget: e
    });

  },
  resolveTranslations: async (lang) => {
    CURRENT_TRANSLATIONS = await TRANSLATION.loadTranslations(lang);

      $('*[data-trans]').each(async (i, e) => {
        const key = $(e).data('trans')

        $(e).text(TRANSLATION.translate(key));
      });
  },
  resolve: async () => {
    $('*[data-trans]').each(async (i, e) => {
      const key = $(e).data('trans')

      $(e).text(TRANSLATION.translate(key));
    });
  },
  translate: (key) => {
    const k = key.toLowerCase();

    return CURRENT_TRANSLATIONS[k]?
        CURRENT_TRANSLATIONS[k] : `[${k.toUpperCase()}]`;
  },
  onLangSwitch: async (e) => {
    const $e = $(e.currentTarget);
    $e.parent().find('.lng').removeClass('active');

    const lng = $e.text();
    $e.addClass('active');
    await TRANSLATION.resolveTranslations(lng);
  },
  loadTranslations: async (lang) => {
    try {
      const url = `/translations/translations.${lang}.json`;
      const response = await fetch(url);
      if (!response.ok) throw new Error(`Fehler beim Laden von ${url}`);
      return  await response.json();

    } catch (err) {
      console.log('eroror', err)
      await ERRORS.show(err, "LOADING TRANSLATIONS");
    }
  },
  getBrowserLang: () => {
    const lang = navigator.language || navigator.userLanguage;
    return lang.substring(0, 2).toLowerCase();
  }
}