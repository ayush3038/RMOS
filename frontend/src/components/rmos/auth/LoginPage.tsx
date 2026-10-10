import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { OfficialLogo } from "@/components/rmos/OfficialLogo";
import { AlertCircle, ArrowLeft, CheckCircle, Eye, EyeOff, Loader2, LogIn, Shield } from "lucide-react";
import { signIn } from "@/services/api/chat";
import { useToast } from "@/store/toast";
import { ApiError } from "@/services/api/client";

const DEMO_USERNAME = "admin";
const DEMO_PASSWORD = "demo123";

export function LoginPage() {
    const navigate = useNavigate();
    const { toast } = useToast();

    const [username, setUsername] = useState(DEMO_USERNAME);
    const [password, setPassword] = useState(DEMO_PASSWORD);
    const [showPassword, setShowPassword] = useState(false);
    const [signingIn, setSigningIn] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!username.trim() || !password || signingIn) return;

        setSigningIn(true);
        setError(null);

        try {
            const response = await signIn(username.trim(), password);
            toast(`Signed in as ${response.role}.`, "success");
            navigate("/dashboard", { replace: true });
        } catch (err) {
            const message =
                err instanceof ApiError
                    ? err.message
                    : err instanceof Error
                      ? err.message
                      : "Sign-in failed. Is the backend running on http://localhost:8080?";
            setError(message);
        } finally {
            setSigningIn(false);
        }
    };

    return (
        <div className="rmos-public-page bg-navy-900 text-ink">
            {/* Slim header keeps the identity visible without eating vertical space. */}
            <header className="border-b border-navy-700/40">
                <div className="rmos-container flex items-center justify-between gap-4 py-3">
                    <div className="flex min-w-0 items-center gap-3">
                        <OfficialLogo className="h-9 w-9 shrink-0" />
                        <span className="min-w-0">
                            <span className="block truncate text-lg font-bold tracking-tight text-white">RMOS</span>
                            <span className="hidden text-[11px] uppercase tracking-[0.14em] text-slate-300 sm:block">
                                Railway Maintenance Optimization System
                            </span>
                        </span>
                    </div>
                    <button
                        type="button"
                        onClick={() => navigate("/")}
                        className="inline-flex items-center gap-2 text-sm font-medium text-slate-300 hover:text-white"
                    >
                        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
                        <span className="hidden sm:inline">Back to site</span>
                        <span className="sm:hidden">Back</span>
                    </button>
                </div>
            </header>

            {/*
                Vertical centering uses `my-auto` on the child instead of
                `items-center` on the parent: auto margins collapse to zero when
                the content is taller than the viewport, so the top of the card
                (header, error banner, first field) is never pushed out of reach.
                On short viewports the page simply grows and scrolls.
            */}
            <main className="flex flex-1 flex-col items-center px-4 py-8 sm:py-12">
                <div className="my-auto w-full max-w-md">
                    <div className="mb-6 text-center">
                        <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">Sign in</h1>
                        <p className="mt-2 text-sm text-slate-300">
                            Enter the demo credentials to open the RMOS workspace.
                        </p>
                    </div>

                    {/* Demo credential panel - visually distinct from the form. */}
                    <Card className="mb-5 border-green-line bg-green-bg">
                        <CardContent className="px-5 py-4">
                            <div className="flex items-center gap-2 text-green">
                                <CheckCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
                                <span className="text-sm font-semibold">Demo credentials (pre-filled)</span>
                            </div>
                            <dl className="mt-3 grid grid-cols-2 gap-3">
                                <div className="rounded-md border border-green-line bg-surface px-3 py-2 text-center">
                                    <dt className="text-xs uppercase tracking-wide text-muted">Username</dt>
                                    <dd className="mt-0.5 font-mono text-sm font-bold text-ink">{DEMO_USERNAME}</dd>
                                </div>
                                <div className="rounded-md border border-green-line bg-surface px-3 py-2 text-center">
                                    <dt className="text-xs uppercase tracking-wide text-muted">Password</dt>
                                    <dd className="mt-0.5 font-mono text-sm font-bold text-ink">{DEMO_PASSWORD}</dd>
                                </div>
                            </dl>
                            <p className="mt-3 flex items-start gap-1.5 text-xs leading-relaxed text-green">
                                <Shield className="mt-0.5 h-3 w-3 shrink-0" aria-hidden="true" />
                                <span>Simulation data only — this account cannot affect real railway operations.</span>
                            </p>
                        </CardContent>
                    </Card>

                    <Card className="border-border bg-surface">
                        <CardHeader>
                            <CardTitle className="text-lg">RMOS workspace</CardTitle>
                            <CardDescription>Sign in with your RMOS credentials to continue.</CardDescription>
                        </CardHeader>

                        <form onSubmit={handleSubmit} className="space-y-4 px-6" noValidate>
                            {error && (
                                <div
                                    role="alert"
                                    aria-live="polite"
                                    className="flex items-start gap-2 rounded-md border border-red-line bg-red-bg p-3 text-sm text-red"
                                >
                                    <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
                                    <span>{error}</span>
                                </div>
                            )}

                            <div className="space-y-1.5">
                                <Label htmlFor="username" className="text-sm font-medium text-muted">
                                    Username
                                </Label>
                                <Input
                                    id="username"
                                    name="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    autoComplete="username"
                                    autoFocus
                                    required
                                    disabled={signingIn}
                                    aria-describedby={error ? "login-error" : undefined}
                                />
                            </div>

                            <div className="space-y-1.5">
                                <Label htmlFor="password" className="text-sm font-medium text-muted">
                                    Password
                                </Label>
                                <div className="relative">
                                    <Input
                                        id="password"
                                        name="password"
                                        type={showPassword ? "text" : "password"}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        autoComplete="current-password"
                                        required
                                        disabled={signingIn}
                                        className="pr-10"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword((visible) => !visible)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                                        aria-label={showPassword ? "Hide password" : "Show password"}
                                        aria-pressed={showPassword}
                                        tabIndex={0}
                                        disabled={signingIn}
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-4 w-4" aria-hidden="true" />
                                        ) : (
                                            <Eye className="h-4 w-4" aria-hidden="true" />
                                        )}
                                    </button>
                                </div>
                            </div>

                            <Button type="submit" className="h-10 w-full" disabled={signingIn || !username.trim() || !password}>
                                {signingIn ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />
                                        Signing in…
                                    </>
                                ) : (
                                    <>
                                        <LogIn className="mr-2 h-4 w-4" aria-hidden="true" />
                                        Sign In
                                    </>
                                )}
                            </Button>
                        </form>

                        <CardFooter className="border-t pt-4">
                            <p className="text-center text-xs leading-relaxed text-muted">
                                No registration available. The demo account accesses simulation data only.
                            </p>
                        </CardFooter>
                    </Card>

                    <p className="mx-auto mt-6 max-w-md text-center text-xs leading-relaxed text-slate-400">
                        RMOS provides planning recommendations only. Every action still requires human review before
                        execution.
                    </p>
                </div>
            </main>
        </div>
    );
}