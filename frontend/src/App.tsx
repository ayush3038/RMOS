import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { PageHeader } from "@/components/rmos/PageHeader";
import { SectionHeader } from "@/components/rmos/SectionHeader";
import { StatusChip } from "@/components/rmos/StatusChip";
import { EmptyState } from "@/components/rmos/EmptyState";
import { LoadingState } from "@/components/rmos/LoadingState";

function App() {
    return (
        <div className="min-h-screen bg-background">
            {/* Fake Topbar */}
            <header className="h-[56px] bg-surface border-b border-border flex items-center px-[18px]">
                <div className="font-bold text-ink">RMOS Design System Verification</div>
            </header>

            {/* Main Content Area */}
            <main className="p-5 max-w-[1200px] mx-auto">
                <PageHeader
                    title="Typography & Foundation"
                    description="Verifying the design system tokens, typography, and foundational components extracted from RAILSYNC."
                    actions={<Button variant="outline">Docs</Button>}
                />

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <Card>
                        <SectionHeader title="Status Chips" description="Semantic status tokens" />
                        <CardContent className="pt-4 flex flex-wrap gap-2">
                            <StatusChip status="Critical" />
                            <StatusChip status="High" />
                            <StatusChip status="Medium" />
                            <StatusChip status="Low" />
                            <StatusChip status="Completed" />
                            <StatusChip status="Scheduled" />
                        </CardContent>
                    </Card>

                    <Card>
                        <SectionHeader title="Typography" description="Inter & IBM Plex Mono" />
                        <CardContent className="pt-4 flex flex-col gap-2">
                            <div className="text-[20px] font-bold text-ink">Primary Heading</div>
                            <div className="text-[14px] text-ink-soft">Body text uses ink-soft for high readability on dense screens.</div>
                            <div className="text-[12.5px] text-muted">Muted text for helper descriptions and secondary info.</div>
                            <div className="font-mono text-[11.5px] text-navy-600 font-bold border rounded bg-paper px-2 py-1 self-start">MONO-ID-772</div>
                        </CardContent>
                    </Card>

                    <Card>
                        <SectionHeader title="Forms & Inputs" />
                        <CardContent className="pt-4 space-y-4">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-[11px] font-semibold text-ink-soft">Maintenance Description</label>
                                <Input placeholder="Enter details..." />
                            </div>
                            <div className="flex gap-2">
                                <Button>Primary Action</Button>
                                <Button variant="outline">Secondary</Button>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <SectionHeader title="Miscellaneous Foundation" />
                        <CardContent className="pt-4">
                            <Tabs defaultValue="empty" className="w-full">
                                <TabsList>
                                    <TabsTrigger value="empty">Empty State</TabsTrigger>
                                    <TabsTrigger value="loading">Loading State</TabsTrigger>
                                </TabsList>
                                <TabsContent value="empty" className="border rounded mt-2 border-border-soft bg-paper">
                                    <EmptyState message="No conflicts detected in this block." />
                                </TabsContent>
                                <TabsContent value="loading" className="border rounded mt-2 border-border-soft bg-paper">
                                    <LoadingState message="Processing simulation..." />
                                </TabsContent>
                            </Tabs>
                        </CardContent>
                    </Card>
                </div>
            </main>
        </div>
    );
}

export default App;
