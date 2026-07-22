// Central runtime config for API & WS base URL
const RENDER_BACKEND_URL = 'https://nourishflow-1.onrender.com';

const API_BASE = (typeof process !== 'undefined' && process.env.REACT_APP_API_URL) 
  ? process.env.REACT_APP_API_URL 
  : RENDER_BACKEND_URL;

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