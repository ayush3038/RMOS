import { useState } from 'react';

export function SimulatorControls() {
    const [status, setStatus] = useState<string>('STOPPED');
    const [scenario, setScenario] = useState<string>('TRAIN_DELAY');
    const [loading, setLoading] = useState(false);

    const callApi = async (endpoint: string, body?: any) => {
        setLoading(true);
        try {
            // Retrieve token if using JWT
            const token = localStorage.getItem('token') || '';
            const res = await fetch(`/api/v1/simulation/${endpoint}`, {
                method: body ? 'POST' : 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? { Authorization: `Bearer ${token}` } : {})
                },
                body: body ? JSON.stringify(body) : undefined
            });
            if (!res.ok) console.error("Simulator API error", res.status);
            // Optionally fetch status after action to refresh state
            if (endpoint !== 'status') fetchStatus();
        } catch (e) {
            console.error("Simulator connect error", e);
        }
        setLoading(false);
    };

    const fetchStatus = async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token') || '';
            const res = await fetch('/api/v1/simulation/status', {
                headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) }
            });
            if (res.ok) {
                const data = await res.json();
                setStatus(data.active ? 'RUNNING' : 'STOPPED');
            }
        } catch (e) { }
        setLoading(false);
    };

    const triggerScenario = () => {
        callApi('scenario', { scenario, seed: 42, intervalSeconds: 5 });
    };

    return (
        <div className="flex items-center gap-2 bg-paper border border-border rounded-[6px] px-2 py-1 relative">
            <span className="text-[11px] font-bold text-muted uppercase tracking-wider pr-1">Ops_Sim</span>

            <select
                value={scenario}
                onChange={(e) => setScenario(e.target.value)}
                className="bg-surface text-ink text-[11px] border border-border rounded px-1 py-0.5 outline-none focus:border-blue"
            >
                <option value="NORMAL_OPERATIONS">Normal</option>
                <option value="TRAIN_DELAY">Train Delay</option>
                <option value="URGENT_MAINTENANCE">Urgent Maint</option>
                <option value="BLOCK_UNAVAILABLE">Block UnAvail</option>
            </select>

            <button
                onClick={triggerScenario}
                disabled={loading}
                className="bg-blue hover:bg-blue-hover text-white text-[10.5px] font-semibold px-2 py-0.5 rounded transition-colors disabled:opacity-50"
            >
                INJECT
            </button>

            <div className="h-4 w-[1px] bg-border mx-1" />

            <button
                onClick={() => callApi('start')}
                disabled={loading || status === 'RUNNING'}
                className="text-green hover:bg-green/10 text-[10.5px] font-semibold px-2 py-0.5 rounded transition-colors disabled:opacity-50"
            >
                START
            </button>
            <button
                onClick={() => callApi('stop')}
                disabled={loading || status === 'STOPPED'}
                className="text-red hover:bg-red/10 text-[10.5px] font-semibold px-2 py-0.5 rounded transition-colors disabled:opacity-50"
            >
                STOP
            </button>
            <button
                onClick={() => callApi('reset')}
                disabled={loading}
                className="text-muted hover:bg-muted/10 text-[10.5px] font-semibold px-2 py-0.5 rounded transition-colors disabled:opacity-50"
            >
                RESET
            </button>

            <div className="flex items-center gap-1.5 ml-1">
                <span className={`w-2 h-2 rounded-full ${status === 'RUNNING' ? 'bg-green animate-pulse' : 'bg-muted'}`} />
            </div>
        </div>
    );
}
