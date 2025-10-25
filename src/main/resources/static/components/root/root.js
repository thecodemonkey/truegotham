let INSIGHTS = null;

async function init(){
  console.log('loaded...');



  $('root').append(await loadHTML('root'))
           .append(await DASHBOARD.view())
           .append(await ERRORS.view());

  await ERRORS.init();
  await MAP.init();
  await DASHBOARD.init();

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

  await MAP.show();
  //await MAP.colorDistrict('Brackel', '#ff0000');

  await DASHBOARD.show();
}

async function loadHTML(name) {
  try {
    const url = `/components/${name}/${name}.html`;
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