/**
 * ============================================================================
 * 결제 성공 페이지
 * ============================================================================
 *
 * [역할]
 * 카카오페이 결제 완료 후 redirect되는 페이지입니다.
 * pg_token을 사용하여 결제 승인을 처리합니다.
 *
 * [URL]
 * /payment/success?pg_token={token}&order_id={orderId}
 */

import { useEffect, useState, useRef } from "react";
import { useSearchParams, useNavigate, Link } from "react-router";
import { CheckCircle, Loader2, AlertCircle, Crown, Home, User } from "lucide-react";
import { usePaymentApprove } from "@/features/payment";
import type { PaymentDTO } from "@/features/payment";
import { formatPrice } from "@/features/payment";

export default function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [payment, setPayment] = useState<PaymentDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const { mutateAsync: approvePayment } = usePaymentApprove();

  const pgToken = searchParams.get("pg_token");
  const orderId = searchParams.get("order_id");

  // StrictMode 중복 호출 방지 (ref는 remount 시에도 유지됨)
  const isProcessingRef = useRef(false);

  useEffect(() => {
    // 유효하지 않은 결제 정보
    if (!pgToken || !orderId) {
      setError("유효하지 않은 결제 정보입니다.");
      setIsLoading(false);
      return;
    }

    // 이미 처리 중이면 스킵 (StrictMode 중복 호출 방지)
    if (isProcessingRef.current) {
      return;
    }
    isProcessingRef.current = true;

    const processPayment = async () => {
      try {
        const data = await approvePayment({ pgToken, orderId });
        setPayment(data);
      } catch (err) {
        console.error("결제 승인 실패:", err);
        setError("결제 승인에 실패했습니다. 고객센터로 문의해주세요.");
      } finally {
        setIsLoading(false);
      }
    };

    processPayment();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pgToken, orderId]);

  // 로딩 중
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Loader2 size={48} className="animate-spin text-primary-500 mx-auto mb-4" />
          <p className="text-gray-600">결제를 확인하고 있습니다...</p>
        </div>
      </div>
    );
  }

  // 에러
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <AlertCircle size={32} className="text-red-500" />
          </div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">
            결제 처리 중 오류 발생
          </h1>
          <p className="text-gray-600 mb-6">{error}</p>
          <div className="space-y-3">
            <Link
              to="/"
              className="block w-full py-3 bg-primary-500 text-white font-semibold rounded-xl hover:bg-primary-600 transition-colors"
            >
              홈으로 돌아가기
            </Link>
            <button
              onClick={() => navigate(-1)}
              className="block w-full py-3 text-gray-500 hover:text-gray-700 transition-colors"
            >
              이전 페이지로
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 성공
  if (payment) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-amber-50 to-orange-50 p-4">
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full">
          {/* 성공 아이콘 */}
          <div className="text-center mb-6">
            <div className="w-20 h-20 bg-gradient-to-br from-amber-400 to-orange-500 rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
              <Crown size={40} className="text-white" />
            </div>
            <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center mx-auto -mt-8 ml-12 border-4 border-white">
              <CheckCircle size={24} className="text-white" />
            </div>
          </div>

          <h1 className="text-2xl font-bold text-gray-900 text-center mb-2">
            프리미엄 구독 완료!
          </h1>
          <p className="text-gray-600 text-center mb-6">
            이제 모든 프리미엄 혜택을 이용하실 수 있습니다.
          </p>

          {/* 결제 정보 */}
          <div className="bg-gray-50 rounded-xl p-4 mb-6">
            <h3 className="font-semibold text-gray-900 mb-3">결제 정보</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">구독 유형</span>
                <span className="font-medium text-gray-900">
                  {payment.paymentTypeName}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">결제 금액</span>
                <span className="font-medium text-gray-900">
                  {formatPrice(payment.amount)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">결제 수단</span>
                <span className="font-medium text-gray-900">
                  {payment.paymentMethod || "카카오페이"}
                </span>
              </div>
              {payment.premiumEndDate && (
                <div className="flex justify-between">
                  <span className="text-gray-500">이용 기간</span>
                  <span className="font-medium text-gray-900">
                    {new Date(payment.premiumEndDate).toLocaleDateString("ko-KR")}까지
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* 버튼 */}
          <div className="space-y-3">
            <Link
              to="/"
              className="flex items-center justify-center gap-2 w-full py-3 bg-primary-500 text-white font-semibold rounded-xl hover:bg-primary-600 transition-colors"
            >
              <Home size={18} />
              홈으로 가기
            </Link>
            <Link
              to="/member/profile"
              className="flex items-center justify-center gap-2 w-full py-3 border border-gray-200 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors"
            >
              <User size={18} />
              마이페이지
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return null;
}
