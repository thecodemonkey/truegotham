const OVERLAY = {
  view: async () => {
    return await loadHTML('overlay');
  },
  init: async () => {
    $('#overlay .close-overlay-btn').off().on('click', async (e) => {
        await OVERLAY.close();
    });

    $('#overlay-navi li').off().on('click', OVERLAY.navigate);


    return this;
  },
  show: async () => {
    const content = await TRANSLATION.loadAbout();
    await OVERLAY.updateContent(content);

    $('#overlay').removeClass('hidden');
  },
  close: async () => {
    $('#overlay').addClass('hidden');
  },
  navigate: async (e) => {
     const $item = $(e.currentTarget);
     const i = $item.data('item');

     $item.parent().find('li').removeClass('active');
     $item.addClass('active');

    const el = $(`#overlay-content h2[data-item="${i}"]`);
    $('#overlay-content').animate({ scrollTop: el.offset().top }, 600);
  },
  updateContent: async (content) => {
    console.log('about content: ', content)
   // console.log('about_cnt', about_cnt)
  }
}