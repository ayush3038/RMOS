
interface LogoProps {
    className?: string;
}

export function OfficialLogo({ className = "" }: LogoProps) {
    return (
        <img
            src="/ir-logo.png"
            alt="Indian Railways Logo"
            className={`object-contain ${className}`}
        />
    );
}
