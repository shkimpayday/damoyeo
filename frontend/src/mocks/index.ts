// MSW 초기화
export async function initMocks() {
  if (import.meta.env.DEV) {
    const { worker } = await import("./browser");

    // MSW 워커 시작
    await worker.start({
      onUnhandledRequest: "bypass", // 처리되지 않은 요청은 그대로 통과
      serviceWorker: {
        url: "/mockServiceWorker.js",
      },
    });

    console.log("[MSW] Mock Service Worker가 활성화되었습니다.");
  }
}
