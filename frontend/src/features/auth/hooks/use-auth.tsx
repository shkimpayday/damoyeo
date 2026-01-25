import { Navigate, useNavigate } from "react-router";
import { useAuthStore } from "../stores";

export function useAuth() {
  const { member, status, login, logout } = useAuthStore();

  const loginState = member;
  const loginStatus = status;

  // 쿠키 복원은 이제 auth-store에서 초기화 시점에 처리됨

  const navigate = useNavigate();

  const doLogin = async (email: string, pw: string) => {
    await login(email, pw);
  };

  const doLogout = () => {
    logout();
    navigate("/");
  };

  const moveToLogin = () => {
    navigate("/member/login");
  };

  const moveToLoginReturn = () => {
    return <Navigate replace to="/member/login" />;
  };

  const moveToPath = (path: string) => {
    navigate({ pathname: path }, { replace: true });
  };

  // 로그인 여부 확인
  const isLoggedIn = !!loginState.email;

  // 특정 역할 확인
  const hasRole = (role: string) => {
    return loginState.roleNames?.includes(role) || false;
  };

  return {
    loginState,
    loginStatus,
    isLoggedIn,
    doLogin,
    doLogout,
    moveToLogin,
    moveToLoginReturn,
    moveToPath,
    hasRole,
  };
}
