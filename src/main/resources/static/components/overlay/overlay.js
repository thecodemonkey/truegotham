const OVERLAY = {
  view: async () => {
    return await loadHTML('overlay');
  },
  init: async () => {
    $('#overlay').off().on('click', async (e) => {
        await OVERLAY.close();
    });


    return this;
  },
  show: async () => {
    $('#overlay').removeClass('hidden');
  },
  close: async () => {
    $('#overlay').addClass('hidden');
  }
}