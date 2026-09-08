import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

function App() {
    return (
        <div className="min-h-screen bg-neutral-50 flex items-center justify-center p-4">
            <Card className="w-full max-w-md shadow-sm border-neutral-200">
                <CardHeader>
                    <div className="flex justify-between items-center mb-2">
                        <CardTitle className="text-xl font-semibold text-neutral-900">shadcn/ui Setup</CardTitle>
                        <Badge variant="outline" className="text-xs text-neutral-500">Verified</Badge>
                    </div>
                    <CardDescription>
                        Successfully integrated shadcn/ui with Tailwind CSS v4 in the RMOS project.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-neutral-600">
                        This minimal component uses Card, Badge, and Button primitives. Path aliases and Tailwind merge are working correctly.
                    </p>
                </CardContent>
                <CardFooter className="flex justify-end space-x-2">
                    <Button variant="outline">Cancel</Button>
                    <Button>Continue Deployment</Button>
                </CardFooter>
            </Card>
        </div>
    )
}

export default App
