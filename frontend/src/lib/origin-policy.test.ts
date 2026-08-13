import { describe, expect, it } from "vitest";
import { isTrustedRequestOrigin } from "@/lib/origin-policy";

describe("BFF origin policy", () => {
  it("allows safe reads without an Origin header", () => {
    expect(isTrustedRequestOrigin("GET", null, "https://autoblog.example")).toBe(true);
  });

  it("allows same-origin mutations", () => {
    expect(isTrustedRequestOrigin(
      "POST",
      "https://autoblog.example",
      "https://autoblog.example"
    )).toBe(true);
  });

  it("rejects cross-origin and origin-less mutations", () => {
    expect(isTrustedRequestOrigin(
      "DELETE",
      "https://attacker.example",
      "https://autoblog.example"
    )).toBe(false);
    expect(isTrustedRequestOrigin("PATCH", null, "https://autoblog.example")).toBe(false);
  });
});
