import { useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../hooks";
import { Avatar } from "@/components/ui/avatar";
import { ProfileEditModal } from "./profile-edit-modal";

export function ProfileCard() {
  const { loginState, doLogout, isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const [showEditModal, setShowEditModal] = useState(false);

  if (!isLoggedIn) {
    navigate("/member/login");
    return null;
  }

  const handleLogout = () => {
    doLogout();
  };

  return (
    <>
      <div className="bg-white rounded-xl p-6 shadow-sm">
        <div className="flex items-center gap-4">
          <Avatar
            src={loginState.profileImage}
            alt={loginState.nickname}
            size="xl"
          />
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              {loginState.nickname}
            </h1>
            <p className="text-gray-500 text-sm">{loginState.email}</p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-6 flex gap-3">
          <button
            onClick={() => setShowEditModal(true)}
            className="flex-1 py-2 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          >
            프로필 수정
          </button>
          <button
            onClick={handleLogout}
            className="flex-1 py-2 border border-red-300 rounded-lg text-red-600 font-medium hover:bg-red-50 transition-colors"
          >
            로그아웃
          </button>
        </div>
      </div>

      <ProfileEditModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
      />
    </>
  );
}
