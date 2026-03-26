/**
 * ============================================================================
 * 모임 채팅 페이지
 * ============================================================================
 *
 * [역할]
 * 모임의 채팅방을 전체 화면으로 제공합니다.
 *
 * [경로]
 * /groups/:groupId/chat
 *
 * [권한]
 * 모임 멤버만 접근 가능 (비멤버는 리다이렉트)
 *
 * @author damoyeo
 * @since 2025-02-25
 */

import { useParams, useNavigate } from "react-router";
import { useGroupDetail } from "@/features/groups";
import { ChatRoom } from "@/features/chat";
import { Spinner } from "@/components/ui";

export default function ChatPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();

  const { data: group, isLoading } = useGroupDetail(Number(groupId));

  // 로딩 중
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Spinner size="lg" />
      </div>
    );
  }

  // 모임을 찾을 수 없거나 멤버가 아닌 경우
  if (!group || !group.myRole) {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 px-4">
        <div className="text-center">
          <p className="text-lg font-semibold text-red-600">
            {!group ? "모임을 찾을 수 없습니다" : "채팅 접근 권한이 없습니다"}
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {!group
              ? "존재하지 않는 모임입니다"
              : "모임 멤버만 채팅에 참여할 수 있습니다"}
          </p>
        </div>
        <button
          onClick={() => navigate(`/groups/${groupId}`)}
          className="rounded-lg bg-gray-200 px-4 py-2 text-gray-700 hover:bg-gray-300"
        >
          모임으로 돌아가기
        </button>
      </div>
    );
  }

  // 채팅방 렌더링
  // h-[calc(100vh-7.5rem)]: 헤더(3.5rem) + 하단네비(4rem) 제외한 높이
  return (
    <div className="h-[calc(100vh-7.5rem)] flex flex-col">
      <ChatRoom
        groupId={group.id}
        groupName={group.name}
        onBack={() => navigate(`/groups/${groupId}`)}
      />
    </div>
  );
}
