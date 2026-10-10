import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { OfficialLogo } from "@/components/rmos/OfficialLogo";
import { ArrowRight, BookOpen, CheckCircle, Shield, Train, Wrench, Zap, Menu, X, Activity } from "lucide-react";
import { useNavigate } from "react-router-dom";

const FEATURES = [
    {
        icon: Train,
        title: "Train Operations",
        description:
            "Synthetic service board with live train states, running delays and path conflicts across corridors.",
    },
    {
        icon: Wrench,
        title: "Maintenance Planning",
        description:
            "Work queue, work orders and execution state for engineering departments, with departmental coordination.",
    },
    {
        icon: Shield,
        title: "Safety-First Optimisation",
        description:
            "Deterministic CP-SAT scheduler with hard safety constraints; every proposal needs human approval.",
    },
    {
        icon: Zap,
        title: "AI Assistant",
        description:
            "Ask about plans, priorities and blocking in plain language. Grounded in system data and RMOS documentation.",
    }
];

/** Workflow steps - every step maps to something actually present in the repository. */
const WORKFLOW = [
    {
        step: "01",
        title: "Register maintenance demand",
        description:
            "Engineering departments raise maintenance tasks with asset, criticality, duration and due date."
    },
    {
        step: "02",
        title: "Score and prioritise",
        description:
            "A deterministic weighted model ranks tasks across safety, train impact, criticality, urgency and effort."
    },
    {
        step: "03",
        title: "Optimise within safety rules",
        description:
            "The CP-SAT optimizer proposes possessions and maintenance windows that satisfy hard safety constraints."
    },
    {
        step: "04",
        title: "Review and approve",
        description:
            "Planners and reviewers inspect conflicts, approve, modify or reject proposals. Nothing executes automatically."
    },
    {
        step: "05",
        title: "Explain and track",
        description:
            "Approved plans flow to weekly and monthly boards while the AI assistant explains the reasoning on request."
    }
];

const NAV_LINKS = [
    { label: "Capabilities", href: "#capabilities" },
    { label: "Workflow", href: "#workflow" },
    { label: "AI Assistant", href: "#capabilities" }
];

