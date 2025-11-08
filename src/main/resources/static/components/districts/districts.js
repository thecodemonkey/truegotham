let SELECTED_DISTRICT = null;

const DISTRICTS = {
  view: async () => {
    return await loadHTML('districts');
  },
  init: async () => {
    $('#districtDescription .details-tab2 span').off().on('click', DISTRICTS.onTabClick);
    return this;
  },
  show: async () => {

  },
  closeIfVisible: async () => {
    if (await DISTRICTS.isVisible()) {
      await DETAILS.unflipRightDetails();
      await delay(300);
      await DISTRICTS.hideDescription(false);
    }
  },
  isVisible: async () => {
    return await DETAILS.areRightDetailsFlipped();
  },
  showDetails: async (districtName) => {
    if (!districtName) return;

    await DETAILS.unflipRightDetails();

    await DISTRICTS.cleanDistrictAreas()
//TODO: spinner shoult be part of flipped card...
/*    await SPINNER.showImageSpinner();
    await SPINNER.showRightSpinner();*/

    let district = await API.district('Dortmund', districtName);
    await delay(500); // simulates delay of the server load..

    await DISTRICTS.showImage(district)
    await DISTRICTS.showDescription(district);

    await delay(100);

    //TODO: spinner shoult be part of flipped card...
/*    await SPINNER.hideImageSpinner();
    await SPINNER.hideRightSpinner();*/

    await DETAILS.flipRightDetails();

    await BREADCRUMB.update(null, districtName);
  },
  hideDetails: async () => {
    await DISTRICTS.hideImage();
    await delay(100);
    await DISTRICTS.hideDescription();
  },
  showImage: async (district) => {

    $('#typesCard .desc-img').attr('src',
        `/api/districts/${district.imageId}/image`);
  },
  showProfileImage: async (url) => {
    $('#typesCard .desc-img').attr('src',url);
  },
  hideImage: async () => {
    let $card = DASHBOARD.cards.topright();
    await DETAILS.unflipDetails($card);
  },
  showDescription: async (district) => {
    let $distDesc = $('#districtDescription');
    $distDesc.find('.card-title').text(district.name);
    let desc = $distDesc.find('.district-description');


    desc.html(
        district.description.trim()
        .split('\n\n')
        .map( p => `<p>${p}</p>`).join('')
    );

    await DISTRICTS.updateFacts({});
  },
  hideDescription: async (flip = true) => {
    if(flip) {
      let $card = DASHBOARD.cards.bottomright();
      await DETAILS.unflipDetails($card);
      await delay(300);
    }

    let $distDesc = $('#districtDescription');
    $distDesc.removeClass('active');
    $distDesc.parent().find('.metric').css('display', 'block');
    $distDesc.find('.district-description').html('');
    $('#suspiciousPersonProfile').css('display', 'table');
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
  cleanDistrictAreas: async () => {
    let $distDesc = $('#districtDescription');
    $distDesc.addClass('active');
    $distDesc.parent().find('.metric').css('display', 'none');
    $('#suspiciousPersonProfile').css('display', 'none');
    let desc = $distDesc.find('.district-description');
    desc.html('');
    $distDesc.find('.card-title').text('');


    // transparent pixel
    $('#typesCard .desc-img').attr('src',
        `data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=`);
  },
  onTabClick: async (e) => {
    const $e = $(e.currentTarget);

    $e.parent().find('span').removeClass('active');
    $e.addClass('active');

    const isDescriptioin = $e.data('item') === 'description';

    if (isDescriptioin) {
      $('#districtDesc').removeClass('out')
      .addClass('in');

      $('#districtFacts').removeClass('in')
    } else {
      $('#districtFacts').addClass('in');
      $('#districtDesc').removeClass('in')
      .addClass('out')
    }
  },
  updateFacts: async (facts) => {

    facts = {
      population: 20000,
      location: 'Wohn- und Mischgebiet mit traditionellen Arbeitersiedlungen sowie neueren Wohn- und Gewerbeparks.',
      social_structure: 'mittleres bis eher niedrigeres Einkommensniveau',
      sense_of_security: 'mittleres wahrgenommenes Sicherheitsniveau',
      migration_background: '30%',
      age_structure: 'vielfältig, älteren Menschen über Familien bis hin zu Studierenden.'
    }

    let html = `
            <tr>
              <td>Einwohnerzahl</td>
              <td>${facts.population}</td>
            </tr>
            <tr>
              <td>Wohnort</td>
              <td>${facts.location}</td>
            </tr>
            <tr>
              <td>Altersstruktur</td>
              <td>${facts.age_structure}</td>
            </tr>            
            <tr>
              <td>Soziale Strukture</td>
              <td>${facts.social_structure}</td>
            </tr>
            <tr>
              <td>Anteil mit Migrationshintergrund</td>
              <td>${facts.migration_background}</td>
            </tr>
            <tr>
              <td>Sicherheitslage</td>
              <td>${facts.sense_of_security}</td>
            </tr>
    `;

    $('#districtFacts table').html(html);
  },

  unselectDistrict: () => {
    SELECTED_DISTRICT = null;
    $('.titleListDistrict').text(``);
  }
}