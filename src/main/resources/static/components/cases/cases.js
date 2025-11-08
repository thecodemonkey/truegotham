let SELECTED_CASE_DETAIL = null;

const CASE_DETAILS = {
  view: async () => {
    return await loadHTML('cases');
  },
  init: async () => {
    return this;
  },
  show: async () => {
    $('#districtsCard .horizontal-slider').removeClass('hidden');
  },
  closeIfVisible: async () => {
    if (await CASE_DETAILS.isVisible()) {
      await DETAILS.unflipRightDetails();
      await delay(300);
    }

    await CASE_DETAILS.close();
  },
  isVisible: async () => {
    return DETAILS.areRightDetailsFlipped();
    // !$('#districtsCard .horizontal-slider').hasClass('hidden');
  },
  close: async () => {
    $('#districtsCard .horizontal-slider').addClass('hidden');
  },
  update: async (cs) => {

    if (!await CASE_DETAILS.isVisible()){
      await CASE_DETAILS.show();
    }

    await CASE_DETAILS.updateProfileData(cs.suspicious);
    await CASE_DETAILS.updateEvidence(cs);
    await CASE_DETAILS.updateOffenses(cs);
    await CASE_DETAILS.updateMotive(cs);

    await TRANSLATION.resolve();
    await delay(100);

    await MAP.showCrimeDistrict(CURRENT_STATEMENT.district);
    //await MAP.showCrimeDetails(CURRENT_STATEMENT.location, 'Dortmund Kampstraße');
  },
  updateProfileData: async (profiles) => {
    const person = profiles[0];

    await DISTRICTS.showProfileImage('/img/mug.shot.front2.jpeg')

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
    `;

    $('#profileData').html(html);
  },
  updateEvidence: async (cs) => {
    let data =
        cs.tools.map(t => `
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
        cs.violations.map(t => `
           <tr>
            <td>${t.text}</td>
            <td><span class="light">${t.paragraph || ''}</span></td>
          </tr>
        `).join('').trim();

    $('#offensesData').html(data);
  },
  updateMotive: async (cs) => {
    const data = `
    <tr>
      <td colSpan="2">
        Der Täter hatte ein Motiv, aber wir wissen nicht genau, welches.
      </td>
    </tr>`.trim();

    $('#motiveData').html(data);
  },
  onMenueItemClick: async (e) => {
    const $e = $(e.currentTarget);
    SELECTED_CASE_DETAIL = $e.data('item')

    await CASE_DETAILS.updateTitle(SELECTED_CASE_DETAIL);

    const currentActive = $(`#districtsCard .horizontal-slider .sliding-item.in`);
    const nextActive = $(`#case_${SELECTED_CASE_DETAIL}`);

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
    $e.text(title);

    $e.removeClass('hide');
  }
}