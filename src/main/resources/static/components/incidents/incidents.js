let SELECTED_INCIDENT_DETAIL = null;

const INCIDENTS = {
  view: async () => {
    return await loadHTML('incidents');
  },
  init: async () => {
    $('#districtsCard').off().on('click', '.next-profile-btn',
         async (e) => await INCIDENTS.onMenueItemClick(e)
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
  },
  update: async (cs) => {

    if (!await INCIDENTS.isVisible()){
      await INCIDENTS.show();
    }

    await INCIDENTS.updateProfileData(cs.offenderProfiles);
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
          l.coordinates = {lat: coords.lat, lon: coords.lng}
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
  updateProfileData: async (profiles) => {

    await DISTRICTS.showProfileImage(`/api/districts/${profiles[0].imageId}/image`)

    for(const person of profiles) {
      const pos = profiles.indexOf(person);

      let html = `
            <tr>
                <td colspan="2">
                ${profiles.map((p, i) =>
          `<span class="next-profile-btn ${(pos === i)? 'active': ''}" data-item="profile${i}">${i + 1}</span>`
      )
                .join(' ')
                }
                </td>
            </tr>
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
              <td data-trans="gender_${person.gender.toLowerCase()}">${TRANSLATION.translate(
          `gender_${person.gender}`)}</td>
            </tr>
              ${person.hair
          ? `<tr><td data-trans="profile_hair">Haare</td><td>${person.hair}</td></tr>`
          : ''}
            <tr>
              <td data-trans="profile_drugs_and_alcohol">Drogen/Alkohol Test</td>
              <td>${person.drugTest}/${person.alcoholTest}</td>
            </tr>
            ${person.look
          ? `<tr><td data-trans="profile_look">Aussehen</td><td>${person.look}</td></tr>`
          : ''}
            <tr>
              <td data-trans="profile_behaviour">Verhalten</td>
              <td>${person.summary}</td>
            </tr>            
    `;

      $('#profileData' + pos).html(html);
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
    SELECTED_INCIDENT_DETAIL = $e.data('item')

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

    return `${i+1}'er Verdächtiger`;
  }
}