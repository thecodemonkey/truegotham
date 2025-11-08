const UTILS = {

  formatDate: (unixTimestamp) => {
    const date = new Date(unixTimestamp * 1000);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}.${month}.${year} – ${hours}:${minutes}`;
  },

  normalizeToMaxPercent: (arr) => {
    const max = Math.max(...arr);
    return max > 0 ? arr.map(v => (v / max) * 100) : arr;
  },

  debounce: (func, wait) => {
    let timeout;
    return function(...args) {
      const later = () => {
        clearTimeout(timeout);
        func.apply(this, args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  },

  delay: async (ms) => {
    return new Promise(resolve => setTimeout(resolve, ms));
  },

  randomPointInsidePolygon: (layer) => {
    let geojson;

    if (layer.toGeoJSON) {
      geojson = layer.toGeoJSON();
    } else {
      geojson = layer;
    }

    const bbox = turf.bbox(geojson);

    let point;
    do {
      point = turf.randomPoint(1, { bbox }).features[0];
    } while (!turf.booleanPointInPolygon(point, geojson));

    const [lng, lat] = point.geometry.coordinates;
    return [lat, lng];
  }
}