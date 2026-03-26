import { useState, useCallback } from "react";

interface LocationState {
  lat: number | null;
  lng: number | null;
  error: string | null;
  loading: boolean;
}

const LOCATION_STORAGE_KEY = "damoyeo_user_location";

// sessionStorage에서 위치 복원
function getStoredLocation(): { lat: number; lng: number } | null {
  try {
    const stored = sessionStorage.getItem(LOCATION_STORAGE_KEY);
    if (stored) {
      const parsed = JSON.parse(stored);
      // 저장된 지 30분 이내인 경우만 사용
      if (Date.now() - parsed.timestamp < 30 * 60 * 1000) {
        return { lat: parsed.lat, lng: parsed.lng };
      }
      sessionStorage.removeItem(LOCATION_STORAGE_KEY);
    }
  } catch {
    // 파싱 실패 시 무시
  }
  return null;
}

// sessionStorage에 위치 저장
function storeLocation(lat: number, lng: number) {
  try {
    sessionStorage.setItem(
      LOCATION_STORAGE_KEY,
      JSON.stringify({ lat, lng, timestamp: Date.now() })
    );
  } catch {
    // 저장 실패 시 무시
  }
}

/**
 * Geolocation API를 사용하여 현재 위치를 가져오는 Hook
 * - 세션 스토리지에 위치를 저장하여 새로고침 시에도 유지 (30분간)
 */
export function useCurrentLocation() {
  const storedLocation = getStoredLocation();

  const [location, setLocation] = useState<LocationState>({
    lat: storedLocation?.lat ?? null,
    lng: storedLocation?.lng ?? null,
    error: null,
    loading: false,
  });

  const requestLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setLocation((prev) => ({
        ...prev,
        error: "이 브라우저에서는 위치 서비스를 지원하지 않습니다.",
      }));
      return;
    }

    setLocation((prev) => ({ ...prev, loading: true, error: null }));

    // 먼저 빠른 저정밀도 위치로 시도, 실패 시 고정밀도로 재시도
    const tryGetPosition = (highAccuracy: boolean) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          storeLocation(latitude, longitude);
          setLocation({
            lat: latitude,
            lng: longitude,
            error: null,
            loading: false,
          });
        },
        (error) => {
          // 저정밀도로 실패했고 아직 고정밀도 시도 안했으면 재시도
          if (!highAccuracy && error.code === error.TIMEOUT) {
            tryGetPosition(true);
            return;
          }

          let errorMessage = "위치를 가져올 수 없습니다.";
          switch (error.code) {
            case error.PERMISSION_DENIED:
              errorMessage = "위치 권한이 거부되었습니다. 브라우저 설정에서 허용해주세요.";
              break;
            case error.POSITION_UNAVAILABLE:
              errorMessage = "위치 정보를 사용할 수 없습니다.";
              break;
            case error.TIMEOUT:
              errorMessage = "위치 요청 시간이 초과되었습니다. 브라우저의 위치 권한을 확인해주세요.";
              break;
          }
          setLocation({
            lat: null,
            lng: null,
            error: errorMessage,
            loading: false,
          });
        },
        {
          enableHighAccuracy: highAccuracy,
          timeout: highAccuracy ? 15000 : 5000,
          maximumAge: 300000, // 5분 캐시
        }
      );
    };

    // 저정밀도(IP 기반)로 먼저 시도
    tryGetPosition(false);
  }, []);

  const clearLocation = useCallback(() => {
    sessionStorage.removeItem(LOCATION_STORAGE_KEY);
    setLocation({
      lat: null,
      lng: null,
      error: null,
      loading: false,
    });
  }, []);

  return {
    ...location,
    requestLocation,
    clearLocation,
    hasLocation: location.lat !== null && location.lng !== null,
  };
}
