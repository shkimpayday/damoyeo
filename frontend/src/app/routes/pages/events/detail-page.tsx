import { useParams, Link } from "react-router";
import { useEventDetail } from "@/features/events";
import { useAuth } from "@/features/auth";
import { Spinner } from "@/components/ui";

function EventDetailPage() {
  const { eventId } = useParams();
  const { data: event, isLoading, error } = useEventDetail(Number(eventId));
  const { isLoggedIn } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <Spinner />
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="flex flex-col items-center justify-center min-h-96 p-4">
        <span className="text-4xl mb-4">😢</span>
        <h2 className="text-lg font-bold text-gray-900">
          이벤트를 찾을 수 없습니다
        </h2>
        <p className="text-gray-500 mt-2">
          존재하지 않거나 종료된 이벤트입니다.
        </p>
        <Link
          to="/"
          className="mt-4 px-4 py-2 bg-primary-500 text-white rounded-lg"
        >
          홈으로 돌아가기
        </Link>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <div className="pb-8">
      {/* 이벤트 이미지 */}
      <div className="relative aspect-[5/1]">
        <img
          src={event.imageUrl}
          alt={event.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
      </div>

      {/* 이벤트 정보 */}
      <div className="app-content">
        <div className="py-4">
        {/* 태그 */}
        {event.tags && event.tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-3">
            {event.tags.map((tag) => (
              <span
                key={tag}
                className="px-2 py-1 text-xs bg-primary-50 text-primary-600 rounded-full"
              >
                #{tag}
              </span>
            ))}
          </div>
        )}

        {/* 제목 */}
        <h1 className="text-2xl font-bold text-gray-900">{event.title}</h1>
        <p className="text-gray-600 mt-2">{event.description}</p>

        {/* 기간 */}
        <div className="mt-4 p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <svg
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span>
              {formatDate(event.startDate)} ~ {formatDate(event.endDate)}
            </span>
          </div>
        </div>

        {/* 상세 내용 */}
        <div className="mt-6 prose prose-sm max-w-none">
          <div
            className="text-gray-700 whitespace-pre-line"
            dangerouslySetInnerHTML={{
              __html: event.content
                .replace(/^## (.+)$/gm, '<h2 class="text-xl font-bold mt-6 mb-3">$1</h2>')
                .replace(/^### (.+)$/gm, '<h3 class="text-lg font-semibold mt-4 mb-2">$1</h3>')
                .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
                .replace(/^- (.+)$/gm, '<li class="ml-4">$1</li>')
                .replace(/^\d+\. (.+)$/gm, '<li class="ml-4 list-decimal">$1</li>')
            }}
          />
        </div>
        </div>
      </div>

      {/* 하단 CTA - 비로그인 사용자에게만 표시 */}
      {!isLoggedIn && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 w-full max-w-[calc(80%-2rem)] px-4">
          <Link
            to="/member/login"
            className="block w-full py-3 bg-primary-500 text-white text-center font-medium rounded-xl shadow-lg"
          >
            다모여 시작하기
          </Link>
        </div>
      )}
    </div>
  );
}

export default EventDetailPage;
