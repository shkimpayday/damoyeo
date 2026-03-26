/**
 * ============================================================================
 * 정모 채팅 페이지
 * ============================================================================
 *
 * [역할]
 * 정모 참석자 전용 채팅방을 전체 화면으로 제공합니다.
 *
 * [경로]
 * /meetings/:meetingId/chat
 *
 * [권한]
 * ATTENDING 상태의 참석자만 접근 가능합니다.
 *
 * @author damoyeo
 * @since 2025-03-16
 */

import { useParams, useNavigate } from "react-router";
import { useMeetingDetail } from "@/features/meetings";
import { MeetingChatRoom } from "@/features/chat";
import { Spinner } from "@/components/ui";

export default function MeetingChatPage() {
  const { meetingId } = useParams<{ meetingId: string }>();
  const navigate = useNavigate();

  const { data: meeting, isLoading } = useMeetingDetail(Number(meetingId));

  // 로딩 중
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Spinner size="lg" />
      </div>
    );
  }

  // 정모를 찾을 수 없거나 참석자가 아닌 경우
  if (!meeting || meeting.myStatus !== "ATTENDING") {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 px-4">
        <div className="text-center">
          <p className="text-lg font-semibold text-red-600">
            {!meeting ? "정모를 찾을 수 없습니다" : "채팅 접근 권한이 없습니다"}
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {!meeting
              ? "존재하지 않는 정모입니다"
              : "정모 참석자만 채팅에 참여할 수 있습니다"}
          </p>
        </div>
        <button
          onClick={() => navigate(`/meetings/${meetingId}`)}
          className="rounded-lg bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
        >
          정모로 돌아가기
        </button>
      </div>
    );
  }

  // 채팅방 렌더링
  return (
    <div className="h-[calc(100vh-7.5rem)] flex flex-col">
      <MeetingChatRoom
        meetingId={meeting.id}
        meetingTitle={meeting.title}
        onBack={() => navigate(`/meetings/${meetingId}`)}
      />
    </div>
  );
}
