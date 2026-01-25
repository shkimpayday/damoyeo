import { create } from "zustand";
import { loginPost } from "../api";
import { setCookie, removeCookie, getCookie } from "@/lib/cookie";
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

      set({ member: { ...data }, status: "fulfilled" });

      const newState = { ...data, status: "fulfilled" };
      setCookie("member", JSON.stringify(newState), 1); // 1일
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
  },

  updateProfile: (nickname: string, profileImage: string) => {
    set((state) => ({
      member: {
        ...state.member,
        nickname,
        profileImage,
      },
    }));
  },
}));
