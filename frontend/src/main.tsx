import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router";
import { AppProvider, router } from "@/app";
import "./index.css";

// MSW 초기화 함수
async function enableMocking() {
  // VITE_USE_MOCK=true 일 때만 MSW 활성화 (기본값: false)
  if (import.meta.env.VITE_USE_MOCK === "true") {
    const { initMocks } = await import("./mocks");
    await initMocks();
  }
}

// MSW 초기화 후 앱 렌더링
enableMocking().then(() => {
  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      <AppProvider>
        <RouterProvider router={router} />
      </AppProvider>
    </StrictMode>
  );
});
