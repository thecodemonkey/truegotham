let CURRENT_STATEMENT = null;
let CURRENT_INCIDENT = null;
let DETAILS_MODE = false;

const DETAILS = {
  view: async () => {
    return '';
  },
  init: async () => {
    return this;
  },
  show: async (id) => {
    DETAILS_MODE = true;

    $('.dashboard-card').removeClass('info');
    //$('.card-details').removeClass('on');

    await DISTRICTS.closeIfVisible();

    CURRENT_STATEMENT = await API.statement(id);
    CURRENT_INCIDENT = await API.incident(id); //CURRENT_STATEMENT.caseId //'08badf0e-4e50-4bcb-999b-cdfaa2777e7e'

    await STATEMENT_DETAILS.update(CURRENT_STATEMENT);

    await INCIDENTS.update(CURRENT_INCIDENT);

    await DETAILS.flipRightDetails();
    await delay(50);
    await DETAILS.flipTotalChartDetails();
    await delay(50);
    await DETAILS.flipLeftStatementsDetails();

    await DASHBOARD.slideTitle();
    await delay(500);
    await NAVIGATION.show();

  },
  close: async (e) => {
    DETAILS_MODE = false;

    await NAVIGATION.close();

    $('#crimesListCard').removeClass('top');
    $('.dashboard-header h1').removeClass('details');

    await delay(200);
    $('#pastCrimes li').removeClass('active');

    if (!SELECTED_DISTRICT) {
      LIST_FILTER.districts = [];
      LIST_FILTER.page = 0
      await STMTS_LIST.callSearch(LIST_FILTER);
    }


    $('.titleTotalDistrict').text(``);
    if (INSIGHTS?.totalCrimes){
      await CHARTS.updateCrimesTimelineChart(INSIGHTS.totalCrimes.data, INSIGHTS.totalCrimes.labels);
    }
    await DETAILS.unflipTotalChartDetails();

    await DISTRICTS.closeIfVisible();
    await INCIDENTS.closeIfVisible();
    await STMTS_LIST.closeIfVisible();

    await MAP.restorePreviousDistrictView(SELECTED_DISTRICT);
    await DISTRICTS.showDetails(SELECTED_DISTRICT)

    if (!SELECTED_DISTRICT)
      await BREADCRUMB.update(null, SELECTED_DISTRICT);
  },

  isFlipped: async ($card) => {
    return $card.hasClass('details') || $card.hasClass('info');
  },
  flipDetails: async ($card) => {
    $card.addClass('details');
    $card.removeClass('info');
  },
  unflipDetails: async ($card) => {
    $card.removeClass('details');
    $card.removeClass('info');
  },
  areRightDetailsFlipped: async () => {
    return await DETAILS.isFlipped(DASHBOARD.cards.bottomright())
  },
  flipRightDetails: async() => {
    await DETAILS.flipDetails(DASHBOARD.cards.bottomright());
    await delay(100);
    await DETAILS.flipDetails(DASHBOARD.cards.topright());
  },
  unflipRightDetails: async() => {
    await DETAILS.unflipDetails(DASHBOARD.cards.bottomright());
    await delay(100);
    await DETAILS.unflipDetails(DASHBOARD.cards.topright());
    await delay(200)
  },

  areLeftStatementsDetailsFlipped: async () => {
    return await DETAILS.isFlipped(DASHBOARD.cards.left())
  },
  flipLeftStatementsDetails: async () => {
    $('#crimesListCard').addClass('details');
  },
  unflipLeftStatements: async () => {
    $('#crimesListCard').removeClass('details');
  },

  flipTotalChartDetails: async () => {
    $('#totalCard').addClass('details');
  },
  unflipTotalChartDetails: async () => {
    $('#totalCard').removeClass('details');
  },
}
