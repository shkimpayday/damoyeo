/**
 * ============================================================================
 * 모임 게시판 페이지
 * ============================================================================
 *
 * [역할]
 * 모임의 게시판을 전체 화면으로 제공합니다.
 * 가입인사, 모임후기, 자유게시글, 공지사항을 카테고리별로 확인하고
 * 새 게시글을 작성할 수 있습니다.
 *
 * [경로]
 * /groups/:groupId/board
 *
 * [권한]
 * 모임 멤버만 접근 가능 (비멤버는 안내 화면 표시)
 */

import { useParams, useNavigate } from "react-router";
import { ChevronLeft, StickyNote } from "lucide-react";
import { useGroupDetail } from "@/features/groups";
import { BoardList } from "@/features/board";
import { Spinner } from "@/components/ui";

export default function BoardPage() {
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
          <StickyNote size={48} className="mx-auto mb-4 text-gray-300" />
          <p className="text-lg font-semibold text-red-600">
            {!group ? "모임을 찾을 수 없습니다" : "게시판 접근 권한이 없습니다"}
          </p>
          <p className="mt-2 text-sm text-gray-600">
            {!group
              ? "존재하지 않는 모임입니다"
              : "모임 멤버만 게시판을 볼 수 있습니다"}
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

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 */}
      <div className="sticky top-0 z-20 bg-white border-b border-gray-100">
        <div className="flex items-center gap-3 px-4 py-3">
          <button
            onClick={() => navigate(`/groups/${groupId}`)}
            className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
          >
            <ChevronLeft size={24} className="text-gray-600" />
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="text-lg font-bold text-gray-900">게시판</h1>
            <p className="text-xs text-gray-500 truncate">{group.name}</p>
          </div>
        </div>
      </div>

      {/* 게시판 목록 */}
      <BoardList groupId={group.id} userRole={group.myRole} />
    </div>
  );
}
