/**
 * 회원 공개 프로필 페이지
 *
 * 다른 회원의 공개 프로필 정보를 표시합니다.
 * - 닉네임, 프로필 이미지, 자기소개
 * - 가입일, 지역
 * - 가입한 공개 모임 목록
 */
import { useParams, Link, useNavigate } from "react-router";
import { ArrowLeft, MapPin, Calendar, Users } from "lucide-react";
import { usePublicProfile } from "@/features/auth/hooks";
import { Avatar, Spinner } from "@/components/ui";
import { formatDate } from "@/utils/date";
import { ENV } from "@/config";

export default function MemberProfilePage() {
  const { memberId } = useParams<{ memberId: string }>();
  const navigate = useNavigate();
  const { data: profile, isLoading, error } = usePublicProfile(
    memberId ? parseInt(memberId) : undefined
  );

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
        <p className="text-gray-500 mb-4">회원 정보를 찾을 수 없습니다.</p>
        <button
          onClick={() => navigate(-1)}
          className="text-primary-600 hover:underline"
        >
          뒤로가기
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 */}
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-lg mx-auto px-4 h-14 flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="p-2 -ml-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <ArrowLeft size={20} />
          </button>
          <h1 className="font-semibold">회원 프로필</h1>
        </div>
      </header>

      <main className="max-w-lg mx-auto">
        {/* 프로필 카드 */}
        <div className="bg-white p-6 border-b">
          <div className="flex items-center gap-4">
            <Avatar
              src={profile.profileImage}
              name={profile.nickname}
              size="xl"
            />
            <div className="flex-1">
              <h2 className="text-xl font-bold">{profile.nickname}</h2>
              {profile.address && (
                <p className="text-gray-500 text-sm flex items-center gap-1 mt-1">
                  <MapPin size={14} />
                  {profile.address}
                </p>
              )}
              <p className="text-gray-400 text-sm flex items-center gap-1 mt-1">
                <Calendar size={14} />
                {formatDate(profile.createdAt)} 가입
              </p>
            </div>
          </div>

          {/* 자기소개 */}
          {profile.introduction && (
            <div className="mt-4 p-4 bg-gray-50 rounded-lg">
              <p className="text-gray-700 text-sm whitespace-pre-wrap">
                {profile.introduction}
              </p>
            </div>
          )}
        </div>

        {/* 활동 정보 */}
        <div className="bg-white p-4 border-b">
          <div className="flex items-center gap-2 text-gray-600">
            <Users size={18} />
            <span className="font-medium">{profile.groupCount}개</span>
            <span className="text-gray-500">모임 활동 중</span>
          </div>
        </div>

        {/* 가입한 모임 목록 */}
        {profile.joinedGroups.length > 0 && (
          <div className="bg-white">
            <h3 className="px-4 py-3 font-semibold text-sm text-gray-500 border-b">
              활동 중인 모임
            </h3>
            <ul className="divide-y">
              {profile.joinedGroups.map((group) => (
                <li key={group.id}>
                  <Link
                    to={`/groups/${group.id}`}
                    className="flex items-center gap-3 p-4 hover:bg-gray-50 transition-colors"
                  >
                    {group.thumbnailImage ? (
                      <img
                        src={
                          group.thumbnailImage.startsWith("/uploads")
                            ? `${ENV.API_URL}${group.thumbnailImage}`
                            : group.thumbnailImage
                        }
                        alt={group.name}
                        className="w-12 h-12 rounded-lg object-cover"
                      />
                    ) : (
                      <div className="w-12 h-12 rounded-lg bg-gray-200 flex items-center justify-center">
                        <Users size={20} className="text-gray-400" />
                      </div>
                    )}
                    <div className="flex-1 min-w-0">
                      <p className="font-medium truncate">{group.name}</p>
                      <p className="text-sm text-gray-500">
                        {group.categoryName}
                      </p>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        )}

        {profile.joinedGroups.length === 0 && (
          <div className="bg-white p-8 text-center">
            <p className="text-gray-500">아직 활동 중인 공개 모임이 없습니다.</p>
          </div>
        )}
      </main>
    </div>
  );
}
