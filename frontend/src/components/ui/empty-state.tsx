import type { ReactNode } from "react";

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?:
    | {
        label: string;
        onClick: () => void;
      }
    | ReactNode;
}

export function EmptyState({
  icon = "📭",
  title,
  description,
  action,
}: EmptyStateProps) {
  const renderAction = () => {
    if (!action) return null;

    // ReactNode인 경우 그대로 렌더링
    if (typeof action !== "object" || !("label" in action)) {
      return action;
    }

    // { label, onClick } 형태인 경우
    return (
      <button
        onClick={action.onClick}
        className="mt-6 px-6 py-2 bg-primary-500 text-white rounded-lg font-medium hover:bg-primary-600 transition-colors"
      >
        {action.label}
      </button>
    );
  };

  return (
    <div className="flex flex-col items-center justify-center py-16 px-4">
      <span className="text-6xl mb-4">{icon}</span>
      <h3 className="text-lg font-medium text-gray-900">{title}</h3>
      {description && (
        <p className="mt-2 text-sm text-gray-500 text-center">{description}</p>
      )}
      {renderAction()}
    </div>
  );
}
