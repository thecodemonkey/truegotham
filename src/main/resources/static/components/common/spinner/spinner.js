const SPINNER = {
  view: async () => {
    return '<span class="spinner"></span>';
  },
  init: async () => {
    return this;
  },
  showImageSpinner: async () => {
    $('#typesCard .spinner').addClass('on');
  },
  hideImageSpinner: async () => {
    $('#typesCard .spinner').removeClass('on');
  },
  showRightSpinner: async () => {
    $('#districtsCard .spinner').addClass('on');
  },
  hideRightSpinner: async () => {
    $('#districtsCard .spinner').removeClass('on');
  },
}