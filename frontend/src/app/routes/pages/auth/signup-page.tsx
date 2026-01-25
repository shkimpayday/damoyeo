import { SignupForm } from "@/features/auth";

function SignupPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      {/* Logo */}
      <div className="mb-8 text-center">
        <span className="text-5xl">🎉</span>
        <h1 className="mt-4 text-2xl font-bold text-gray-900">회원가입</h1>
        <p className="mt-2 text-gray-500">다모여와 함께 새로운 취미를 찾아보세요</p>
      </div>

      <SignupForm />
    </div>
  );
}

export default SignupPage;
