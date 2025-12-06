let INSIGHTS = null;
let SETTINGS = null;
let STARTED = false;

async function init(){
  console.log('loaded...');
  $(window).on("resize", isValidScreenSize);

  if (!isValidScreenSize()) return;

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
  await INCIDENTS.init();

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

const minW = 1280;
const minH = 720;

function isValidScreenSize() {
  if ($(window).width() < minW || $(window).height() < minH) {
    if ($("#size-warning").length) return false;

    $("body").append(`
        <div id="size-warning">
          <div class="size-warning-message">
          <h1 class="glitch" data-text="True Gotham">True Gotham</h1>
          <br/>
          <p>
            Diese Stadt zeigt nicht jedem ihr wahres Gesicht. 
            Nur mit schwerem Gerät öffnest du ihre Tore. 
            Lass deine Spielzeugpistole fallen und komm mit einem ordentlichen Desktop 
            ab 1280×720px Auflösung zurück oder verstumme in der Finsternis!
          </p>
          </div>
        </div>
      `);
    collapseRootClaim()
    return false;
  } else {
    $("#size-warning").remove();
    collapseRootClaim(true)
    return true
  }
}

function collapseRootClaim(expand){

  if (STARTED) return;

  if (expand === true)
    $('.root-claim').removeClass('off');
  else
    $('.root-claim').addClass('off');
}

async function start(){

  STARTED = true;
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