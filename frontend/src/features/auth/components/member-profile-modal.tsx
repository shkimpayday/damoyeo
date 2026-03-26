/**
 * 회원 공개 프로필 모달
 *
 * 다른 회원의 공개 프로필 정보를 모달로 표시합니다.
 * - 닉네임, 프로필 이미지, 자기소개
 * - 가입일, 지역
 * - 가입한 공개 모임 목록
 * - 프로필 이미지 클릭 시 크게 보기
 */
import { useState } from "react";
import { Link } from "react-router";
import { X, MapPin, Calendar, Users, Lock, ChevronRight } from "lucide-react";
import { usePublicProfile } from "../hooks";
import { Avatar, Spinner } from "@/components/ui";
import { formatDate } from "@/utils/date";
import { ENV } from "@/config";
import { getImageUrl } from "@/utils";

interface MemberProfileModalProps {
  memberId: number | null;
  onClose: () => void;
}

export function MemberProfileModal({ memberId, onClose }: MemberProfileModalProps) {
  const [showImageViewer, setShowImageViewer] = useState(false);
  const { data: profile, isLoading, error } = usePublicProfile(
    memberId ?? undefined
  );

  if (!memberId) return null;

  const profileImageUrl = profile?.profileImage ? getImageUrl(profile.profileImage) : null;

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
        {/* Backdrop */}
        <div
          className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          onClick={onClose}
        />

        {/* Modal - 모바일에서는 하단 시트, 데스크톱에서는 중앙 모달 */}
        <div className="relative bg-white w-full sm:rounded-2xl sm:max-w-md sm:mx-4 max-h-[90vh] sm:max-h-[85vh] overflow-hidden flex flex-col rounded-t-3xl animate-in slide-in-from-bottom duration-300">
          {/* 상단 핸들 (모바일) */}
          <div className="sm:hidden flex justify-center pt-3 pb-1">
            <div className="w-10 h-1 bg-gray-300 rounded-full" />
          </div>

          {/* 닫기 버튼 */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 z-10 p-2 bg-white/80 backdrop-blur-sm hover:bg-white rounded-full transition-colors shadow-sm"
          >
            <X size={18} className="text-gray-600" />
          </button>

          {/* Content */}
          <div className="overflow-y-auto flex-1">
            {isLoading && (
              <div className="flex items-center justify-center py-24">
                <Spinner size="lg" />
              </div>
            )}

            {error && (
              <div className="flex flex-col items-center justify-center py-24 px-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                  <Users size={32} className="text-gray-400" />
                </div>
                <p className="text-gray-500 text-center font-medium">
                  회원 정보를 찾을 수 없습니다
                </p>
              </div>
            )}

            {profile && (
              <>
                {/* 프로필 헤더 - 그라데이션 배경 */}
                <div className="relative">
                  {/* 배경 그라데이션 */}
                  <div className="h-24 bg-gradient-to-br from-primary-400 via-primary-500 to-primary-600" />

                  {/* 프로필 이미지 - 배경 위에 겹치기 (클릭 가능) */}
                  <div className="absolute left-1/2 -translate-x-1/2 -bottom-12">
                    <button
                      type="button"
                      onClick={() => profileImageUrl && setShowImageViewer(true)}
                      className="relative group cursor-pointer"
                      disabled={!profileImageUrl}
                    >
                      <div className="ring-4 ring-white rounded-full shadow-lg transition-transform group-hover:scale-105">
                        <Avatar
                          src={profile.profileImage}
                          alt={profile.nickname}
                          size="2xl"
                        />
                      </div>
                      {/* 확대 힌트 오버레이 */}
                      {profileImageUrl && (
                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 rounded-full transition-colors flex items-center justify-center">
                          <span className="opacity-0 group-hover:opacity-100 text-white text-xs font-medium transition-opacity">
                            크게 보기
                          </span>
                        </div>
                      )}
                    </button>
                  </div>
                </div>

                {/* 프로필 정보 */}
                <div className="pt-16 pb-4 px-5 text-center">
                  <h3 className="text-xl font-bold text-gray-900">
                    {profile.nickname}
                  </h3>

                  {/* 위치 & 가입일 */}
                  <div className="flex items-center justify-center gap-3 mt-2 text-sm text-gray-500">
                    {profile.address && (
                      <span className="flex items-center gap-1">
                        <MapPin size={14} className="text-gray-400" />
                        {profile.address}
                      </span>
                    )}
                    {profile.address && <span className="text-gray-300">•</span>}
                    <span className="flex items-center gap-1">
                      <Calendar size={14} className="text-gray-400" />
                      {formatDate(profile.createdAt)} 가입
                    </span>
                  </div>

                  {/* 자기소개 */}
                  {profile.introduction && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-2xl text-left">
                      <p className="text-gray-600 text-sm whitespace-pre-wrap leading-relaxed">
                        {profile.introduction}
                      </p>
                    </div>
                  )}
                </div>

                {/* 활동 통계 카드 */}
                <div className="px-5 pb-4">
                  <div className="bg-gradient-to-r from-primary-50 to-blue-50 rounded-2xl p-4">
                    <div className="flex items-center justify-center gap-2">
                      <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm">
                        <Users size={20} className="text-primary-500" />
                      </div>
                      <div className="text-left">
                        <p className="text-2xl font-bold text-gray-900">
                          {profile.groupCount}
                          <span className="text-sm font-normal text-gray-500 ml-1">개</span>
                        </p>
                        <p className="text-xs text-gray-500">모임 활동 중</p>
                      </div>
                    </div>
                  </div>
                </div>

                {/* 가입한 모임 목록 */}
                {profile.joinedGroups.length > 0 && (
                  <div className="px-5 pb-6">
                    <h4 className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
                      <span>활동 중인 모임</span>
                      <span className="text-xs font-normal text-primary-500 bg-primary-50 px-2 py-0.5 rounded-full">
                        {profile.joinedGroups.length}
                      </span>
                    </h4>
                    <div className="space-y-2">
                      {profile.joinedGroups.map((group) => (
                        <Link
                          key={group.id}
                          to={`/groups/${group.id}`}
                          onClick={onClose}
                          className="flex items-center gap-3 p-3 bg-gray-50 hover:bg-gray-100 rounded-xl transition-colors group"
                        >
                          {group.thumbnailImage ? (
                            <img
                              src={
                                group.thumbnailImage.startsWith("/uploads")
                                  ? `${ENV.API_URL}${group.thumbnailImage}`
                                  : group.thumbnailImage
                              }
                              alt={group.name}
                              className="w-12 h-12 rounded-xl object-cover"
                            />
                          ) : (
                            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-gray-200 to-gray-300 flex items-center justify-center">
                              <Users size={20} className="text-gray-400" />
                            </div>
                          )}
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-gray-900 truncate group-hover:text-primary-600 transition-colors">
                              {group.name}
                            </p>
                            <p className="text-xs text-gray-500 mt-0.5">
                              {group.categoryName}
                            </p>
                          </div>
                          <ChevronRight size={18} className="text-gray-300 group-hover:text-gray-400 transition-colors" />
                        </Link>
                      ))}
                    </div>
                  </div>
                )}

                {/* 비공개 또는 모임 없음 */}
                {profile.joinedGroups.length === 0 && (
                  <div className="px-5 pb-8">
                    <div className="flex flex-col items-center py-8 px-4 bg-gray-50 rounded-2xl">
                      {profile.showJoinedGroups ? (
                        <>
                          <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mb-3">
                            <Users size={24} className="text-gray-400" />
                          </div>
                          <p className="text-gray-500 text-sm text-center">
                            아직 활동 중인 공개 모임이 없습니다
                          </p>
                        </>
                      ) : (
                        <>
                          <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mb-3">
                            <Lock size={24} className="text-gray-400" />
                          </div>
                          <p className="text-gray-500 text-sm text-center font-medium">
                            활동 모임이 비공개입니다
                          </p>
                          <p className="text-gray-400 text-xs text-center mt-1">
                            이 회원은 모임 목록을 비공개로 설정했습니다
                          </p>
                        </>
                      )}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {/* 이미지 뷰어 (프로필 이미지 크게 보기) */}
      {showImageViewer && profileImageUrl && (
        <div
          className="fixed inset-0 z-[60] flex items-center justify-center bg-black/90 animate-in fade-in duration-200"
          onClick={() => setShowImageViewer(false)}
        >
          {/* 닫기 버튼 */}
          <button
            onClick={() => setShowImageViewer(false)}
            className="absolute top-4 right-4 p-3 bg-white/10 hover:bg-white/20 rounded-full transition-colors"
          >
            <X size={24} className="text-white" />
          </button>

          {/* 닉네임 */}
          {profile && (
            <div className="absolute top-4 left-4 text-white">
              <p className="font-semibold">{profile.nickname}</p>
              <p className="text-sm text-white/70">프로필 사진</p>
            </div>
          )}

          {/* 이미지 */}
          <img
            src={profileImageUrl}
            alt={profile?.nickname || "프로필 이미지"}
            className="max-w-[90vw] max-h-[85vh] object-contain rounded-lg shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          />

          {/* 안내 문구 */}
          <p className="absolute bottom-6 text-white/50 text-sm">
            화면을 탭하면 닫힙니다
          </p>
        </div>
      )}
    </>
  );
}
