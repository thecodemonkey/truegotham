let INSIGHTS = null;
let SETTINGS = null;

async function init(){
  console.log('loaded...');

  SETTINGS = await API.settings();

  $('root').append(await loadHTML('root'))
           .append(await DASHBOARD.view())
           .append(await ERRORS.view())
           .append(await OVERLAY.view());

  await OVERLAY.init();
  await BREADCRUMB.init();
  await ERRORS.init();
  await MAP.init();
  await DASHBOARD.init();
  await NAVIGATION.init();
  await DISTRICTS.init();

  await STATEMENT_DETAILS.init();

  await STMTS_LIST.init();
  await TRANSLATION.init();


  $('#startBtn').on('click', async (e) => {
    e.preventDefault();
    e.stopPropagation();
    $('.root-claim').addClass('off');
    $('.police-overlay').addClass('hidden');

    await start();
  });

  $('.cockpit-navi .lng').off().on('click', TRANSLATION.onLangSwitch);
}

async function start(){

  INSIGHTS = await API.insights();


  await delay(800);

  await BREADCRUMB.show();
  await MAP.show();
  await DASHBOARD.show();
}

async function loadHTML(name) {
  return await loadComponentHTML(`/components/${name}/${name}.html`)
}

async function loadComponentHTML(url) {
  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Fehler beim Laden von ${url}`);
    return  await response.text();

  } catch (err) {
    console.error(err);
  }
}

async function delay(ms){
  return new Promise((res, rej) => {
    setTimeout(async () => {
      res();
    }, ms);
  });

}

$( async () => {
  await init();
});