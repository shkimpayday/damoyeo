/**
 * ============================================================================
 * 관리자 이벤트/배너 관리 페이지
 * ============================================================================
 *
 * [기능]
 * - 이벤트 목록 조회 (전체)
 * - 이벤트 생성 (제목/설명/이미지URL/링크URL/타입/기간/노출순서)
 * - 이벤트 활성화/비활성화 토글 (메인 배너에 노출 여부 제어)
 * - 이벤트 삭제
 *
 * [API 엔드포인트]
 * - GET    /api/events          전체 목록 (관리자용)
 * - POST   /api/events          이벤트 생성
 * - PATCH  /api/events/{id}/toggle  활성화 토글
 * - DELETE /api/events/{id}     이벤트 삭제
 *
 * @author damoyeo
 * @since 2025-03-18
 */

import { useState, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { jwtAxios } from "@/lib/axios";
import { Spinner } from "@/components/ui";
import { Eye, EyeOff, Trash2, Plus, ExternalLink, X, Pencil } from "lucide-react";
import { formatDate } from "@/utils/date";

// ============================================================================
// 타입 정의
// ============================================================================

type EventType = "PROMOTION" | "NOTICE" | "SPECIAL" | "FEATURE";

/**
 * 관리자용 이벤트 DTO
 */
interface AdminEventDTO {
  id: number;
  title: string;
  description: string;
  content?: string;
  imageUrl: string;
  linkUrl: string | null;
  type: EventType;
  startDate: string;
  endDate: string;
  isActive: boolean;
  displayOrder: number;
  tags?: string[];
  createdAt: string;
}

/**
 * 이벤트 생성 요청
 */
interface EventCreateRequest {
  title: string;
  description: string;
  imageUrl: string;
  linkUrl?: string;
  type: EventType;
  startDate: string;
  endDate: string;
  displayOrder: number;
}

/**
 * 이벤트 수정 요청
 */
interface EventUpdateRequest {
  title: string;
  description: string;
  content?: string;
  imageUrl: string;
  linkUrl?: string;
  type: EventType;
  startDate: string;
  endDate: string;
  displayOrder: number;
}

// ============================================================================
// API 함수
// ============================================================================

/**
 * 전체 이벤트 목록 조회 (관리자용)
 *
 * 비활성 이벤트도 포함하여 전체 목록을 반환합니다.
 */
const fetchAllEvents = async (): Promise<AdminEventDTO[]> => {
  const res = await jwtAxios.get("/api/events");
  return res.data;
};

/**
 * 이벤트 생성
 */
const createEvent = async (request: EventCreateRequest): Promise<{ id: number }> => {
  // 날짜를 ISO 8601 형식으로 변환 (date input은 yyyy-MM-dd 형식)
  const payload = {
    ...request,
    startDate: `${request.startDate}T00:00:00`,
    endDate: `${request.endDate}T23:59:59`,
  };
  const res = await jwtAxios.post("/api/events", payload);
  return res.data;
};

/**
 * 이벤트 활성화/비활성화 토글
 */
const toggleEvent = async (eventId: number): Promise<void> => {
  await jwtAxios.patch(`/api/events/${eventId}/toggle`);
};

/**
 * 이벤트 수정
 */
const updateEvent = async ({
  eventId,
  request,
}: {
  eventId: number;
  request: EventUpdateRequest;
}): Promise<EventUpdateRequest> => {
  const payload = {
    ...request,
    startDate: `${request.startDate}T00:00:00`,
    endDate: `${request.endDate}T23:59:59`,
  };
  const res = await jwtAxios.put(`/api/events/${eventId}`, payload);
  return res.data;
};

/**
 * 이벤트 삭제
 */
const deleteEvent = async (eventId: number): Promise<void> => {
  await jwtAxios.delete(`/api/events/${eventId}`);
};

// ============================================================================
// 하위 컴포넌트
// ============================================================================

/**
 * 이벤트의 실제 배너 노출 상태를 계산합니다.
 *
 * 백엔드 findActiveBanners() 쿼리 조건과 동일하게 3가지를 모두 확인합니다:
 * 1. isActive = true
 * 2. startDate <= 현재시간
 * 3. endDate >= 현재시간
 */
function getBannerDisplayStatus(event: AdminEventDTO): {
  status: "displaying" | "scheduled" | "expired" | "inactive";
  label: string;
  className: string;
} {
  if (!event.isActive) {
    return { status: "inactive", label: "○ 비활성", className: "bg-gray-100 text-gray-500" };
  }

  const now = new Date();
  const start = new Date(event.startDate);
  const end = new Date(event.endDate);

  if (start > now) {
    return { status: "scheduled", label: "◷ 시작 전", className: "bg-blue-100 text-blue-600" };
  }
  if (end < now) {
    return { status: "expired", label: "✕ 기간 만료", className: "bg-orange-100 text-orange-600" };
  }
  return { status: "displaying", label: "● 노출 중", className: "bg-green-100 text-green-700" };
}

/**
 * 이벤트 타입 뱃지
 */
function TypeBadge({ type }: { type: EventType }) {
  const config: Record<EventType, { bg: string; text: string; label: string }> = {
    PROMOTION: { bg: "bg-purple-100", text: "text-purple-700", label: "프로모션" },
    NOTICE: { bg: "bg-blue-100", text: "text-blue-700", label: "공지" },
    SPECIAL: { bg: "bg-yellow-100", text: "text-yellow-700", label: "특별" },
    FEATURE: { bg: "bg-green-100", text: "text-green-700", label: "신기능" },
  };
  const c = config[type] ?? config.PROMOTION;
  return (
    <span className={`px-2 py-0.5 text-xs rounded-full font-medium ${c.bg} ${c.text}`}>
      {c.label}
    </span>
  );
}

/**
 * 이벤트 생성 모달
 *
 * 이벤트 정보를 입력받아 생성 요청을 부모에게 전달합니다.
 * 이미지 URL 입력 시 실시간 미리보기를 제공합니다.
 */
function CreateEventModal({
  isOpen,
  onClose,
  onSubmit,
  isLoading,
}: {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: EventCreateRequest) => void;
  isLoading: boolean;
}) {
  const [formData, setFormData] = useState<EventCreateRequest>({
    title: "",
    description: "",
    imageUrl: "",
    linkUrl: "",
    type: "PROMOTION",
    startDate: "",
    endDate: "",
    displayOrder: 0,
  });
  const [imagePreviewError, setImagePreviewError] = useState(false);

  if (!isOpen) return null;

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "displayOrder" ? Number(value) : value,
    }));
    if (name === "imageUrl") setImagePreviewError(false);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (new Date(formData.endDate) < new Date(formData.startDate)) {
      alert("종료일은 시작일 이후여야 합니다.");
      return;
    }
    onSubmit(formData);
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        {/* 모달 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h2 className="text-lg font-bold text-gray-900">새 이벤트 생성</h2>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X size={18} className="text-gray-500" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* 제목 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              제목 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="이벤트 제목을 입력하세요"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              maxLength={100}
              required
            />
          </div>

          {/* 설명 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              설명 <span className="text-red-500">*</span>
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="배너에 표시될 짧은 설명을 입력하세요"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400 resize-none"
              rows={2}
              maxLength={200}
              required
            />
          </div>

          {/* 이미지 URL + 미리보기 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              배너 이미지 URL <span className="text-red-500">*</span>
            </label>
            <input
              type="url"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/banner.jpg"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              required
            />
            {/* 이미지 미리보기 */}
            {formData.imageUrl && !imagePreviewError && (
              <div className="mt-2 rounded-lg overflow-hidden border border-gray-100 bg-gray-50 h-24">
                <img
                  src={formData.imageUrl}
                  alt="미리보기"
                  className="w-full h-full object-cover"
                  onError={() => setImagePreviewError(true)}
                />
              </div>
            )}
            {imagePreviewError && (
              <p className="mt-1 text-xs text-red-500">이미지를 불러올 수 없습니다. URL을 확인해주세요.</p>
            )}
            <p className="mt-1 text-xs text-gray-400">권장 비율: 2:1 (예: 800×400)</p>
          </div>

          {/* 링크 URL */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              클릭 링크 URL <span className="text-gray-400 font-normal">(선택)</span>
            </label>
            <input
              type="text"
              name="linkUrl"
              value={formData.linkUrl}
              onChange={handleChange}
              placeholder="https://example.com 또는 /events/1"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
            />
          </div>

          {/* 타입 + 노출 순서 */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                타입 <span className="text-red-500">*</span>
              </label>
              <select
                name="type"
                value={formData.type}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              >
                <option value="PROMOTION">프로모션</option>
                <option value="NOTICE">공지</option>
                <option value="SPECIAL">특별 이벤트</option>
                <option value="FEATURE">신기능 소개</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                노출 순서
              </label>
              <input
                type="number"
                name="displayOrder"
                value={formData.displayOrder}
                onChange={handleChange}
                min={0}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              />
              <p className="mt-0.5 text-xs text-gray-400">숫자가 낮을수록 먼저 표시</p>
            </div>
          </div>

          {/* 시작일 ~ 종료일 */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                시작일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                종료일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                min={formData.startDate}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
                required
              />
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 text-sm bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50 flex items-center gap-2"
            >
              {isLoading ? (
                <>
                  <Spinner size="sm" />
                  생성 중...
                </>
              ) : (
                <>
                  <Plus size={16} />
                  이벤트 생성
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

/**
 * ISO datetime 문자열을 date input 형식(yyyy-MM-dd)으로 변환
 */
function toDateInputValue(isoString: string): string {
  if (!isoString) return "";
  return isoString.split("T")[0];
}

/**
 * 이벤트 수정 모달
 *
 * 기존 이벤트 데이터로 폼을 초기화하여 수정 요청을 부모에게 전달합니다.
 */
function EditEventModal({
  event,
  onClose,
  onSubmit,
  isLoading,
}: {
  event: AdminEventDTO;
  onClose: () => void;
  onSubmit: (data: EventUpdateRequest) => void;
  isLoading: boolean;
}) {
  const [formData, setFormData] = useState<EventUpdateRequest>({
    title: event.title,
    description: event.description,
    content: event.content ?? "",
    imageUrl: event.imageUrl,
    linkUrl: event.linkUrl ?? "",
    type: event.type,
    startDate: toDateInputValue(event.startDate),
    endDate: toDateInputValue(event.endDate),
    displayOrder: event.displayOrder,
  });
  const [imagePreviewError, setImagePreviewError] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "displayOrder" ? Number(value) : value,
    }));
    if (name === "imageUrl") setImagePreviewError(false);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (new Date(formData.endDate) < new Date(formData.startDate)) {
      alert("종료일은 시작일 이후여야 합니다.");
      return;
    }
    onSubmit(formData);
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        {/* 모달 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h2 className="text-lg font-bold text-gray-900">이벤트 수정</h2>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X size={18} className="text-gray-500" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* 제목 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              제목 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="이벤트 제목을 입력하세요"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              maxLength={100}
              required
            />
          </div>

          {/* 설명 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              설명 <span className="text-red-500">*</span>
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="배너에 표시될 짧은 설명을 입력하세요"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400 resize-none"
              rows={2}
              maxLength={200}
              required
            />
          </div>

          {/* 본문 내용 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              본문 내용 <span className="text-gray-400 font-normal">(선택 · Markdown 지원)</span>
            </label>
            <textarea
              name="content"
              value={formData.content}
              onChange={handleChange}
              placeholder={"## 이벤트 상세 내용\n\n이벤트에 대한 자세한 내용을 Markdown으로 작성하세요."}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400 resize-y font-mono"
              rows={6}
            />
            <p className="mt-1 text-xs text-gray-400">이벤트 상세 페이지에 표시됩니다. Markdown 문법 사용 가능.</p>
          </div>

          {/* 이미지 URL + 미리보기 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              배너 이미지 URL <span className="text-red-500">*</span>
            </label>
            <input
              type="url"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/banner.jpg"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              required
            />
            {formData.imageUrl && !imagePreviewError && (
              <div className="mt-2 rounded-lg overflow-hidden border border-gray-100 bg-gray-50 h-24">
                <img
                  src={formData.imageUrl}
                  alt="미리보기"
                  className="w-full h-full object-cover"
                  onError={() => setImagePreviewError(true)}
                />
              </div>
            )}
            {imagePreviewError && (
              <p className="mt-1 text-xs text-red-500">이미지를 불러올 수 없습니다. URL을 확인해주세요.</p>
            )}
            <p className="mt-1 text-xs text-gray-400">권장 비율: 2:1 (예: 800×400)</p>
          </div>

          {/* 링크 URL */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              클릭 링크 URL <span className="text-gray-400 font-normal">(선택)</span>
            </label>
            <input
              type="text"
              name="linkUrl"
              value={formData.linkUrl}
              onChange={handleChange}
              placeholder="https://example.com 또는 /events/1"
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
            />
          </div>

          {/* 타입 + 노출 순서 */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                타입 <span className="text-red-500">*</span>
              </label>
              <select
                name="type"
                value={formData.type}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              >
                <option value="PROMOTION">프로모션</option>
                <option value="NOTICE">공지</option>
                <option value="SPECIAL">특별 이벤트</option>
                <option value="FEATURE">신기능 소개</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                노출 순서
              </label>
              <input
                type="number"
                name="displayOrder"
                value={formData.displayOrder}
                onChange={handleChange}
                min={0}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
              />
              <p className="mt-0.5 text-xs text-gray-400">숫자가 낮을수록 먼저 표시</p>
            </div>
          </div>

          {/* 시작일 ~ 종료일 */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                시작일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                종료일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                min={formData.startDate}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-400"
                required
              />
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 text-sm bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50 flex items-center gap-2"
            >
              {isLoading ? (
                <>
                  <Spinner size="sm" />
                  저장 중...
                </>
              ) : (
                <>
                  <Pencil size={16} />
                  수정 저장
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ============================================================================
// 메인 페이지
// ============================================================================

/**
 * 관리자 이벤트/배너 관리 페이지
 */
export default function EventsPage() {
  const queryClient = useQueryClient();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<AdminEventDTO | null>(null);

  // ========================================================================
  // Query
  // ========================================================================

  /**
   * 전체 이벤트 목록 (비활성 포함)
   */
  const {
    data: events,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["admin", "events"],
    queryFn: fetchAllEvents,
    staleTime: 30 * 1000,
  });

  // ========================================================================
  // Mutations
  // ========================================================================

  /**
   * 이벤트 생성
   */
  const createMutation = useMutation({
    mutationFn: createEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "events"] });
      // 메인 배너 캐시도 무효화 (메인 페이지에서 즉시 반영)
      queryClient.invalidateQueries({ queryKey: ["events", "banners"] });
      setIsCreateModalOpen(false);
    },
    onError: (err) => {
      console.error("이벤트 생성 실패:", err);
      alert("이벤트 생성에 실패했습니다. 다시 시도해주세요.");
    },
  });

  /**
   * 활성화/비활성화 토글
   *
   * 토글 성공 시 메인 배너 캐시도 무효화합니다.
   */
  const toggleMutation = useMutation({
    mutationFn: toggleEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "events"] });
      queryClient.invalidateQueries({ queryKey: ["events", "banners"] });
    },
    onError: () => {
      alert("상태 변경에 실패했습니다.");
    },
  });

  /**
   * 이벤트 수정
   */
  const updateMutation = useMutation({
    mutationFn: updateEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "events"] });
      queryClient.invalidateQueries({ queryKey: ["events", "banners"] });
      setEditingEvent(null);
    },
    onError: () => {
      alert("이벤트 수정에 실패했습니다. 다시 시도해주세요.");
    },
  });

  /**
   * 이벤트 삭제
   */
  const deleteMutation = useMutation({
    mutationFn: deleteEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "events"] });
      queryClient.invalidateQueries({ queryKey: ["events", "banners"] });
    },
    onError: () => {
      alert("삭제에 실패했습니다.");
    },
  });

  // ========================================================================
  // 핸들러
  // ========================================================================

  const handleCreate = useCallback(
    (data: EventCreateRequest) => {
      createMutation.mutate(data);
    },
    [createMutation]
  );

  const handleToggle = useCallback(
    (eventId: number) => {
      toggleMutation.mutate(eventId);
    },
    [toggleMutation]
  );

  const handleUpdate = useCallback(
    (data: EventUpdateRequest) => {
      if (!editingEvent) return;
      updateMutation.mutate({ eventId: editingEvent.id, request: data });
    },
    [updateMutation, editingEvent]
  );

  const handleDelete = useCallback(
    (eventId: number, title: string) => {
      if (confirm(`"${title}" 이벤트를 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.`)) {
        deleteMutation.mutate(eventId);
      }
    },
    [deleteMutation]
  );

  // ========================================================================
  // 렌더링
  // ========================================================================

  const totalCount = events?.length ?? 0;
  // 현재 실제로 메인 배너에 노출 중인 이벤트 수 (3가지 조건 모두 충족)
  const displayingCount =
    events?.filter((e) => getBannerDisplayStatus(e).status === "displaying").length ?? 0;

  return (
    <div>
      {/* 페이지 헤더 */}
      <div className="flex items-start justify-between gap-3 mb-6">
        <div className="min-w-0">
          <h1 className="text-2xl font-bold text-gray-800">이벤트/배너 관리</h1>
          <p className="text-gray-500 mt-1 text-sm">
            메인 화면의 배너 슬라이더를 관리합니다
            {totalCount > 0 && (
              <span className="ml-2 font-medium">
                <span className="text-green-600">현재 노출 중 {displayingCount}개</span>
                <span className="text-gray-400"> / 전체 {totalCount}개</span>
              </span>
            )}
          </p>
        </div>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="shrink-0 flex items-center gap-2 px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors text-sm font-medium whitespace-nowrap"
        >
          <Plus size={16} />
          새 이벤트
        </button>
      </div>

      {/* API 에러 표시 */}
      {isError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 text-sm text-red-700">
          이벤트 목록을 불러오지 못했습니다.{" "}
          {error instanceof Error ? error.message : "서버 오류가 발생했습니다."}
        </div>
      )}

      {/* 이벤트 목록 테이블 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center h-64">
            <Spinner size="md" />
          </div>
        ) : (
          <div className="overflow-x-auto">
          <table className="w-full min-w-200">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  이벤트
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  타입
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  기간
                </th>
                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                  순서
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  상태
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  액션
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {events?.map((event) => (
                <tr
                  key={event.id}
                  className="hover:bg-gray-50 transition-colors"
                >
                  {/* 이벤트 정보 */}
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="w-20 h-12 rounded-lg overflow-hidden bg-gray-100 shrink-0">
                        <img
                          src={event.imageUrl}
                          alt={event.title}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src =
                              "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='48' fill='%23e5e7eb'%3E%3Crect width='80' height='48'/%3E%3C/svg%3E";
                          }}
                        />
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="font-medium text-gray-900 text-sm truncate max-w-40">
                            {event.title}
                          </p>
                          {event.linkUrl && (
                            <a
                              href={event.linkUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-gray-400 hover:text-primary-500 shrink-0"
                              title="링크 열기"
                            >
                              <ExternalLink size={12} />
                            </a>
                          )}
                        </div>
                        <p className="text-xs text-gray-400 line-clamp-1 mt-0.5">
                          {event.description}
                        </p>
                      </div>
                    </div>
                  </td>

                  {/* 타입 */}
                  <td className="px-6 py-4 whitespace-nowrap">
                    <TypeBadge type={event.type} />
                  </td>

                  {/* 기간 */}
                  <td className="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">
                    <div>{formatDate(event.startDate)}</div>
                    <div className="text-xs text-gray-400">~ {formatDate(event.endDate)}</div>
                  </td>

                  {/* 노출 순서 */}
                  <td className="px-6 py-4 text-center whitespace-nowrap">
                    <span className="text-sm font-medium text-gray-700 bg-gray-100 px-2 py-0.5 rounded">
                      {event.displayOrder}
                    </span>
                  </td>

                  {/* 상태 - 실제 배너 노출 여부 (isActive + 날짜 범위 모두 체크) */}
                  <td className="px-6 py-4 whitespace-nowrap">
                    {(() => {
                      const { label, className } = getBannerDisplayStatus(event);
                      return (
                        <span className={`px-2 py-0.5 text-xs rounded-full font-medium whitespace-nowrap ${className}`}>
                          {label}
                        </span>
                      );
                    })()}
                  </td>

                  {/* 액션 버튼 */}
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center justify-end gap-2">
                      {/* 수정 */}
                      <button
                        onClick={() => setEditingEvent(event)}
                        className="flex items-center gap-1 px-3 py-1.5 text-xs bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 transition-colors font-medium"
                        title="이벤트 수정"
                      >
                        <Pencil size={12} />
                        수정
                      </button>

                      {/* 활성화/비활성화 토글 */}
                      <button
                        onClick={() => handleToggle(event.id)}
                        disabled={toggleMutation.isPending}
                        className={`flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg transition-colors font-medium ${
                          event.isActive
                            ? "bg-gray-100 text-gray-600 hover:bg-gray-200"
                            : "bg-green-100 text-green-700 hover:bg-green-200"
                        } disabled:opacity-50`}
                        title={event.isActive ? "배너에서 숨기기" : "배너에 노출하기"}
                      >
                        {event.isActive ? (
                          <>
                            <EyeOff size={12} />
                            비활성화
                          </>
                        ) : (
                          <>
                            <Eye size={12} />
                            활성화
                          </>
                        )}
                      </button>

                      {/* 삭제 */}
                      <button
                        onClick={() => handleDelete(event.id, event.title)}
                        disabled={deleteMutation.isPending}
                        className="flex items-center gap-1 px-3 py-1.5 text-xs bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors font-medium disabled:opacity-50"
                        title="이벤트 삭제"
                      >
                        <Trash2 size={12} />
                        삭제
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {/* 빈 상태 */}
              {(!events || events.length === 0) && (
                <tr>
                  <td colSpan={6} className="px-6 py-16 text-center">
                    <div className="text-gray-400">
                      <div className="text-4xl mb-3">🎪</div>
                      <p className="font-medium">등록된 이벤트가 없습니다</p>
                      <p className="text-sm mt-1">새 이벤트 버튼을 눌러 배너를 추가하세요</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          </div>
        )}
      </div>

      {/* 상태 설명 */}
      <div className="mt-4 p-4 bg-blue-50 rounded-lg text-sm text-blue-800">
        <strong>배너 노출 조건 (3가지 모두 충족 시 노출):</strong>
        <div className="mt-2 flex flex-wrap gap-3">
          <span className="flex items-center gap-1.5">
            <span className="px-2 py-0.5 text-xs rounded-full bg-green-100 text-green-700 font-medium">● 노출 중</span>
            활성화 + 기간 내
          </span>
          <span className="flex items-center gap-1.5">
            <span className="px-2 py-0.5 text-xs rounded-full bg-blue-100 text-blue-600 font-medium">◷ 시작 전</span>
            활성화 + 시작일 미도래
          </span>
          <span className="flex items-center gap-1.5">
            <span className="px-2 py-0.5 text-xs rounded-full bg-orange-100 text-orange-600 font-medium">✕ 기간 만료</span>
            활성화 + 종료일 경과
          </span>
          <span className="flex items-center gap-1.5">
            <span className="px-2 py-0.5 text-xs rounded-full bg-gray-100 text-gray-500 font-medium">○ 비활성</span>
            수동으로 비활성화됨
          </span>
        </div>
      </div>

      {/* 이벤트 생성 모달 */}
      <CreateEventModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreate}
        isLoading={createMutation.isPending}
      />

      {/* 이벤트 수정 모달 */}
      {editingEvent && (
        <EditEventModal
          event={editingEvent}
          onClose={() => setEditingEvent(null)}
          onSubmit={handleUpdate}
          isLoading={updateMutation.isPending}
        />
      )}
    </div>
  );
}
