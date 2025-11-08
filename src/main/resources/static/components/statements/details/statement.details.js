
const STATEMENT_DETAILS = {
  view: async () => {
    return await loadComponentHTML(`/components/statements/details/statement.details.html`);
  },
  init: async () => {

    $('.details-tab span').off().on('click', STATEMENT_DETAILS.onTabClick)

    return this;
  },
  onTabClick: async (e) => {
    const $e = $(e.currentTarget);

    $('.details-tab span').removeClass('active');
    $e.addClass('active');

    const isSummary = $e.data('item') === 'summary';

    if (isSummary) {
      $('#statementSummaryContent').removeClass('out')
                                   .addClass('in');

      $('#statementContent').removeClass('in')
    } else {
      $('#statementContent').addClass('in');
      $('#statementSummaryContent').removeClass('in')
                                   .addClass('out')
    }


  },
  update: async (statement) => {
    let ttl = statement.title?.replace(/pol-do:/i, '').trim();
    $('#statementTime').text(UTILS.formatDate(statement.unixts));
    $('#statementTitle').text(ttl);

    $('#statementURL').attr('href', statement.url);

    if (statement.imageId){
      $('#crimesListCard .details-head img').attr('src', `/api/districts/${statement.imageId}/image`);
    } else {
      $('#crimesListCard .details-head img').attr('src', `/img/sample5.jpeg`);
    }


    await BREADCRUMB.update(ttl, SELECTED_DISTRICT);

    $('#statementContent').html(`
        <p>
          ${statement.content.replaceAll("\n\n", "</p><p>")}
        </p>
      `.trim()
    );

    if (statement.summary) {
      $('#statementSummaryContent').html(`
        <p>
          ${statement.summary.replaceAll("\n\n", "</p><p>")}
        </p>
      `.trim()
      );
    }




    $('#crimesListCard').addClass('top');
  },
}