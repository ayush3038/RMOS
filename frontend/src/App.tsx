export default function App() {
    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6 font-sans text-slate-900">
            <div className="max-w-md w-full bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                    <h1 className="text-lg font-semibold tracking-tight text-navy-900">RMOS UI</h1>
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 border border-blue-200">
                        Development
                    </span>
                </div>
                <div className="p-6">
                    <p className="text-sm text-slate-600 mb-6 leading-relaxed">
                        Tailwind CSS foundation is active. The application structure uses utility classes for layout, typography, and status indicators.
                    </p>
                    <div className="flex gap-3 flex-col sm:flex-row">
                        <button className="px-4 py-2 bg-navy-800 hover:bg-navy-900 text-white rounded font-medium text-sm transition-colors shadow-sm w-full cursor-pointer">
                            Primary Action
                        </button>
                        <button className="px-4 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 rounded font-medium text-sm transition-colors shadow-sm w-full cursor-pointer">
                            Secondary Action
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}
