const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"]);

export function isTrustedRequestOrigin(
  method: string,
  originHeader: string | null,
  requestOrigin: string
) {
  if (SAFE_METHODS.has(method)) {
    return true;
  }
  return originHeader !== null && originHeader === requestOrigin;
}
