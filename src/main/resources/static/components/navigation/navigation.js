const SELECTED_NAVIGATION_ITEM = null;

const NAVIGATION = {
  view: async () => {
    return await loadHTML('navigation');
  },
  init: async () => {
    $('#navigation li').off().on('click', NAVIGATION.onClick)
    return this;
  },
  show: async () => {
    $('#navigation').removeClass('hidden');
  },
  close: async () => {
    $('#navigation').addClass('hidden');
  },
  onClick: async (e) => {
    $('#navigation li').removeClass('active');

    $(e.currentTarget).addClass('active');

    await CASE_DETAILS.onMenueItemClick(e);
  }
}