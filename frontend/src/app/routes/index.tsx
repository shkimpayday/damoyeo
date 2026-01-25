import { lazy, Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router";
import { MobileLayout } from "@/components/layout";
import { ProtectedRoute } from "./protected-route";
import { Spinner } from "@/components/ui";

// Lazy load pages
const MainPage = lazy(() => import("@/app/routes/pages/main-page"));
const SearchPage = lazy(() => import("@/app/routes/pages/search-page"));
const NotificationPage = lazy(
  () => import("@/app/routes/pages/notification-page")
);

// Auth pages
const LoginPage = lazy(() => import("@/app/routes/pages/auth/login-page"));
const SignupPage = lazy(() => import("@/app/routes/pages/auth/signup-page"));
const ProfilePage = lazy(() => import("@/app/routes/pages/auth/profile-page"));
const MyGroupsPage = lazy(
  () => import("@/app/routes/pages/auth/my-groups-page")
);
const KakaoRedirectPage = lazy(
  () => import("@/app/routes/pages/auth/kakao-redirect-page")
);

// Group pages
const GroupListPage = lazy(
  () => import("@/app/routes/pages/groups/list-page")
);
const GroupDetailPage = lazy(
  () => import("@/app/routes/pages/groups/detail-page")
);
const GroupCreatePage = lazy(
  () => import("@/app/routes/pages/groups/create-page")
);
const GroupManagePage = lazy(
  () => import("@/app/routes/pages/groups/manage-page")
);
const GroupEditPage = lazy(
  () => import("@/app/routes/pages/groups/edit-page")
);

// Meeting pages
const MeetingListPage = lazy(
  () => import("@/app/routes/pages/meetings/list-page")
);
const MeetingDetailPage = lazy(
  () => import("@/app/routes/pages/meetings/detail-page")
);
const MeetingCreatePage = lazy(
  () => import("@/app/routes/pages/meetings/create-page")
);
const MeetingEditPage = lazy(
  () => import("@/app/routes/pages/meetings/edit-page")
);

// Event pages
const EventDetailPage = lazy(
  () => import("@/app/routes/pages/events/detail-page")
);

// Loading component
function Loading() {
  return (
    <div className="flex items-center justify-center h-64">
      <Spinner size="lg" />
    </div>
  );
}

// Wrap component with Suspense
function withSuspense(Component: React.LazyExoticComponent<React.ComponentType>) {
  return (
    <Suspense fallback={<Loading />}>
      <Component />
    </Suspense>
  );
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: <MobileLayout />,
    children: [
      // Public routes
      {
        index: true,
        element: withSuspense(MainPage),
      },
      {
        path: "search",
        element: withSuspense(SearchPage),
      },
      {
        path: "notifications",
        element: withSuspense(NotificationPage),
      },

      // Events
      {
        path: "events/:eventId",
        element: withSuspense(EventDetailPage),
      },

      // Groups - public listing
      {
        path: "groups",
        children: [
          {
            index: true,
            element: <Navigate to="list" replace />,
          },
          {
            path: "list",
            element: withSuspense(GroupListPage),
          },
          {
            path: ":groupId",
            element: withSuspense(GroupDetailPage),
          },
        ],
      },

      // Protected routes
      {
        element: <ProtectedRoute />,
        children: [
          // Group management
          {
            path: "groups/create",
            element: withSuspense(GroupCreatePage),
          },
          {
            path: "groups/:groupId/manage",
            element: withSuspense(GroupManagePage),
          },
          {
            path: "groups/:groupId/edit",
            element: withSuspense(GroupEditPage),
          },

          // Meetings
          {
            path: "meetings",
            children: [
              {
                index: true,
                element: withSuspense(MeetingListPage),
              },
              {
                path: ":meetingId",
                element: withSuspense(MeetingDetailPage),
              },
              {
                path: "create/:groupId",
                element: withSuspense(MeetingCreatePage),
              },
              {
                path: ":meetingId/edit",
                element: withSuspense(MeetingEditPage),
              },
            ],
          },

          // Member
          {
            path: "member",
            children: [
              {
                path: "profile",
                element: withSuspense(ProfilePage),
              },
              {
                path: "my-groups",
                element: withSuspense(MyGroupsPage),
              },
            ],
          },
        ],
      },
    ],
  },

  // Auth routes (outside MobileLayout)
  {
    path: "member/login",
    element: withSuspense(LoginPage),
  },
  {
    path: "member/signup",
    element: withSuspense(SignupPage),
  },
  {
    path: "member/kakao",
    element: withSuspense(KakaoRedirectPage),
  },
]);
