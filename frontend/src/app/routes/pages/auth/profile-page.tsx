import { useNavigate } from "react-router";
import { ProfileCard, useAuth } from "@/features/auth";

interface MenuItemProps {
  icon: string;
  label: string;
  onClick: () => void;
}

function MenuItem({ icon, label, onClick }: MenuItemProps) {
  return (
    <button
      onClick={onClick}
      className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors"
    >
      <div className="flex items-center gap-3">
        <span className="text-xl">{icon}</span>
        <span className="text-gray-700">{label}</span>
      </div>
      <span className="text-gray-400">›</span>
    </button>
  );
}

function ProfilePage() {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  if (!isLoggedIn) {
    navigate("/member/login");
    return null;
  }

  return (
    <div className="p-4">
      {/* Profile Header */}
      <ProfileCard />

      {/* Menu List */}
      <div className="mt-4 bg-white rounded-xl shadow-sm divide-y divide-gray-100">
        <MenuItem
          icon="👥"
          label="내 모임"
          onClick={() => navigate("/member/my-groups")}
        />
        <MenuItem
          icon="📅"
          label="참석할 정모"
          onClick={() => navigate("/meetings")}
        />
        <MenuItem
          icon="🔔"
          label="알림 설정"
          onClick={() => navigate("/notifications")}
        />
        <MenuItem icon="❓" label="고객센터" onClick={() => {}} />
        <MenuItem icon="📜" label="이용약관" onClick={() => {}} />
      </div>

      {/* App Info */}
      <p className="mt-8 text-center text-xs text-gray-400">다모여 v1.0.0</p>
    </div>
  );
}

export default ProfilePage;
