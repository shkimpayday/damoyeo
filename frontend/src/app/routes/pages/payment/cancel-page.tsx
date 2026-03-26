/**
 * ============================================================================
 * 결제 취소 페이지
 * ============================================================================
 *
 * [역할]
 * 사용자가 카카오페이 결제창에서 취소 버튼을 클릭한 경우 redirect되는 페이지입니다.
 *
 * [URL]
 * /payment/cancel?order_id={orderId}
 */

import { useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import { XCircle, Home, RotateCcw } from "lucide-react";
import { usePaymentCancel } from "@/features/payment";

export default function PaymentCancelPage() {
  const [searchParams] = useSearchParams();
  const { mutate: cancelPayment } = usePaymentCancel();

  const orderId = searchParams.get("order_id");

  useEffect(() => {
    // 백엔드에 취소 처리 알림
    if (orderId) {
      cancelPayment(orderId);
    }
  }, [orderId, cancelPayment]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
        {/* 아이콘 */}
        <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <XCircle size={32} className="text-gray-400" />
        </div>

        <h1 className="text-xl font-bold text-gray-900 mb-2">
          결제가 취소되었습니다
        </h1>
        <p className="text-gray-600 mb-6">
          결제를 취소하셨습니다.
          <br />
          언제든지 다시 시도하실 수 있습니다.
        </p>

        {/* 버튼 */}
        <div className="space-y-3">
          <Link
            to="/"
            className="flex items-center justify-center gap-2 w-full py-3 bg-primary-500 text-white font-semibold rounded-xl hover:bg-primary-600 transition-colors"
          >
            <Home size={18} />
            홈으로 가기
          </Link>
          <button
            onClick={() => window.history.go(-2)} // 결제 페이지 이전으로
            className="flex items-center justify-center gap-2 w-full py-3 border border-gray-200 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors"
          >
            <RotateCcw size={18} />
            다시 시도하기
          </button>
        </div>
      </div>
    </div>
  );
}
