import { LoginForm } from "@/features/auth";

function LoginPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      {/* Logo */}
      <div className="mb-8 text-center">
        <span className="text-5xl">🎉</span>
        <h1 className="mt-4 text-3xl font-bold text-gray-900">다모여</h1>
        <p className="mt-2 text-gray-500">우리 동네 취미 모임</p>
      </div>

      <LoginForm />
    </div>
  );
}

export default LoginPage;
