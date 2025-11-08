
const BREADCRUMB = {
  view: async () => {
    return await loadHTML('breadcrumb');
  },
  init: async () => {
    BREADCRUMB.render();
    $('#breadcrumb').off().on('click', '.subactive', BREADCRUMB.onClick);

    return this;
  },
  show: async () => {
    $('#breadcrumb').removeClass('hidden');
  },
  onClick: async (e) => {
    let $e = $(e.currentTarget);
    let isRoot = $e.text().trim() === 'root';

    if (isRoot) {
      await delay(300);
      // await MAP.closeDistrictMode();
      await MAP.restoreAllDistricts();
      await MAP.flyOUT();

      DISTRICTS.unselectDistrict();
    }

    await DETAILS.close(e);

    console.log('clicked element: ', e.currentTarget);
  },
  render: () => {
    let html = `
        <span class="item subactive">root</span>
        <span class="separator">&RightAngleBracket;</span>
        
        <span class="item dist collapsed">district</span>
        <span class="separator dist collapsed">&RightAngleBracket;</span>
        <span class="item dist subactive collapsed itm"></span>
        <span class="separator dist collapsed last">&RightAngleBracket;</span>
        
        <span class="item stmt collapsed">statement</span>
        <span class="separator stmt collapsed">&RightAngleBracket;</span>
        <span class="item active stmt collapsed itm"></span>                
    `;

    $('#breadcrumb').html(html);
  },
  update: async (statement, district) => {
    let $distAll = $('#breadcrumb .dist');
    let $dist = $('#breadcrumb .dist.itm');
    let distLabel = $dist.text().trim();
    let keepDist = distLabel === district;

    let $stmtAll = $('#breadcrumb .stmt');
    let $stmt = $('#breadcrumb .stmt.itm');

    if (district) {
      //if($dist.text().trim().length > 0){
      if (!keepDist) {

        if (distLabel) {
          $distAll.addClass('collapsed');
          await delay(300);
        }

        $dist.text(district);
        await delay(100);
      }

      $distAll.removeClass('collapsed');

      if (!statement) {
        $dist.removeClass('subactive');
        $dist.addClass('active');
      } else {
        $dist.removeClass('active');
        $dist.addClass('subactive');
      }

      await delay(200);
    } else {
      if(distLabel){
        $distAll.addClass('collapsed');
        await delay(500);
        $dist.text('');
      }
    }

    if (statement) {
      if($stmt.text().trim().length > 0){
        $stmtAll.addClass('collapsed');
        await delay(500);
      }

      $stmt.text(statement);
      await delay(100);
      $stmtAll.removeClass('collapsed');

    } else {
      if($stmt.text().trim().length > 0){
        $stmtAll.addClass('collapsed');
        await delay(500);
        $stmt.text('');
      }

      $('#breadcrumb .separator.dist.last').addClass('collapsed');
    }
  }
}