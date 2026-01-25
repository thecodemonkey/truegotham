let SELECTED_INCIDENT_DETAIL = null;

const INCIDENTS = {
  view: async () => {
    return await loadHTML('incidents');
  },
  init: async () => {
    $('#districtsCard').off().on('click', '.next-profile-btn',
      async (e) => {
        await INCIDENTS.onMenueItemClick(e)
      }
    );


    return this;
  },
  show: async () => {
    $('#districtsCard .horizontal-slider').removeClass('hidden');
  },
  closeIfVisible: async () => {
    if (await INCIDENTS.isVisible()) {
      await DETAILS.unflipRightDetails();
      await delay(300);
    }

    await INCIDENTS.close();
  },
  isVisible: async () => {
    return DETAILS.areRightDetailsFlipped();
    // !$('#districtsCard .horizontal-slider').hasClass('hidden');
  },
  close: async () => {
    $('#districtsCard .horizontal-slider').addClass('hidden');
    $('#profile_navi').addClass('hidden');
  },
  update: async (cs) => {

    if (!await INCIDENTS.isVisible()) {
      await INCIDENTS.show();
    }

    await INCIDENTS.updateProfileData(cs.offenderProfiles, cs);
    await INCIDENTS.updateEvidence(cs);
    await INCIDENTS.updateOffenses(cs);
    await INCIDENTS.updateMotive(cs);

    await TRANSLATION.resolve();
    await delay(100);



    cs.locations?.filter(l => !l.coordinates && !l.district)?.forEach(l => {
      const d = KNOWN_DISTRICTS.find(
        kd => l.address.toLowerCase().indexOf(kd) > -1);
      if (d) {
        l.district = d;

        const coords = MAP.getCoordsByDistrictName(d);
        if (coords) {
          l.coordinates = { lat: coords.lat, lon: coords.lng }
        }
      }
    });

    const locations = cs.locations ? cs.locations.filter(l => l.coordinates?.lat && l.coordinates?.lon) : [];

    if (locations.length > 0) {
      await MAP.updateIncidentHotspots(locations)
    } else {
      await MAP.showCrimeDistrict([CURRENT_STATEMENT.district], true);
    }

    await CHARTS.updateCrimeDetailsTimelineChart(locations);

    //await MAP.showCrimeDetails(CURRENT_STATEMENT.location, 'Dortmund Kampstraße');
  },
  updateProfileData: async (profiles, cs) => {

    const title = profiles.map(p => {
      if (!p.age) return '';
      const gender = TRANSLATION.translate(`gender_${p.gender}`);
      return `${p.age}, ${gender}`;
    }).filter(t => t.length > 0).join(" | ");

    await DISTRICTS.showProfileImage(`/api/districts/${profiles[0].imageId}/image`, cs.motive, title)

    await INCIDENTS.updateProfileNavi(profiles);

    const row = (field, value) => {
      if (!value || value.toString()
        .toLowerCase()
        .trim()
        .replace('null', '')
        .replace('0', '')
        .length < 1)
        return '';

      return `<tr>
        <td data-trans="profile_${field}"></td>
        <td>${value}</td>
      </tr>`
    }

    for (const person of profiles) {
      const pos = profiles.indexOf(person);

      let html = `
            ${row('age', person.age)}
            ${row('location', person.location)}
            <tr>
              <td data-trans="profile_gender">Geschlecht</td>
              <td data-trans="gender_${person.gender.toLowerCase()}">${TRANSLATION.translate(
        `gender_${person.gender}`)}</td>
            </tr>
            ${row('hair', person.hair)}
            ${row('drugs_and_alcohol', person.drugTest + '/' + person.alcoholTest)}
            ${row('look', person.look)}
            ${row('behaviour', person.summary)}
    `;

      $('#profileData' + pos).html(html);
    }
  },
  updateProfileNavi: async (profiles) => {
    const isMultiple = profiles.length > 1;
    const $nav = $('#profile_navi')

    if (isMultiple) {
      $nav.removeClass('hidden');

      const html = profiles.slice(0, 4)
        .map((p, i) =>
          `<span class="next-profile-btn ${i < 1 ? 'active' : ''}" data-item="profile${i}">${i + 1}</span>`
        )
        .join('\n');
      $nav.html(html);

    } else {
      $nav.addClass('hidden');
      $nav.html('');
    }
  },
  updateEvidence: async (cs) => {
    let data =
      cs.tools?.map(t => `
           <tr>
            <td colSpan="2">${t}</td>
          </tr>
        `).join('').trim();

    data +=
      cs.evidence.map(t => `
           <tr>
            <td colSpan="2">${t}</td>
          </tr>
        `).join('').trim();

    $('#evidenceData').html(data);
  },
  updateOffenses: async (cs) => {

    let data =
      cs?.offences?.map(t => `
           <tr>
            <td>${t.text}</td>
            <td><span class="light">${t.paragraph || ''}</span></td>
          </tr>
        `).join('').trim();

    $('#offensesData').html(data);
  },
  updateMotive: async (cs) => {
    const data = cs.motive ? `<p> 
          ${cs.motive?.split('\n\n').join('</p><p>')}
       </p>`.trim() : '';

    $('#motiveData').html(data);
  },
  onMenueItemClick: async (e) => {
    const $e = $(e.currentTarget);
    const $nav = $('#profile_navi');
    SELECTED_INCIDENT_DETAIL = $e.data('item')

    if (SELECTED_INCIDENT_DETAIL.startsWith('profile')) {
      $('.next-profile-btn[data-item^="profile"]').removeClass('active');
      $(`.next-profile-btn[data-item="${SELECTED_INCIDENT_DETAIL}"]`).addClass('active');

      if ($nav.hasClass('hidden')) $nav.removeClass('hidden');
    } else {
      $nav.addClass('hidden');
    }

    await INCIDENTS.updateTitle(SELECTED_INCIDENT_DETAIL);

    const currentActive = $(`#districtsCard .horizontal-slider .sliding-item.in`);
    const nextActive = $(`#case_${SELECTED_INCIDENT_DETAIL}`);

    if (nextActive.hasClass('out')) { // from left to right =>
      nextActive.removeClass('out')
        .addClass('in')
      currentActive.removeClass('in')
    } else {                                 //from right to left <=
      nextActive.addClass('in');
      currentActive.removeClass('in')
        .addClass('out');
    }

  },
  updateTitle: async (title) => {
    let $e = $('#case_title');

    $e.addClass('hide');

    await delay(300);

    if (title.startsWith('profile')) {
      $e.text(INCIDENTS.getProfileTitle(title));
    } else {
      $e.text(title);
    }



    $e.removeClass('hide');
  },
  getProfileTitle: (title) => {
    const i = parseInt(title.replace('profile', ''));

    return `${i + 1}'er Verdächtiger`;
  }
}