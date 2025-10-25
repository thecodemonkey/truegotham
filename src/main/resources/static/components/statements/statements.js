let STATEMENTS = null;
let SEARCHRESULTS = null;
const LIST_FILTER = {search: '', categories: [], districts:[], page:0, size:8};


const STMTS_LIST = {
  view: async () => {
    return await loadHTML('statements');
  },
  init: async () => {

    $('#crimeTypeFilter').off().on('change', STMTS_LIST.onCrimeTypeFilterSelect);
    $('#pastCrimes').off().on('click', 'li', STMTS_LIST.onListItemClick);
    $('#crimeSearchInput').off().on('input', STMTS_LIST.onCrimeSearchInputChange);

    $('#list-back').off().on('click', STMTS_LIST.onListPageBack);
    $('#list-ahead').off().on('click', STMTS_LIST.onListPageAhead);

    await STMTS_LIST.loadInitialStatements();

    return this;
  },
  loadInitialStatements: async () => {
    const result = await API.search(LIST_FILTER);
    await STMTS_LIST.updateSearchResults(result);
  },
  onCrimeTypeFilterSelect: async (e) => {
    LIST_FILTER.categories = e.currentTarget.value ? [e.currentTarget.value] : [];
    await STMTS_LIST.callSearch(LIST_FILTER);
  },
  onCrimeSearchInputChange: UTILS.debounce( async (e) => {
    LIST_FILTER.search = e.currentTarget.value;

    await STMTS_LIST.callSearch(LIST_FILTER);
  }, 1200),
  updateSearchResults: async (result) => {
    SEARCHRESULTS = result;

    const from = result.empty? 0 : result.pageable.offset + 1;
    const to = result.empty? 0 : result.pageable.offset + result.numberOfElements;

    $('.search-result .active.from').text(from);
    $('.search-result .active.to').text(to);
    $('.search-result .total').text(result.totalElements);


    $('#list-back').attr("disabled", result.first || result.empty);
    $('#list-ahead').attr("disabled", result.last || result.empty);

    if (result.empty) {
      $('#emptyList').show();
      $('#pastCrimes').hide();
    } else {
      $('#emptyList').hide()
      $('#pastCrimes').show();
    }

    await STMTS_LIST.updateStatementsList(result.content);
  },
  updateStatementsList: async (statements) => {
    STATEMENTS = statements;

    const html = `${STATEMENTS.map(d => `
      <li data-id="${d.id}">
        <span class="time">${UTILS.formatDate(d.unixts)}</span>
        <span class="title">${d.title.replace('POL-DO:', '').trim()}</span>
      </li>
    `).join('')}`;

    const $crimesList = $('#pastCrimes');

    $crimesList.empty().append(html);
  },
  onListItemClick: async (e) => {
      const $e = $(e.currentTarget);
      $e.parent().find('li').removeClass('active');
      $e.addClass('active');
      await DASHBOARD.showCrimeDetails($e.data('id'));
  },
  onListPageBack: async (e) => {
      e.preventDefault();
      e.stopPropagation();

    if ($(e.currentTarget).attr("disabled")) return;

      LIST_FILTER.page -= 1;
      if (LIST_FILTER.page < 0) LIST_FILTER.page = 0;

      await STMTS_LIST.callSearch(LIST_FILTER);
  },
  onListPageAhead: async (e) => {
    e.preventDefault();
    e.stopPropagation();

    if ($(e.currentTarget).attr("disabled")) return;

    LIST_FILTER.page += 1;

    await STMTS_LIST.callSearch(LIST_FILTER);
  },
  callSearch: async (filter) => {
    const result = await API.search(filter);
    await STMTS_LIST.updateSearchResults(result);
  },


}