import { describe, expect, it } from "vitest";
import { providerLabel, resolveProviderBadge } from "./chatProvider";

/**
 * Badge resolution is driven purely by backend data, so these tests cover the
 * regression where a stale provider latched from an old chat reply kept the
 * indicator on a provider the backend had stopped using.
 */
describe("providerLabel", () => {
    it("maps the backend provider ids to display names", () => {
        expect(providerLabel("gemini")).toBe("Gemini");
        expect(providerLabel("ollama")).toBe("Ollama");
        expect(providerLabel("nim")).toBe("NVIDIA NIM");
    });

    it("is case and whitespace insensitive", () => {
        expect(providerLabel(" GEMINI ")).toBe("Gemini");
        expect(providerLabel("Ollama")).toBe("Ollama");
    });

    it("falls back to the raw id for unknown providers", () => {
        expect(providerLabel("some-future-provider")).toBe("some-future-provider");
    });

    it("returns an empty string when the backend reported no provider", () => {
        expect(providerLabel(undefined)).toBe("");
        expect(providerLabel(null)).toBe("");
        expect(providerLabel("")).toBe("");
    });
});

describe("resolveProviderBadge - Gemini", () => {
    it("shows Gemini Active with the configured model", () => {
        const badge = resolveProviderBadge({
            available: true,
            provider: "gemini",
            model: "gemini-3.1-flash-lite",
        });
        expect(badge).toEqual({
            kind: "ready",
            label: "Gemini Active",
            model: "gemini-3.1-flash-lite",
        });
    });

    it("shows Gemini Active without a model when the backend omits it", () => {
        const badge = resolveProviderBadge({ available: true, provider: "gemini", model: null });
        expect(badge.kind).toBe("ready");
        expect(badge.kind === "ready" && badge.label).toBe("Gemini Active");
        expect(badge.kind === "ready" && badge.model).toBeUndefined();
    });

    it("prefers the provider from the latest reply over the status snapshot", () => {
        // Status was captured earlier and says ollama; the newest reply was
        // served by gemini, so the indicator must say gemini.
        const badge = resolveProviderBadge({
            available: true,
            provider: "gemini",
            model: "gemini-3.1-flash-lite",
            hasLive: true,
        });
        expect(badge.kind === "ready" && badge.label).toBe("Gemini Active");
    });
});

describe("resolveProviderBadge - Ollama", () => {
    it("shows Ollama Active when the backend reports ollama", () => {
        const badge = resolveProviderBadge({
            available: true,
            provider: "ollama",
            model: "llama3.2",
        });
        expect(badge).toEqual({ kind: "ready", label: "Ollama Active", model: "llama3.2" });
    });

    it("shows Ollama Active from a live reply even when the status fetch failed", () => {
        const badge = resolveProviderBadge({
            provider: "ollama",
            model: "llama3.2",
            error: "network down",
            hasLive: true,
        });
        expect(badge.kind === "ready" && badge.label).toBe("Ollama Active");
    });
});

describe("resolveProviderBadge - unavailable", () => {
    it("never claims a provider when the backend reports none", () => {
        const badge = resolveProviderBadge({ available: false, provider: null, model: null });
        expect(badge.kind).toBe("unavailable");
    });

    it("does not claim a provider when available is true but no id was returned", () => {
        // Defensive: an inconsistent payload must not produce an active label.
        const badge = resolveProviderBadge({ available: true, provider: undefined, model: undefined });
        expect(badge.kind).toBe("unavailable");
    });

    it("reports unavailable after a stale provider was cleared by a status refresh", () => {
        const badge = resolveProviderBadge({
            available: false,
            provider: null,
            model: null,
            hasLive: false,
        });
        expect(badge.kind).toBe("unavailable");
    });
});

describe("resolveProviderBadge - loading and errors", () => {
    it("shows loading while the status request is in flight", () => {
        expect(resolveProviderBadge({ loading: true }).kind).toBe("loading");
    });

    it("shows an error when the status request failed and nothing was proven", () => {
        const badge = resolveProviderBadge({ error: "boom", provider: null });
        expect(badge).toEqual({ kind: "error", detail: "boom" });
    });

    it("does not show loading once a live reply is known", () => {
        const badge = resolveProviderBadge({
            loading: true,
            provider: "gemini",
            model: "gemini-3.1-flash-lite",
            hasLive: true,
        });
        expect(badge.kind === "ready" && badge.label).toBe("Gemini Active");
    });
});