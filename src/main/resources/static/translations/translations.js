let CURRENT_TRANSLATIONS = null;
let CURRENT_LANGUAGE = '';

const TRANSLATION = {
  init: async () => {
    let lng = TRANSLATION.getBrowserLang();
    if (!lng) lng = 'en';

    CURRENT_LANGUAGE = lng;

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
    CURRENT_LANGUAGE = lng;
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
  },
  loadDynamicScript: async (path)  => {
    $('script[data-dyn]').remove();

    console.log('load script: ' + path)

    return new Promise((resolve, reject) => {
      $('<script>', { src: path, 'data-dyn': '' })
      .appendTo('head')
      .on('load', () => {
        console.log(path + ' geladen');
        resolve();
      })
      .on('error', (e) => {
          console.log('error on loading script: ', e);
          reject(new Error(path + ' konnte nicht geladen werden'))
      }
      );
    });
  },
  loadAbout: async () => {
    const module = await import(`/translations/about.${CURRENT_LANGUAGE}.js?ts='${Date.now()}`);
    //await TRANSLATION.loadDynamicScript(`/translations/about.${CURRENT_LANGUAGE}.js`);
    //await UTILS.delay(1000);
    return module.ABOUT_CNT;
  }
}