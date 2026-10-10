import { useCallback, useEffect, useRef, useState } from "react";
import { PageHeader } from "@/components/rmos/PageHeader";
import { DemoBanner } from "@/components/rmos/DemoBanner";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle
} from "@/components/ui/card";
import { ApiError } from "@/services/api/client";
import {
    getChatStatus,
    sendChatMessage,
    type ChatStatus,
    type ChatTurn
} from "@/services/api/chat";
import { resolveProviderBadge, providerLabel } from "@/lib/chatProvider";
import { useToast } from "@/store/toast";
import {
    AlertTriangle,
    Bot,
    Database,
    FileText,
    Loader2,
    Plus,
    RefreshCcw,
    Send,
    ShieldCheck
} from "lucide-react";

interface ChatMessage {
    id: number;
    role: "user" | "assistant";
    content: string;
    error?: boolean;
    sources?: { sourceReference?: string; documentType?: string; similarity?: number }[];
    systemDataIncluded?: boolean;
    provider?: string;
    model?: string;
    disclaimer?: string;
}

const SUGGESTIONS = [
    "Explain this maintenance plan.",
    "Why was this asset prioritized?",
    "Summarize the current scheduling result.",
    "Explain the railway block-planning workflow."
];

const MAX_MESSAGE_CHARS = 4000;
const HISTORY_LIMIT = 12;

let messageSeq = 0;

