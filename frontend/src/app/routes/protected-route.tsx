import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "@/features/auth";

interface ProtectedRouteProps {
  requiredRoles?: string[];
}

export function ProtectedRoute({ requiredRoles }: ProtectedRouteProps) {
  const { loginState } = useAuth();
  const location = useLocation();

  // 로그인 상태 확인
  if (!loginState.email) {
    return <Navigate to="/member/login" state={{ from: location }} replace />;
  }

  // 역할 기반 접근 제어
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = loginState.roleNames || [];
    const hasRequiredRole = requiredRoles.some((role) =>
      userRoles.includes(role)
    );

    if (!hasRequiredRole) {
      return <Navigate to="/" replace />;
    }
  }

  return <Outlet />;
}
