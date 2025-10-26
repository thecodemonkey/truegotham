

const DISTRICTS = {
  view: async () => {
    return '';
  },
  init: async () => {
    return this;
  },
  show: async () => {

  },

  showDetails: async (districtName) => {
    let district = await API.district('Dortmund', districtName);

    await DISTRICTS.showImage(district)
    await delay(100);
    await DISTRICTS.showDescription(district);
  },
  hideDetails: async () => {
    await DISTRICTS.hideImage();
    await delay(100);
    await DISTRICTS.hideDescription();
  },
  showImage: async (district) => {
    let districtImageURL = `/api/districts/${district.id}/image` //await API.districtImage(district.id);
    $('#typesCard .desc-img').attr('src', districtImageURL);

    let $card = DASHBOARD.cards.topright();
    await DASHBOARD.flipDetails($card);
  },
  hideImage: async () => {
    let $card = DASHBOARD.cards.topright();
    await DASHBOARD.unflipDetails($card);
  },
  showDescription: async (district) => {

    let $distDesc = $('#districtDescription');
    $distDesc.addClass('active');
    $distDesc.parent().find('.metric').css('display', 'none');
    $('#suspiciousPersonProfile').css('display', 'none');

    $distDesc.find('.card-title').text(district.name);
    let desc = $distDesc.find('.district-description');
    desc.html('');

    let $card = DASHBOARD.cards.bottomright();
    await DASHBOARD.flipDetails($card);

    //let districtDetails = await API.district('Dortmund', district);
    desc.html(
        district.description.trim()
        .split('\n\n')
        .map( p => `<p>${p}</p>`).join('')
    );
  },
  hideDescription: async () => {
    let $card = DASHBOARD.cards.bottomright();
    await DASHBOARD.unflipDetails($card);

    await delay(300);

    let $distDesc = $('#districtDescription');
    $distDesc.removeClass('active');
    $distDesc.parent().find('.metric').css('display', 'block');
    $distDesc.find('.district-description').html('');
    $('#suspiciousPersonProfile').css('display', 'table');
  }
}