export function AssistantPage() {
    const { toast } = useToast();

    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState("");
    const [sending, setSending] = useState(false);
    const [failedText, setFailedText] = useState<string | null>(null);

    const [status, setStatus] = useState<ChatStatus | null>(null);
    const [statusLoading, setStatusLoading] = useState(true);
    const [statusError, setStatusError] = useState<string | null>(null);
    /*
     * Provider metadata taken from the most recent chat reply. The backend puts
     * `provider` and `model` on every response, so this is the authoritative
     * record of which provider actually served the request - it takes precedence
     * over the status snapshot in the badge.
     */
    const [liveProvider, setLiveProvider] = useState<string | null>(null);
    const [liveModel, setLiveModel] = useState<string | null>(null);

    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    const loadStatus = useCallback(async () => {
        setStatusLoading(true);
        setStatusError(null);
        try {
            const next = await getChatStatus();
            setStatus(next);
            /*
             * Re-sync with the backend. A provider latched from an older reply
             * must never outlive the backend's current answer - otherwise the
             * badge keeps claiming a provider the server no longer uses (for
             * example an "Ollama Active" badge surviving a switch to Gemini).
             */
            setLiveProvider(null);
            setLiveModel(null);
        } catch (error) {
            setStatusError(error instanceof Error ? error.message : "Status unavailable");
        } finally {
            setStatusLoading(false);
        }
    }, []);

    useEffect(() => {
        void loadStatus();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Re-check when the tab regains focus: a backend restart or a key change made
    // while the user was away would otherwise leave a stale provider indicator.
    useEffect(() => {
        const onFocus = () => void loadStatus();
        window.addEventListener("focus", onFocus);
        return () => window.removeEventListener("focus", onFocus);
    }, [loadStatus]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
    }, [messages, sending]);

    const sendMessage = useCallback(
        async (rawText: string, options?: { skipUserAppend?: boolean }) => {
            const text = rawText.trim();
            if (!text || sending) return;

            if (!options?.skipUserAppend) {
                setMessages((previous) => [
                    ...previous,
                    { id: ++messageSeq, role: "user", content: text }
                ]);
            }
            setInput("");
            setSending(true);
            setFailedText(null);

            const history: ChatTurn[] = messages
                .filter((message) => !message.error)
                .slice(-HISTORY_LIMIT)
                .map((message) => ({
                    role: message.role,
                    content: message.content
                }));
            // On retry the pending user message is already in the list; do not
            // replay it twice (it is always sent as the final user turn).
            if (
                options?.skipUserAppend &&
                history.length > 0 &&
                history[history.length - 1].role === "user" &&
                history[history.length - 1].content === text
            ) {
                history.pop();
            }

            try {
                const reply = await sendChatMessage(text, history);
                // The reply itself states which provider handled the request.
                if (reply?.provider) {
                    setLiveProvider(reply.provider);
                    setLiveModel(reply.model ?? null);
                }
                setMessages((previous) => [
                    ...previous,
                    {
                        id: ++messageSeq,
                        role: "assistant",
                        content: reply.reply,
                        sources: reply.sources,
                        systemDataIncluded: reply.systemDataIncluded,
                        provider: reply.provider,
                        model: reply.model,
                        disclaimer: reply.disclaimer
                    }
                ]);
            } catch (error) {
                const apiError = error instanceof ApiError ? error : null;
                if (apiError && (apiError.status === 401 || apiError.status === 403)) {
                    // Session expired - the ProtectedRoute will handle redirect to login
                    toast("Session expired. Please sign in again.", "danger");
                    setFailedText(text);
                } else {
                    const detail =
                        error instanceof Error ? error.message : "The assistant request failed.";
                    setMessages((previous) => [
                        ...previous,
                        { id: ++messageSeq, role: "assistant", content: detail, error: true }
                    ]);
                    setFailedText(text);
                }
            } finally {
                setSending(false);
            }
        },
        [messages, sending, toast]
    );

    const retryLast = useCallback(() => {
        if (!failedText) return;
        setMessages((previous) => previous.filter((message) => !message.error));
        void sendMessage(failedText, { skipUserAppend: true });
    }, [failedText, sendMessage]);

    const newConversation = useCallback(() => {
        setMessages([]);
        setFailedText(null);
        setInput("");
        // Drop any provider latched from the previous conversation so the badge
        // reflects the current backend state, not an old reply.
        setLiveProvider(null);
        setLiveModel(null);
        textareaRef.current?.focus();
    }, []);

    const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
            event.preventDefault();
            void sendMessage(input);
        }
    };

    // A provider is "ready" when the backend status says so, or when a chat reply
    // has already proven one is serving requests (status fetch may have failed
    // while the provider itself is perfectly healthy).
    const providerReady = Boolean(status?.available) || Boolean(liveProvider);

    /*
     * Provider indicator. Every label comes from backend data: the provider id
     * on the latest chat reply when one exists, otherwise `chat/status`.
     * There is no hardcoded provider and no optimistic default - when nothing is
     * configured the badge says so instead of claiming a provider is active.
     */
    const badge = resolveProviderBadge({
        available: providerReady,
        provider: liveProvider ?? status?.provider ?? null,
        model: liveModel ?? status?.model ?? null,
        loading: statusLoading,
        error: statusError,
        hasLive: Boolean(liveProvider)
    });

    const statusChip =
        badge.kind === "loading" ? (
            <span className="inline-flex items-center gap-1.5 h-7 px-2.5 rounded-full border bg-surface text-[11.5px] font-medium text-muted">
                <Loader2 className="h-3 w-3 animate-spin" />
                Checking provider…
            </span>
        ) : badge.kind === "error" ? (
            <span
                className="inline-flex items-center gap-1.5 h-7 px-2.5 rounded-full border border-red-line bg-red-bg text-[11.5px] font-medium text-red"
                title={badge.detail}
            >
                <AlertTriangle className="h-3 w-3" />
                Status unavailable
            </span>
        ) : badge.kind === "ready" ? (
            <span
                className="inline-flex items-center gap-1.5 h-7 px-2.5 rounded-full border border-green-line bg-green-bg text-[11.5px] font-medium text-green"
                title={status?.message}
            >
                <span className="h-1.5 w-1.5 rounded-full bg-green" />
                {badge.label}
                {badge.model && <span className="font-mono text-[10.5px] opacity-80">· {badge.model}</span>}
            </span>
        ) : (
            <span
                className="inline-flex items-center gap-1.5 h-7 px-2.5 rounded-full border border-amber-line bg-amber-bg text-[11.5px] font-medium text-amber"
                title={badge.detail ?? status?.message}
            >
                <AlertTriangle className="h-3 w-3" />
                AI Provider Unavailable
            </span>
        );

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title="RMOS AI Assistant"
                description="Ask about maintenance plans, block planning, priority decisions and RMOS functionality."
                actions={
                    <>
                        {statusChip}
                        {messages.length > 0 && (
                            <Button variant="outline" size="sm" onClick={newConversation}>
                                <Plus className="h-4 w-4" />
                                New conversation
                            </Button>
                        )}
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => void loadStatus()}
                            disabled={statusLoading}
                            title="Refresh provider status"
                        >
                            <RefreshCcw className="h-4 w-4" />
                            Refresh status
                        </Button>
                    </>
                }
            />

            <DemoBanner />

            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Bot className="h-5 w-5 text-primary" />
                        Assistant
                    </CardTitle>
                    <CardDescription>
                        {status?.retrievalConfigured
                            ? "Answers can cite authorized RMOS documentation and verified system data."
                            : "Answers use verified system data; document retrieval is not configured."}
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    {status && !providerReady && !statusLoading ? (
                        <div className="flex flex-col gap-2 max-w-2xl">
                            <div className="flex items-center gap-2 text-sm font-medium">
                                <AlertTriangle className="h-4 w-4 text-amber" />
                                {status.message}
                            </div>
                            <ul className="flex flex-col gap-1.5 text-[12.5px] text-muted list-disc pl-5">
                                {status.setupInstructions.map((instruction) => (
                                    <li key={instruction}>{instruction}</li>
                                ))}
                            </ul>
                        </div>
                    ) : (
                        <div className="flex flex-col gap-3 min-h-[200px]">
                            {messages.length === 0 && !sending && (
                                <div className="flex flex-col gap-2">
                                    <p className="text-[12.5px] text-muted">Suggested questions:</p>
                                    <div className="flex flex-wrap gap-2">
                                        {SUGGESTIONS.map((suggestion) => (
                                            <button
                                                key={suggestion}
                                                type="button"
                                                onClick={() => void sendMessage(suggestion)}
                                                className="text-[12.5px] px-3 py-1.5 rounded-full border bg-surface hover:bg-slate-50 transition-colors text-left"
                                            >
                                                {suggestion}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {messages.map((message) =>
                                message.role === "user" ? (
                                    <div key={message.id} className="flex justify-end">
                                        <div className="max-w-[88%] sm:max-w-[75%] rounded-lg bg-navy-900 text-white px-3.5 py-2.5 text-sm whitespace-pre-wrap break-words">
                                            {message.content}
                                        </div>
                                    </div>
                                ) : message.error ? (
                                    <div
                                        key={message.id}
                                        className="max-w-[92%] sm:max-w-[80%] rounded-lg border border-red-line bg-red-bg px-3.5 py-2.5 text-sm flex flex-col gap-2"
                                    >
                                        <div className="flex items-start gap-2 text-red">
                                            <AlertTriangle className="h-4 w-4 mt-0.5 shrink-0" />
                                            <span className="whitespace-pre-wrap break-words">{message.content}</span>
                                        </div>
                                        <div className="flex gap-2">
                                            <Button size="xs" variant="outline" onClick={retryLast}>
                                                <RefreshCcw className="h-3 w-3" />
                                                Retry
                                            </Button>
                                            {statusError && (
                                                <Button
                                                    size="xs"
                                                    variant="ghost"
                                                    onClick={() => void loadStatus()}
                                                >
                                                    Check provider status
                                                </Button>
                                            )}
                                        </div>
                                    </div>
                                ) : (
                                    <div key={message.id} className="flex justify-start">
                                        <div className="max-w-[92%] sm:max-w-[80%] rounded-lg border bg-surface px-3.5 py-2.5 text-sm">
                                            <div className="whitespace-pre-wrap break-words">{message.content}</div>

                                            {(Boolean(message.sources?.length) || message.systemDataIncluded) && (
                                                <div className="flex flex-wrap items-center gap-1.5 mt-2.5 pt-2.5 border-t">
                                                    {message.systemDataIncluded && (
                                                        <span
                                                            className="inline-flex items-center gap-1 text-[10.5px] font-medium px-1.5 py-0.5 rounded border border-green-line bg-green-bg text-green"
                                                            title="Grounded in live, read-only RMOS system data"
                                                        >
                                                            <Database className="h-3 w-3" />
                                                            Verified system data
                                                        </span>
                                                    )}
                                                    {Boolean(message.sources?.length) && (
                                                        <>
                                                            <span
                                                                className="inline-flex items-center gap-1 text-[10.5px] font-medium px-1.5 py-0.5 rounded border border-blue-line bg-blue-bg text-blue"
                                                                title="Retrieved from authorized RMOS documentation"
                                                            >
                                                                <FileText className="h-3 w-3" />
                                                                RMOS docs · {message.sources?.length}
                                                            </span>
                                                            {message.sources?.slice(0, 3).map((source, index) => (
                                                                <span
                                                                    key={`${source.sourceReference ?? "doc"}-${index}`}
                                                                    className="text-[10.5px] px-1.5 py-0.5 rounded bg-slate-100 text-muted font-mono"
                                                                    title={source.documentType}
                                                                >
                                                                    {source.sourceReference ?? source.documentType ?? "document"}
                                                                    {typeof source.similarity === "number"
                                                                        ? ` · ${source.similarity.toFixed(2)}`
                                                                        : ""}
                                                                </span>
                                                            ))}
                                                        </>
                                                    )}
                                                    <span className="ml-auto text-[10.5px] text-muted font-mono">
                                                        {providerLabel(message.provider)}
                                                        {message.model ? ` · ${message.model}` : ""}
                                                    </span>
                                                </div>
                                            )}

                                            {message.disclaimer && (
                                                <p className="flex items-start gap-1 mt-2 text-[11px] text-muted">
                                                    <ShieldCheck className="h-3 w-3 mt-0.5 shrink-0" />
                                                    {message.disclaimer}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                )
                            )}

                            {sending && (
                                <div className="flex justify-start">
                                    <div className="inline-flex items-center gap-2 rounded-lg border bg-slate-50 px-3.5 py-2.5 text-sm text-muted">
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        Generating response…
                                    </div>
                                </div>
                            )}

                            <div ref={messagesEndRef} />
                        </div>
                    )}
                </CardContent>

                <CardFooter className="border-t flex-col gap-2 [.border-t]:pt-4">
                    <form
                        className="w-full flex flex-col gap-2"
                        onSubmit={(event) => {
                            event.preventDefault();
                            void sendMessage(input);
                        }}
                    >
                        <textarea
                            ref={textareaRef}
                            rows={2}
                            maxLength={MAX_MESSAGE_CHARS}
                            value={input}
                            onChange={(event) => setInput(event.target.value)}
                            onKeyDown={handleKeyDown}
                            disabled={sending}
                            placeholder={
                                providerReady
                                    ? "Ask about maintenance plans, blocks, priorities…"
                                    : "Configure an AI provider to start chatting…"
                            }
                            aria-label="Message for the RMOS AI assistant"
                            className="w-full resize-none rounded-md border bg-surface px-3 py-2 text-sm outline-none focus:border-ring disabled:opacity-60 min-h-[64px]"
                        />
                        <div className="flex items-center justify-between gap-2">
                            <span className="hidden sm:inline text-[11px] text-muted">
                                Enter to send · Shift+Enter for a new line
                            </span>
                            <Button
                                type="submit"
                                size="sm"
                                disabled={sending || !input.trim() || !providerReady}
                                title={providerReady ? "Send message" : "No AI provider configured"}
                            >
                                {sending ? (
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                ) : (
                                    <Send className="h-4 w-4" />
                                )}
                                Send
                            </Button>
                        </div>
                    </form>
                </CardFooter>
            </Card>
        </div>
    );
}