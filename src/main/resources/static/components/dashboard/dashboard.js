
const DASHBOARD = {
  view: async () => {
    const v = await loadHTML('dashboard');

    return v.replace('[STATEMETS_LIST]', await STMTS_LIST.view())
            .replace('[BREADCRUMB]', await BREADCRUMB.view())
            .replace('[NAVIGATION]', await NAVIGATION.view())
            .replace('[STATEMET_DETAILS]', await STATEMENT_DETAILS.view())
            .replace('[CASE_DETAILS]', await CASE_DETAILS.view())
            .replace('[DISTRICT_DESCRIPTION]', await DISTRICTS.view());

  },
  init: async () => {

    $('#aboutItem').off().on('click', async () => {
      await OVERLAY.show();
    });

    return this;
  },
  show: async () => {


    const cards = $('.dashboard-card').get();

    for(const c of cards) {
      await delay(100);
      const $e = $(c);
      if ($e.hasClass('hidden')){
        $e.removeClass('hidden');
      }
    }

    await DASHBOARD.initCharts();


  },
  cards: {
    topright: () => $('#typesCard'),
    bottomright: () => $('#districtsCard'),
    left: () =>  $('#crimesListCard')
  },
  initCharts: async () => {
    await CHARTS.initCrimesChart();
    //await CHARTS.initCrimeTypesChart();
    await CHARTS.initCrimesTimelineChart();
    await CHARTS.initCrimeDetailsTimelineChart();
    await DISTRICTS.initDistrictTable();
    await STMTS_LIST.initLatestStatements();

    $('.dashboard-container .close-info').off().on('click', DASHBOARD.closeInfo);
    $('.open-info').off().on('click', DASHBOARD.closeInfo);

  },
  closeInfo: async(e) =>  {
    const $e = $(e.currentTarget).closest('.flip-container');

    if ($e.hasClass('info')) {
      $e.removeClass('info');
    } else {
      $e.addClass('info');
    }
  },
  slideTitle: async () => {
    $('.dashboard-header h1').addClass('details');
  }
}