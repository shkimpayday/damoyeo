import { Link } from "react-router";
import { formatDateTime, getDayOfWeek } from "@/utils/date";
import type { MeetingListDTO } from "../types";

interface MeetingCardProps {
  meeting: MeetingListDTO;
}

/**
 * 정모 상태별 설정 (색상 + 라벨)
 */
const STATUS_CONFIG = {
  SCHEDULED: { color: "bg-blue-100 text-blue-700", label: "예정" },
  ONGOING: { color: "bg-green-100 text-green-700", label: "진행중" },
  COMPLETED: { color: "bg-gray-100 text-gray-600", label: "완료" },
  CANCELLED: { color: "bg-red-100 text-red-600", label: "취소" },
} as const;

export function MeetingCard({ meeting }: MeetingCardProps) {
  return (
    <Link
      to={`/meetings/${meeting.id}`}
      className="block bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow p-4"
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          {/* Group Name */}
          <p className="text-xs text-gray-500 mb-1">{meeting.groupName}</p>

          {/* Title */}
          <h3 className="font-bold text-gray-900">{meeting.title}</h3>

          {/* Date & Location */}
          <div className="mt-2 space-y-1">
            <p className="text-sm text-gray-600">
              📅 {formatDateTime(meeting.meetingDate)} (
              {getDayOfWeek(meeting.meetingDate)})
            </p>
            <p className="text-sm text-gray-500 truncate">
              📍 {meeting.address}
            </p>
          </div>

          {/* Attendees */}
          <p className="mt-2 text-sm">
            <span className="text-primary-600 font-medium">
              {meeting.currentAttendees}
            </span>
            <span className="text-gray-500">/{meeting.maxAttendees}명 참석</span>
          </p>
        </div>

        {/* Status Badge */}
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${
            STATUS_CONFIG[meeting.status].color
          }`}
        >
          {STATUS_CONFIG[meeting.status].label}
        </span>
      </div>
    </Link>
  );
}
