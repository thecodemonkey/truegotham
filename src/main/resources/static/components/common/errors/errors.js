const ERR_ELM = () => $('#error-panel');
const ERR_CLOSE_ELM = () => $('#error-panel .close-info');

const ERRORS = {
  view: async () => {
    return `<div id="error-panel">
            <h3>hello....</h3>
            <p>asdfkj alsdf öasjdf ös dfjaös dföa södf</p>
            <span class="close-info">x</span> 
    </div>`;
  },
  init : async () => {
    ERR_CLOSE_ELM().on('click', ERRORS.hide);
  },
  show: async (message, title) => {
    const $e = ERR_ELM()
    $e.addClass('on');
    $e.find('h3').text(title || '');
    $e.find('p').text(message);

  },
  hide: async (e) => {
    ERR_ELM().removeClass('on');
  }
}