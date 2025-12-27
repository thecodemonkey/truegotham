const OVERLAY = {
  view: async () => {
    return await loadHTML('overlay');
  },
  init: async () => {
    $('#overlay .close-overlay-btn').off().on('click', async (e) => {
      await OVERLAY.close();
    });

    $('#overlay-navi ul').off().on('click', 'li', OVERLAY.navigate);

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

    var html = ``;
    $('#overlay-navi ul').empty();


    for (const k in content) {
      $('#overlay-navi ul').append(`<li data-item="${k.toLowerCase()}">${k}</li>`);


      html += `
        <h2 data-item="${k.toLowerCase()}">${k}</h2>
        <div class="content-multi">
          <p>${OVERLAY.renderContent(content[k].left)}</p> 
          <p>${OVERLAY.renderContent(content[k].right)}</p>
        </div>
      `;
    }

    $('#overlay-navi ul').find('li').first().addClass('active');
    $('#overlay-content').html(html);

  },
  renderContent: (content) => {
    if (OVERLAY.isImage(content)) {
      return `<img src="${content}" alt="image" />`;
    }

    return OVERLAY.getParagraphs(content);
  },
  isImage: (str) => {
    return str.startsWith('/img/');
  },
  getParagraphs: (str) => {
    return str.trim().split('\n').join('<br/>').trim();
  }
}