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
  }
}