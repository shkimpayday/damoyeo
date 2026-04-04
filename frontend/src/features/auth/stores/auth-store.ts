import { create } from "zustand";
import { loginPost } from "../api";
import { setCookie, removeCookie, getCookie } from "@/lib/cookie";
import { queryClient } from "@/lib/react-query";
import type { MemberInfo } from "../types";

export interface AuthStore {
  member: MemberInfo;
  status: "" | "pending" | "fulfilled" | "error";
  login: (email: string, pw: string) => Promise<void>;
  logout: () => void;
  save: (memberInfo: MemberInfo) => void;
  updateProfile: (nickname: string, profileImage: string) => void;
}

const initState: MemberInfo = {
  id: undefined,
  email: "",
  nickname: "",
  profileImage: "",
  accessToken: "",
  refreshToken: "",
  roleNames: [],
  social: false,
};

// 쿠키에서 초기 상태 복원
const getInitialState = (): MemberInfo => {
  const cookieData = getCookie("member");
  if (cookieData && cookieData.email) {
    return cookieData;
  }
  return initState;
};

const initialMember = getInitialState();

export const useAuthStore = create<AuthStore>((set) => ({
  member: initialMember,
  status: initialMember.email ? "fulfilled" : "",

  login: async (email: string, pw: string) => {
    set({ status: "pending" });

    try {
      const data = await loginPost(email, pw);

      queryClient.clear();
      set({ member: { ...data }, status: "fulfilled" });

      // ✅ status 제거, 쿠키 만료 시간을 Refresh Token과 동일하게 (1일)
      setCookie("member", JSON.stringify(data), 1);
    } catch (error) {
      console.error("Login failed", error);
      set({ status: "error" });
      throw error;
    }
  },

  logout: () => {
    set({ member: { ...initState }, status: "" });
    removeCookie("member");
  },

  save: (memberInfo: MemberInfo) => {
    set({ member: memberInfo, status: "fulfilled" });
    // ✅ 쿠키도 함께 업데이트
    setCookie("member", JSON.stringify(memberInfo), 1);
  },

  updateProfile: (nickname: string, profileImage: string) => {
    set((state) => {
      const updated = { ...state.member, nickname, profileImage };
      setCookie("member", JSON.stringify(updated), 1);
      return { member: updated };
    });
  },
}));
