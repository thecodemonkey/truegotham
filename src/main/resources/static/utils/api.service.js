const API_BASE_URL = "/api";

const API = {
  insights: async () => {
    return await API.get("/insights");
  },
  insightsTotals: async (districts) => {
    let districtParams = (districts && districts.length > 0) ?
        '?' + districts.map(d => `districts=${encodeURIComponent(d)}`).join('&') : '';
     return await API.get(`/insights/total${districtParams}`);
  },
  districts: async (limit) => {
    return await API.get(`/insights/districts${(limit ? `?limit=${limit}` : '')}`);
  },
  district: async (city, name) => {
    return await API.get(`/districts/${encodeURIComponent(city)}/${encodeURIComponent(name)}`);
  },
  districtImage: async (id) => {
    return await API.get(`/districts/${id}/image`);
  },
  statementsLatest: async (limit) => {
    return await API.get(`/statements/latest?limit=${limit}`);
  },
  statements: async () => {
    return await API.get("/statements");
  },
  statement: async (id) => {
    return await API.get(`/statements/${id}`);
  },
  incident: async (id) => {
    return await API.get(`/incident/${id}`);
  },

  /**
   * 🔎 Filtered + paginated search for statements
   * @param {Object} params - search parameters
   * @param {string} [params.search] - text query
   * @param {string[]} [params.categories] - list of categories
   * @param {string[]} [params.districts] - list of districts
   * @param {number} [params.page=0] - page index
   * @param {number} [params.size=10] - page size
   */
  search: async ({ search, categories, districts, page = 0, size = 10 } = {}) => {
    let url = `${API_BASE_URL}/statements/search?page=${page}&size=${size}`;

    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (Array.isArray(categories)) categories.forEach(c => url += `&categories=${encodeURIComponent(c)}`);
    if (Array.isArray(districts)) districts.forEach(d => url += `&districts=${encodeURIComponent(d)}`);

    return await API.call(url.toString(), {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
  },

  // basic stuff for making API calls
  get: async (path, data) => {
    return await API.call(`${API_BASE_URL}${path}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
    })
  },
  post: async (path, data) => {
    return await API.call(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
  },
  call: async (url, options = {}) => {
    try {
      await ERRORS.hide();
      const response = await fetch(url, options);

      if (!response.ok) {
        const errorText = await response.text();
        const errorMsg = `HTTP ${response.status} - ${response.statusText}: ${errorText}`;
        throw new Error(errorMsg);
      }

      const contentType = response.headers.get("content-type");

      if (contentType && contentType.includes("application/json")) {
        return response.json();
      } else {
        return response.text();
      }

    } catch (e) {
      await ERRORS.show(e, "ERROR WHILE LOADING DATA");
      throw new Error(e);
    }
  }
}