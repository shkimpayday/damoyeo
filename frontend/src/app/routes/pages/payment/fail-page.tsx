/**
 * ============================================================================
 * 결제 실패 페이지
 * ============================================================================
 *
 * [역할]
 * 카카오페이 결제 과정에서 오류가 발생한 경우 redirect되는 페이지입니다.
 *
 * [URL]
 * /payment/fail?order_id={orderId}
 */

import { useEffect } from "react";
import { useSearchParams, Link } from "react-router";
import { AlertTriangle, Home, RotateCcw, MessageCircle } from "lucide-react";
import { usePaymentFail } from "@/features/payment";

export default function PaymentFailPage() {
  const [searchParams] = useSearchParams();
  const { mutate: failPayment } = usePaymentFail();

  const orderId = searchParams.get("order_id");

  useEffect(() => {
    // 백엔드에 실패 처리 알림
    if (orderId) {
      failPayment(orderId);
    }
  }, [orderId, failPayment]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
        {/* 아이콘 */}
        <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <AlertTriangle size={32} className="text-red-500" />
        </div>

        <h1 className="text-xl font-bold text-gray-900 mb-2">
          결제에 실패했습니다
        </h1>
        <p className="text-gray-600 mb-6">
          결제 처리 중 문제가 발생했습니다.
          <br />
          잠시 후 다시 시도해주세요.
        </p>

        {/* 에러 원인 안내 */}
        <div className="bg-gray-50 rounded-xl p-4 mb-6 text-left">
          <h3 className="font-medium text-gray-900 mb-2 text-sm">
            결제 실패 원인
          </h3>
          <ul className="text-sm text-gray-600 space-y-1">
            <li>• 카드 한도 초과</li>
            <li>• 네트워크 연결 문제</li>
            <li>• 결제 시간 초과</li>
            <li>• 카드사 승인 거부</li>
          </ul>
        </div>

        {/* 버튼 */}
        <div className="space-y-3">
          <button
            onClick={() => window.history.go(-2)}
            className="flex items-center justify-center gap-2 w-full py-3 bg-primary-500 text-white font-semibold rounded-xl hover:bg-primary-600 transition-colors"
          >
            <RotateCcw size={18} />
            다시 시도하기
          </button>
          <Link
            to="/"
            className="flex items-center justify-center gap-2 w-full py-3 border border-gray-200 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors"
          >
            <Home size={18} />
            홈으로 가기
          </Link>
          <a
            href="mailto:support@damoyeo.com"
            className="flex items-center justify-center gap-2 w-full py-3 text-gray-500 hover:text-gray-700 text-sm transition-colors"
          >
            <MessageCircle size={16} />
            고객센터 문의
          </a>
        </div>
      </div>
    </div>
  );
}
