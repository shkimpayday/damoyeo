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
const MemberProfilePage = lazy(
  () => import("@/app/routes/pages/auth/member-profile-page")
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
const ChatPage = lazy(
  () => import("@/app/routes/pages/groups/chat-page")
);
const GalleryPage = lazy(
  () => import("@/app/routes/pages/groups/gallery-page")
);
const BoardPage = lazy(
  () => import("@/app/routes/pages/groups/board-page")
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
const MeetingChatPage = lazy(
  () => import("@/app/routes/pages/meetings/chat-page")
);

// Event pages
const EventDetailPage = lazy(
  () => import("@/app/routes/pages/events/detail-page")
);

// Payment pages
const PaymentSuccessPage = lazy(
  () => import("@/app/routes/pages/payment/success-page")
);
const PaymentCancelPage = lazy(
  () => import("@/app/routes/pages/payment/cancel-page")
);
const PaymentFailPage = lazy(
  () => import("@/app/routes/pages/payment/fail-page")
);

// Admin pages
const AdminLayout = lazy(
  () => import("@/app/routes/pages/admin/admin-layout")
);
const AdminDashboardPage = lazy(
  () => import("@/app/routes/pages/admin/dashboard-page")
);
const AdminMembersPage = lazy(
  () => import("@/app/routes/pages/admin/members-page")
);
const AdminGroupsPage = lazy(
  () => import("@/app/routes/pages/admin/groups-page")
);
const AdminEventsPage = lazy(
  () => import("@/app/routes/pages/admin/events-page")
);
const AdminSupportPage = lazy(
  () => import("@/app/routes/pages/admin/support-page")
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
          {
            path: "groups/:groupId/chat",
            element: withSuspense(ChatPage),
          },
          {
            path: "groups/:groupId/gallery",
            element: withSuspense(GalleryPage),
          },
          {
            path: "groups/:groupId/board",
            element: withSuspense(BoardPage),
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
              {
                path: ":meetingId/chat",
                element: withSuspense(MeetingChatPage),
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
              {
                path: ":memberId",
                element: withSuspense(MemberProfilePage),
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

  // Payment callback routes (outside MobileLayout)
  {
    path: "payment/success",
    element: withSuspense(PaymentSuccessPage),
  },
  {
    path: "payment/cancel",
    element: withSuspense(PaymentCancelPage),
  },
  {
    path: "payment/fail",
    element: withSuspense(PaymentFailPage),
  },

  // Admin routes (separate layout, ADMIN role required)
  {
    element: <ProtectedRoute requiredRoles={["ADMIN"]} />,
    children: [
      {
        path: "admin",
        element: withSuspense(AdminLayout),
        children: [
          {
            index: true,
            element: <Navigate to="dashboard" replace />,
          },
          {
            path: "dashboard",
            element: withSuspense(AdminDashboardPage),
          },
          {
            path: "members",
            element: withSuspense(AdminMembersPage),
          },
          {
            path: "groups",
            element: withSuspense(AdminGroupsPage),
          },
          {
            path: "events",
            element: withSuspense(AdminEventsPage),
          },
          {
            path: "support",
            element: withSuspense(AdminSupportPage),
          },
        ],
      },
    ],
  },
]);
