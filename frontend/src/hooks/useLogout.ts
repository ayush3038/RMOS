import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { signOut } from "@/services/api/session";

/**
 * Single shared logout action used by every logout entry point in the app.
 *
 * Behaviour:
 * - clears the JWT and the persisted identity (`rmos_jwt`, `rmos.session`),
 * - leaves workspace preferences and application data untouched,
 * - navigates to /login with `replace` so the protected page is not left in the
 *   history stack.
 *
 * Because the token is gone, re-opening /dashboard or pressing Back re-mounts
 * `ProtectedRoute`, which re-reads the session and redirects to /login again.
 */
export function useLogout() {
    const navigate = useNavigate();

    return useCallback(() => {
        signOut();
        navigate("/login", { replace: true });
    }, [navigate]);
}