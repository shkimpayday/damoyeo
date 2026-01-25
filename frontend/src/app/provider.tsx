import { QueryClientProvider } from "@tanstack/react-query";
import { CookiesProvider } from "react-cookie";
import { queryClient } from "@/lib/react-query";

interface AppProviderProps {
  children: React.ReactNode;
}

export function AppProvider({ children }: AppProviderProps) {
  return (
    <CookiesProvider>
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    </CookiesProvider>
  );
}
