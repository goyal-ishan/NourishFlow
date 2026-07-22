import axios from 'axios';
import { API_BASE } from '../config';

// Default axios instance for all backend calls.
// Keeps your code DRY and makes it easier to swap base URL for production.
const client = axios.create({
  baseURL: API_BASE,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

export default client;