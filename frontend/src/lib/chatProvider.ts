/**
 * Chat provider presentation helpers.
 *
 * The provider name shown in the UI always comes from the backend
 * (`GET /api/v1/chat/status` or the provider metadata on each chat reply).
 * Nothing here invents a provider, defaults to one, or guesses a model.
 */

/** Display names for the provider identifiers the backend can report. */
const PROVIDER_LABELS: Record<string, string> = {
    gemini: "Gemini",
    ollama: "Ollama",
    nim: "NVIDIA NIM",
};

/** Pretty label for a backend provider id, falling back to the raw id. */
export function providerLabel(provider?: string | null): string {
    const id = (provider ?? "").trim().toLowerCase();
    if (!id) return "";
    return PROVIDER_LABELS[id] ?? id;
}

export type ProviderBadgeState =
    | { kind: "loading" }
    | { kind: "error"; detail?: string }
    | { kind: "ready"; label: string; model?: string }
    | { kind: "unavailable"; detail?: string };

/**
 * Derives the badge to render.
 *
 * `liveProvider`/`liveModel` come from the provider metadata of the most recent
 * chat reply, i.e. the provider that actually served that request. They take
 * precedence over the status snapshot so the indicator can never contradict a
 * response the user just received.
 *
 * @param available  `status.available` from the backend status endpoint.
 * @param provider   provider id from the backend (status or reply metadata).
 * @param model      model id from the backend, when available.
 * @param loading    the status request is still in flight.
 * @param error      the status request failed.
 * @param hasLive    the provider metadata came from an actual chat reply.
 */
export function resolveProviderBadge(input: {
    available?: boolean;
    provider?: string | null;
    model?: string | null;
    loading?: boolean;
    error?: string | null;
    hasLive?: boolean;
}): ProviderBadgeState {
    const { available, provider, model, loading, error, hasLive } = input;

    if (loading && !hasLive) {
        return { kind: "loading" };
    }
    if (error && !hasLive) {
        return { kind: "error", detail: error };
    }

    const label = providerLabel(provider);
    const hasProvider = Boolean(label);

    // An actual reply always proves a provider served the request.
    if (hasLive && hasProvider) {
        return { kind: "ready", label: `${label} Active`, model: model?.trim() || undefined };
    }

    // No provider id, or the backend explicitly reported none: never claim one.
    if (available && hasProvider) {
        return { kind: "ready", label: `${label} Active`, model: model?.trim() || undefined };
    }

    return { kind: "unavailable", detail: error ?? undefined };
}