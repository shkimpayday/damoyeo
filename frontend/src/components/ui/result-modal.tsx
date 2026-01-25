interface ResultModalProps {
  title: string;
  content: string;
  callbackFn?: () => void;
}

export function ResultModal({ title, content, callbackFn }: ResultModalProps) {
  const handleClose = () => {
    if (callbackFn) {
      callbackFn();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-2xl shadow-xl w-80 max-w-[90%] overflow-hidden">
        <div className="p-6">
          <h3 className="text-lg font-bold text-gray-900 text-center">
            {title}
          </h3>
          <p className="mt-3 text-gray-600 text-center">{content}</p>
        </div>
        <div className="border-t border-gray-200">
          <button
            onClick={handleClose}
            className="w-full py-3 text-primary-600 font-medium hover:bg-gray-50 transition-colors"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
