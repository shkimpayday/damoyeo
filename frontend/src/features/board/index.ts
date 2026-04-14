export { BoardList } from "./components/board-list";
export { PostCard } from "./components/post-card";
export { PostDetailModal } from "./components/post-detail-modal";
export { PostCreateModal } from "./components/post-create-modal";

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

export type {
  BoardCategory,
  BoardPostDTO,
  BoardPostCreateRequest,
  BoardCommentDTO,
  BoardPageResponse,
  GroupRole,
} from "./types";

export { BOARD_CATEGORY_LABELS, BOARD_CATEGORY_COLORS } from "./types";
