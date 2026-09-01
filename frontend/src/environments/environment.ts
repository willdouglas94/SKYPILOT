declare global {
  interface Window {
    __APP_CONFIG__?: {
      apiBaseUrl?: string;
    };
  }
}

const runtimeApiBaseUrl =
  (typeof window !== 'undefined' && window.__APP_CONFIG__?.apiBaseUrl) ||
  (typeof window !== 'undefined' ? 'http://localhost:8080' : 'http://localhost:8080');

export const environment = {
  production: false,
  apiBaseUrl: runtimeApiBaseUrl,
};
