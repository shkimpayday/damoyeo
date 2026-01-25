import { useState, useEffect, useRef } from "react";
import { REGIONS, SIDO_LIST } from "@/data/regions";

interface RegionSelectProps {
  value: string;
  onChange: (region: string) => void;
  label?: string;
  required?: boolean;
  placeholder?: string;
}

export function RegionSelect({
  value,
  onChange,
  label,
  required = false,
  placeholder = "지역을 선택하세요",
}: RegionSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedSido, setSelectedSido] = useState<string>("");
  const dropdownRef = useRef<HTMLDivElement>(null);

  // value가 변경되면 selectedSido 동기화
  useEffect(() => {
    if (value) {
      const sido = SIDO_LIST.find((s) => value.startsWith(s));
      if (sido) setSelectedSido(sido);
    } else {
      setSelectedSido("");
    }
  }, [value]);

  // 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSidoSelect = (sido: string) => {
    setSelectedSido(sido);
    // 세종시는 바로 선택 완료
    if (sido === "세종") {
      onChange("세종");
      setIsOpen(false);
    }
  };

  const handleSigunguSelect = (sigungu: string) => {
    onChange(`${selectedSido} ${sigungu}`);
    setIsOpen(false);
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange("");
    setSelectedSido("");
  };

  return (
    <div className="relative" ref={dropdownRef}>
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}

      {/* 선택 버튼 */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={`
          w-full px-4 py-3 border rounded-lg text-left flex items-center justify-between
          transition-all duration-200
          ${isOpen
            ? "border-primary-500 ring-2 ring-primary-100"
            : "border-gray-300 hover:border-gray-400"
          }
          ${value ? "text-gray-900" : "text-gray-400"}
        `}
      >
        <span className="flex items-center gap-2">
          {value ? (
            <>
              <span className="text-primary-500">📍</span>
              {value}
            </>
          ) : (
            placeholder
          )}
        </span>
        <span className="flex items-center gap-1">
          {value && (
            <span
              onClick={handleClear}
              className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            >
              ✕
            </span>
          )}
          <span className={`transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}>
            ▼
          </span>
        </span>
      </button>

      {/* 드롭다운 */}
      {isOpen && (
        <div className="absolute z-50 mt-2 w-full bg-white border border-gray-200 rounded-xl shadow-lg overflow-hidden animate-in fade-in slide-in-from-top-2 duration-200">
          <div className="flex h-72">
            {/* 시/도 목록 */}
            <div className="w-1/3 border-r border-gray-100 overflow-y-auto">
              <div className="sticky top-0 bg-gray-50 px-3 py-2 text-xs font-medium text-gray-500 border-b">
                시/도
              </div>
              {SIDO_LIST.map((sido) => (
                <button
                  key={sido}
                  type="button"
                  onClick={() => handleSidoSelect(sido)}
                  className={`
                    w-full px-3 py-2.5 text-left text-sm transition-colors
                    ${selectedSido === sido
                      ? "bg-primary-50 text-primary-600 font-medium"
                      : "hover:bg-gray-50 text-gray-700"
                    }
                  `}
                >
                  {sido}
                </button>
              ))}
            </div>

            {/* 시/군/구 목록 */}
            <div className="w-2/3 overflow-y-auto">
              <div className="sticky top-0 bg-gray-50 px-3 py-2 text-xs font-medium text-gray-500 border-b">
                {selectedSido ? `${selectedSido}의 시/군/구` : "시/도를 먼저 선택하세요"}
              </div>
              {selectedSido && selectedSido !== "세종" ? (
                <div className="p-2 grid grid-cols-2 gap-1">
                  {REGIONS[selectedSido]?.map((sigungu) => (
                    <button
                      key={sigungu}
                      type="button"
                      onClick={() => handleSigunguSelect(sigungu)}
                      className={`
                        px-3 py-2 text-left text-sm rounded-lg transition-colors
                        ${value === `${selectedSido} ${sigungu}`
                          ? "bg-primary-500 text-white"
                          : "hover:bg-gray-100 text-gray-700"
                        }
                      `}
                    >
                      {sigungu}
                    </button>
                  ))}
                </div>
              ) : selectedSido === "세종" ? (
                <div className="flex items-center justify-center h-full text-gray-400 text-sm">
                  세종특별자치시가 선택되었습니다
                </div>
              ) : (
                <div className="flex items-center justify-center h-full text-gray-400 text-sm">
                  ← 시/도를 선택해주세요
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
