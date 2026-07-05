const TOKEN_KEY = "autoblog.accessToken";

let memoryToken: string | null = null;

export function getAccessToken() {
  if (memoryToken) {
    return memoryToken;
  }
  if (typeof window === "undefined") {
    return null;
  }
  memoryToken = window.localStorage.getItem(TOKEN_KEY);
  return memoryToken;
}

export function setAccessToken(token: string) {
  memoryToken = token;
  if (typeof window !== "undefined") {
    window.localStorage.setItem(TOKEN_KEY, token);
  }
}

export function clearAccessToken() {
  memoryToken = null;
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(TOKEN_KEY);
  }
}

// TODO: Move production token storage to HttpOnly cookies through a BFF/API gateway.