export function LandingPage() {
    const navigate = useNavigate();
    const [menuOpen, setMenuOpen] = useState(false);
    const [imageOk, setImageOk] = useState(true);

    const goTo = (path: string) => {
        setMenuOpen(false);
        navigate(path);
    };

    /** Scrolls to the features section, clearing the mobile drawer first. */
    const scrollToCapabilities = () => {
        setMenuOpen(false);
        document.getElementById("capabilities")?.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    return (
        <div className="rmos-public-page bg-paper text-ink">
            {/* ------------------------------------------------------------ */}
            {/* Navigation                                                     */}
            {/* ------------------------------------------------------------ */}
            <header className="sticky top-0 z-30 border-b border-navy-700/40 bg-navy-900/95 backdrop-blur">
                <nav className="rmos-container flex items-center justify-between gap-4 py-3" aria-label="Primary">
                    <a href="#top" className="flex min-w-0 items-center gap-3">
                        <OfficialLogo className="h-9 w-9 shrink-0" />
                        <span className="min-w-0">
                            <span className="block truncate text-lg font-bold tracking-tight text-white">RMOS</span>
                            <span className="hidden text-[11px] uppercase tracking-[0.14em] text-slate-300 sm:block">
                                Railway Maintenance Optimization System
                            </span>
                        </span>
                    </a>

                    <div className="hidden items-center gap-7 lg:flex">
                        {NAV_LINKS.map((link) => (
                            <a
                                key={link.label}
                                href={link.href}
                                className="text-sm font-medium text-slate-200 transition-colors hover:text-white"
                            >
                                {link.label}
                            </a>
                        ))}
                    </div>

                    <div className="flex items-center gap-2">
                        <Button
                            size="sm"
                            onClick={() => goTo("/login")}
                            className="hidden bg-white text-navy-900 hover:bg-slate-100 sm:inline-flex"
                        >
                            Access Demo Workspace
                            <ArrowRight className="ml-2 h-4 w-4" />
                        </Button>
                        <button
                            type="button"
                            onClick={() => setMenuOpen((open) => !open)}
                            aria-label={menuOpen ? "Close navigation menu" : "Open navigation menu"}
                            aria-expanded={menuOpen}
                            className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-navy-700 text-slate-200 lg:hidden"
                        >
                            {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                        </button>
                    </div>
                </nav>

                {menuOpen && (
                    <div className="border-t border-navy-700 bg-navy-900 lg:hidden">
                        <div className="rmos-container flex flex-col gap-1 py-3">
                            {NAV_LINKS.map((link) => (
                                <a
                                    key={link.label}
                                    href={link.href}
                                    onClick={() => setMenuOpen(false)}
                                    className="rounded-md px-2 py-2 text-sm font-medium text-slate-200 hover:bg-navy-800"
                                >
                                    {link.label}
                                </a>
                            ))}
                            <Button size="sm" onClick={() => goTo("/login")} className="mt-2 w-full bg-white text-navy-900 hover:bg-slate-100">
                                Access Demo Workspace
                            </Button>
                        </div>
                    </div>
                )}
            </header>

            <main id="top" className="flex-1">
                {/* ---------------------------------------------------------- */}
                {/* Hero                                                          */}
                {/* ---------------------------------------------------------- */}
                <section className="relative isolate overflow-hidden bg-navy-900 text-white">
                    {imageOk ? (
                        <img
                            src="/railway-hero.svg"
                            alt="Illustration of a passenger train travelling on railway track"
                            onError={() => setImageOk(false)}
                            className="absolute inset-0 h-full w-full object-cover object-center"
                        />
                    ) : (
                        /* Fallback when the image asset cannot be loaded. */
                        <div
                            className="absolute inset-0"
                            style={{
                                background:
                                    "linear-gradient(135deg, #0A1420 0%, #173352 45%, #2A5C8A 100%)",
                            }}
                            aria-hidden="true"
                        />
                    )}
                    <div className="absolute inset-0 bg-gradient-to-r from-navy-900 via-navy-900/85 to-navy-900/35" aria-hidden="true" />

                    <div className="rmos-container relative py-14 sm:py-20 lg:py-28">
                        <div className="max-w-2xl">
                            <div className="inline-flex items-center gap-2 rounded-full border border-amber-line/50 bg-amber-bg/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.12em] text-amber-line">
                                <Activity className="h-3.5 w-3.5" />
                                Deterministic planning · safety-constrained
                            </div>

                            <h1 className="mt-5 text-3xl font-bold leading-[1.12] tracking-tight sm:text-4xl lg:text-5xl">
                                Railway maintenance planning that engineers can actually defend
                            </h1>

                            <p className="mt-5 max-w-xl text-base leading-relaxed text-slate-200 sm:text-lg">
                                RMOS combines a deterministic priority model, a CP-SAT optimiser with hard safety
                                constraints and human approval into a single operational workspace — with an AI assistant
                                that explains every recommendation.
                            </p>

                            <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:items-center">
                                <Button
                                    size="lg"
                                    onClick={() => goTo("/login")}
                                    className="w-full bg-white px-7 py-3 text-navy-900 hover:bg-slate-100 sm:w-auto"
                                >
                                    Access Demo Workspace
                                    <ArrowRight className="ml-2 h-5 w-5" />
                                </Button>
                                <Button
                                    size="lg"
                                    variant="outline"
                                    onClick={scrollToCapabilities}
                                    className="w-full border-slate-400 bg-transparent px-7 py-3 text-white hover:bg-white/10 sm:w-auto"
                                >
                                    <BookOpen className="mr-2 h-5 w-5" />
                                    Explore RMOS
                                </Button>
                            </div>

                            <dl className="mt-10 grid max-w-lg grid-cols-2 gap-x-6 gap-y-5 border-t border-white/15 pt-6 sm:grid-cols-3">
                                <div>
                                    <dt className="text-[11px] uppercase tracking-[0.12em] text-slate-400">Planner</dt>
                                    <dd className="mt-1 text-sm font-medium">CP-SAT possessions</dd>
                                </div>
                                <div>
                                    <dt className="text-[11px] uppercase tracking-[0.12em] text-slate-400">Governance</dt>
                                    <dd className="mt-1 text-sm font-medium">Human approval required</dd>
                                </div>
                                <div>
                                    <dt className="text-[11px] uppercase tracking-[0.12em] text-slate-400">Assistant</dt>
                                    <dd className="mt-1 text-sm font-medium">Grounded explanations</dd>
                                </div>
                            </dl>
                        </div>
                    </div>
                </section>

                {/* ---------------------------------------------------------- */}
                {/* Simulation notice                                            */}
                {/* ---------------------------------------------------------- */}
                <section className="border-b border-amber-line bg-amber-bg">
                    <div className="rmos-container flex flex-col gap-2 py-4 sm:flex-row sm:items-center sm:gap-3">
                        <span className="inline-flex items-center gap-2 text-sm font-semibold text-amber">
                            <span className="h-2 w-2 shrink-0 rounded-full bg-amber" aria-hidden="true" />
                            Simulation Mode
                        </span>
                        <p className="text-sm text-amber">
                            This demonstration runs entirely on synthetic data. Assets, trains, tasks and documents are
                            generated for evaluation and cannot affect real railway operations.
                        </p>
                    </div>
                </section>

                {/* ---------------------------------------------------------- */}
                {/* Capabilities                                                 */}
                {/* ---------------------------------------------------------- */}
                <section id="capabilities" className="scroll-mt-24 bg-surface py-14 sm:py-20">
                    <div className="rmos-container">
                        <div className="max-w-2xl">
                            <h2 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Core capabilities</h2>
                            <p className="mt-3 text-base text-muted">
                                Four connected modules covering the maintenance lifecycle from demand to explanation.
                            </p>
                        </div>

                        <div className="mt-9 grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
                            {FEATURES.map((feature) => (
                                <Card
                                    key={feature.title}
                                    className="h-full border-border bg-paper transition-colors hover:border-blue-line"
                                >
                                    <CardContent className="px-5 py-6">
                                        <span className="inline-flex h-10 w-10 items-center justify-center rounded-md border border-blue-line bg-blue-bg">
                                            <feature.icon className="h-5 w-5 text-blue" aria-hidden="true" />
                                        </span>
                                        <h3 className="mt-4 text-base font-semibold text-ink">{feature.title}</h3>
                                        <p className="mt-2 text-sm leading-relaxed text-muted">{feature.description}</p>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    </div>
                </section>

                {/* ---------------------------------------------------------- */}
                {/* Workflow                                                     */}
                {/* ---------------------------------------------------------- */}
                <section id="workflow" className="scroll-mt-24 border-y border-border bg-paper py-14 sm:py-20">
                    <div className="rmos-container">
                        <div className="max-w-2xl">
                            <h2 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">How the workflow runs</h2>
                            <p className="mt-3 text-base text-muted">
                                RMOS never executes work on its own. Every stage below is a reviewable step inside the
                                workspace.
                            </p>
                        </div>

                        <ol className="mt-9 grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-5">
                            {WORKFLOW.map((item) => (
                                <li
                                    key={item.step}
                                    className="flex h-full flex-col rounded-lg border border-border bg-surface p-5"
                                >
                                    <span className="font-mono text-sm font-semibold text-blue">{item.step}</span>
                                    <h3 className="mt-2 text-base font-semibold text-ink">{item.title}</h3>
                                    <p className="mt-2 text-sm leading-relaxed text-muted">{item.description}</p>
                                </li>
                            ))}
                        </ol>
                    </div>
                </section>

                {/* ---------------------------------------------------------- */}
                {/* Demo access                                                  */}
                {/* ---------------------------------------------------------- */}
                <section className="bg-surface py-14 sm:py-20">
                    <div className="rmos-container">
                        <div className="grid grid-cols-1 items-center gap-8 rounded-lg border border-blue-line bg-blue-bg p-6 sm:p-9 lg:grid-cols-2">
                            <div>
                                <h2 className="text-2xl font-bold tracking-tight text-ink">Access the demo workspace</h2>
                                <p className="mt-3 text-base leading-relaxed text-muted">
                                    No registration. Sign in with the pre-seeded demonstration account to explore every
                                    module, run a planning cycle and query the AI assistant. The account can only see
                                    synthetic simulation data.
                                </p>
                                <Button onClick={() => goTo("/login")} className="mt-6">
                                    Access Demo Workspace
                                    <ArrowRight className="ml-2 h-4 w-4" />
                                </Button>
                            </div>

                            <div className="rounded-lg border border-blue-line bg-surface p-5">
                                <div className="flex items-center gap-2 text-blue">
                                    <CheckCircle className="h-5 w-5" aria-hidden="true" />
                                    <span className="text-sm font-semibold">Pre-configured demo credentials</span>
                                </div>
                                <dl className="mt-4 grid grid-cols-2 gap-4">
                                    <div className="rounded-md border border-border bg-paper px-4 py-3">
                                        <dt className="text-xs uppercase tracking-wide text-muted">Username</dt>
                                        <dd className="mt-1 font-mono text-base font-bold text-ink">admin</dd>
                                    </div>
                                    <div className="rounded-md border border-border bg-paper px-4 py-3">
                                        <dt className="text-xs uppercase tracking-wide text-muted">Password</dt>
                                        <dd className="mt-1 font-mono text-base font-bold text-ink">demo123</dd>
                                    </div>
                                </dl>
                                <p className="mt-4 text-xs leading-relaxed text-muted">
                                    Credentials are pre-filled on the sign-in page. RMOS provides planning
                                    recommendations only — every action still requires human review before execution.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>
            </main>

            {/* ------------------------------------------------------------ */}
            {/* Footer                                                         */}
            {/* ------------------------------------------------------------ */}
            <footer className="border-t border-border bg-navy-900 text-slate-300">
                <div className="rmos-container py-10">
                    <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
                        <div>
                            <div className="flex items-center gap-3">
                                <OfficialLogo className="h-9 w-9" />
                                <span className="text-lg font-bold text-white">RMOS</span>
                            </div>
                            <p className="mt-3 max-w-xs text-sm leading-relaxed">
                                Railway Maintenance Optimization System — deterministic planning, safety-first
                                optimisation and human approval.
                            </p>
                        </div>

                        <nav aria-label="Footer">
                            <h2 className="text-sm font-semibold uppercase tracking-[0.12em] text-white">Explore</h2>
                            <ul className="mt-3 space-y-2 text-sm">
                                <li>
                                    <a href="#capabilities" className="hover:text-white hover:underline">
                                        Capabilities
                                    </a>
                                </li>
                                <li>
                                    <a href="#workflow" className="hover:text-white hover:underline">
                                        Workflow
                                    </a>
                                </li>
                                <li>
                                    <button
                                        type="button"
                                        onClick={scrollToCapabilities}
                                        className="hover:text-white hover:underline"
                                    >
                                        AI Assistant
                                    </button>
                                </li>
                            </ul>
                        </nav>

                        <nav aria-label="Workspace">
                            <h2 className="text-sm font-semibold uppercase tracking-[0.12em] text-white">Workspace</h2>
                            <ul className="mt-3 space-y-2 text-sm">
                                <li>
                                    <button
                                        type="button"
                                        onClick={() => goTo("/login")}
                                        className="hover:text-white hover:underline"
                                    >
                                        Sign in
                                    </button>
                                </li>
                                <li>
                                    <button
                                        type="button"
                                        onClick={() => goTo("/dashboard")}
                                        className="hover:text-white hover:underline"
                                    >
                                        Dashboard
                                    </button>
                                </li>
                                <li className="text-slate-400">Registration is not available</li>
                            </ul>
                        </nav>
                    </div>

                    <div className="mt-9 flex flex-col gap-2 border-t border-white/10 pt-6 text-xs sm:flex-row sm:items-center sm:justify-between">
                        <p>RMOS — Railway Maintenance Optimization System</p>
                        <p>Simulation environment · synthetic data only · no official railway endorsement</p>
                    </div>
                </div>
            </footer>
        </div>
    );
}