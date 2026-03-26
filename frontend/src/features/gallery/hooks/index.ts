// 새로운 Post 기반 hooks
export {
  useGalleryPosts,
  useGalleryPostsInfinite,
  useGalleryPostCount,
  useRecentGalleryPosts,
  useUploadGalleryPost,
  useDeleteGalleryPost,
  useTogglePostLike,
  usePostComments,
  useAddPostComment,
  useDeletePostComment,
} from "./use-gallery";

// 하위 호환성을 위한 별칭 (deprecated)
export {
  useGalleryImages,
  useGalleryImagesInfinite,
  useGalleryImageCount,
  useRecentGalleryImages,
  useUploadGalleryImages,
  useDeleteGalleryImage,
  useToggleImageLike,
  useImageComments,
  useAddImageComment,
  useDeleteImageComment,
} from "./use-gallery";
