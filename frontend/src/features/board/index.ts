/**
 * 모임 게시판 피처 모듈
 *
 * [Public API]
 * - BoardList: 게시판 목록 메인 컴포넌트
 * - PostCard: 게시글 카드 컴포넌트
 * - PostDetailModal: 게시글 상세 모달
 * - PostCreateModal: 게시글 작성 모달
 * - types: 타입 정의
 */

// Components
export { BoardList } from "./components/board-list";
export { PostCard } from "./components/post-card";
export { PostDetailModal } from "./components/post-detail-modal";
export { PostCreateModal } from "./components/post-create-modal";

// Hooks
export {
  useBoardPostsInfinite,
  useBoardPost,
  useCreateBoardPost,
  useDeleteBoardPost,
  useToggleBoardPostLike,
  useBoardComments,
  useAddBoardComment,
  useDeleteBoardComment,
} from "./hooks/use-board";

// Types
export type {
  BoardCategory,
  BoardPostDTO,
  BoardPostCreateRequest,
  BoardCommentDTO,
  BoardPageResponse,
  GroupRole,
} from "./types";

export { BOARD_CATEGORY_LABELS, BOARD_CATEGORY_COLORS } from "./types";
