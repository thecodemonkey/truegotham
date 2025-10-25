let CURRENT_STATEMENT = null;
let CURRENT_CASE = null;
let DETAILS_MODE = false;

const DASHBOARD = {
  view: async () => {
    const v = await loadHTML('dashboard');

    return v.replace('[STATEMETS_LIST]', await STMTS_LIST.view());
  },
  init: async () => {
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
  },
  initCharts: async () => {
    await CHARTS.initCrimesChart();
    //await CHARTS.initCrimeTypesChart();
    await CHARTS.initCrimesTimelineChart();
    await CHARTS.initCrimeDetailsTimelineChart();
    await DASHBOARD.initDistrictTable();
    await DASHBOARD.initLatestStatements();



    $('.dashboard-container .close-info').off().on('click', DASHBOARD.closeInfo);
    $('.open-info').off().on('click', DASHBOARD.closeInfo);

  },
  initDistrictTable: async () => {
    const total = INSIGHTS.districts.reduce( (a,b) => a + b.data, 0);
    const districts = INSIGHTS.districts.map( d =>
        ({
          label: d.label,
          data: d.data,
          percentage: ((d.data / total) * 100).toFixed(1)
        }));

    const html = `
      <tr><th>district</th><th class="number"></th></tr>
        ${districts.map(d => `<tr>
            <td>${d.label}</td>
            <td class="number">${d.percentage}%</td>
        </tr>`)}`;

    $('#dangerousDistricts').empty().append(html);


    const alldistricts = await API.districts();
    const t = alldistricts.reduce( (a,b) => a + b.data, 0);
    const allpercentages = UTILS.normalizeToMaxPercent(alldistricts.map(d => d.data));

    await MAP.flyIN([lat, lon], 12);

    await UTILS.delay(500);

    for(const d of alldistricts) {
      const i = alldistricts.indexOf(d);
      await UTILS.delay(30);
      let p = allpercentages[i];
      MAP.updateDistrict(d.label, p, i);
    }
  },
  initLatestStatements: async () => {
    await STMTS_LIST.updateStatementsList(STATEMENTS);

    $('#closeDetails').off().on('click', async (e) => {
      DETAILS_MODE = false;

      $('#crimesListCard').removeClass('top');
      $('.dashboard-header h1').removeClass('details');

      await MAP.closeCrimeDetails();

      await delay(200);
      $('#pastCrimes li').removeClass('active');
      $('.dashboard-card').removeClass('details');
    });
  },
  showCrimeDetails: async (id) => {
    DETAILS_MODE = true;

    $('.dashboard-card').removeClass('info');
    $('.card-details').removeClass('on');

    CURRENT_STATEMENT = await API.statement(id);
    CURRENT_CASE = await API.case('08badf0e-4e50-4bcb-999b-cdfaa2777e7e'); //CURRENT_STATEMENT.caseId

    await DASHBOARD.renderStatementDetails(CURRENT_STATEMENT);
    await DASHBOARD.renderCaseDetails(CURRENT_CASE);

    await delay(100);

    $('#typesCard').addClass('details');
    await delay(100);
    $('#districtsCard').addClass('details');
    await delay(100);
    $('#totalCard').addClass('details');
    //$('#totalCard').addClass('hidden');

    await delay(100);
    $('#crimesListCard').addClass('details');
    $('.dashboard-header h1').addClass('details');

    await delay(500);
    const cdetails = $('.card-details').get();
    for(const d of cdetails) {
      await delay(200);
      $(d).addClass('on');
    }

    $('#crimesListCard').addClass('top');

    await MAP.showCrimeDetails(CURRENT_STATEMENT.location, 'Dortmund Kampstraße');
  },
  closeDetails: async ($e) => {
    const isActive = $e.hasClass('active');

    $e.parent().find('li').removeClass('active');

    if (!isActive){

      $e.addClass('active');
      await DASHBOARD.showCrimeDetails($e.data('id')); //'f47ac10b-58cc-4372-a567-0e02b2c3d479'

    } else {
      DETAILS_MODE = false;
      await MAP.restoreAllDistricts();

      $('.dashboard-card').removeClass('info');
      $('.dashboard-card').removeClass('details');
      $('.dashboard-header h1').removeClass('details');
      await MAP.flyOUT();
    }
  },
  closeInfo: async(e) =>  {
    const $e = $(e.currentTarget).closest('.flip-container');

    if ($e.hasClass('info')) {
      $e.removeClass('info');
    } else {
      $e.addClass('info');
    }
  },
  renderStatementDetails: async (statement) => {
    $('#statementTime').text(UTILS.formatDate(statement.unixts));
    $('#statementTitle').text(statement.title);
    $('#statementURL').attr('href', statement.url);

    $('#statementContent').html(`
        <p>
          ${statement.content.replaceAll("\n\n", "</p><p>")}
        </p>
      `.trim()
    );
  },
  renderCaseDetails: async (cs) => {

    const person = cs.suspicious[0];

    let html = `
            <tr>
              <td data-trans="profile_age">Alter</td>
              <td>${person.age}</td>
            </tr>
            <tr>
              <td data-trans="profile_location">Wohnort</td>
              <td>${person.location}</td>
            </tr>
            <tr>
              <td data-trans="profile_gender">Geschlecht</td>
              <td data-trans="gender_${person.gender.toLowerCase()}">${TRANSLATION.translate(`gender_${person.gender}`)}</td>
            </tr>
            <tr>
              <td data-trans="profile_hair">Haare</td>
              <td>${person.hair}</td>
            </tr>
            <tr>
              <td data-trans="profile_behaviour">Verhalten</td>
              <td>${person.behaviour}</td>
            </tr>
            <tr>
              <td data-trans="profile_drugs_and_alcohol">Drogen/Alkohol Test</td>
              <td>${person.drugTest}/${person.alcoholTest}</td>
            </tr>
            <tr>
              <td colspan="2"><h3 data-trans="title_tools_and_evidence">Tatwerkzeuge/Beweismittel</h3></td>
            </tr>
            <tr>
              <td colspan="2">
                <ul class="table-ul">
                  ${cs.tools.map(t => `<li>${t}</li>`).join('')}
                  ${cs.evidence.map(t => `<li>${t}</li>`).join('')}
                </ul>
              </td>
            </tr>
            <tr>
              <td colspan="2"><h3 data-trans="title_violations">Tatbestand</h3></td>
            </tr>
            <tr>
              <td colspan="2">
                <ul class="table-ul">
                  ${cs.violations.map(t => `<li>${t.text} (${t.paragraph})</li>`).join('')}
                </ul>
              </td>
            </tr>
    `;

    $('#suspiciousPersonProfile').html(html);

    await TRANSLATION.resolve();
  },

  // card interactions
  showDistrictImage: async (district) => {
    let $card = DASHBOARD.cards.topright();
    await DASHBOARD.flipDetails($card);
  },
  hideDistrictImage: async () => {
    let $card = DASHBOARD.cards.topright();
    await DASHBOARD.unflipDetails($card);
  },
  showDistrictDescription: async (district) => {

    let $distDesc = $('#districtDescription');
    $distDesc.addClass('active');
    $distDesc.parent().find('.metric').css('display', 'none');
    $('#suspiciousPersonProfile').css('display', 'none');



    $distDesc.find('.card-title').text(district);
    let desc = $distDesc.find('.district-description');
    desc.html('');


    let $card = DASHBOARD.cards.bottomright();
    await DASHBOARD.flipDetails($card);

    let districtDetails = await API.district('Dortmund', district);
    desc.html(
        districtDetails.description.trim()
          .split('\n\n')
        .map( p => `<p>${p}</p>`).join('')
    );

  },
  hideDistrictDescription: async () => {
    let $card = DASHBOARD.cards.bottomright();
    await DASHBOARD.unflipDetails($card);

    await delay(300);

    let $distDesc = $('#districtDescription');
    $distDesc.removeClass('active');
    $distDesc.parent().find('.metric').css('display', 'block');
    $distDesc.find('.district-description').html('');
    $('#suspiciousPersonProfile').css('display', 'table');
  },
  flipDetails: async ($card) => {
    $card.addClass('details');
    $card.removeClass('info');
  },
  unflipDetails: async ($card) => {
    $card.removeClass('details');
    $card.removeClass('info');
  }
}