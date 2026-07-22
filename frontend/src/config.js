// central runtime config for API & WS base URL
// Uses REACT_APP_API_URL (CRA) if set at build time, otherwise falls back to window.location.origin (useful for preview)
const API_BASE = (typeof process !== 'undefined' && process.env.REACT_APP_API_URL)
  || (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8080');

const WS_BASE = (typeof process !== 'undefined' && process.env.REACT_APP_WS_URL)
  || (() => {
    try {
      const u = new URL(API_BASE);
      return (u.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + u.host;
    } catch (e) {
      return (API_BASE.startsWith('https') ? 'wss:' : 'ws:') + '//' + API_BASE.replace(/^https?:\/\//, '');
    }
  })();

export { API_BASE, WS_BASE };
export default API_BASE;