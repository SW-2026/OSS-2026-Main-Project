import { createContext, useContext, useState, useEffect, type ReactNode } from "react";

export interface User {
  id: string;
  email: string;
  nickname: string;
  avatar: string;
  createdAt: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<{ success: boolean; error?: string }>;
  signup: (email: string, password: string, nickname: string) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = "webtoon_ai_user";
const USERS_KEY = "webtoon_ai_users";

interface StoredUser extends User {
  password: string;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem(STORAGE_KEY);
      }
    }
    setIsLoading(false);
  }, []);

  const getUsers = (): StoredUser[] => {
    try {
      return JSON.parse(localStorage.getItem(USERS_KEY) || "[]");
    } catch {
      return [];
    }
  };

  const saveUsers = (users: StoredUser[]) => {
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  };

  const getAvatarColor = (nickname: string) => {
    return nickname.charAt(0).toUpperCase();
  };

  const login = async (email: string, password: string): Promise<{ success: boolean; error?: string }> => {
    await new Promise((r) => setTimeout(r, 600));
    const users = getUsers();
    const found = users.find((u) => u.email === email && u.password === password);
    if (!found) {
      return { success: false, error: "이메일 또는 비밀번호가 올바르지 않습니다." };
    }
    const { password: _pw, ...userWithoutPw } = found;
    void _pw;
    setUser(userWithoutPw);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(userWithoutPw));
    return { success: true };
  };

  const signup = async (email: string, password: string, nickname: string): Promise<{ success: boolean; error?: string }> => {
    await new Promise((r) => setTimeout(r, 600));
    const users = getUsers();
    if (users.find((u) => u.email === email)) {
      return { success: false, error: "이미 사용 중인 이메일입니다." };
    }
    const newUser: StoredUser = {
      id: `user_${Date.now()}`,
      email,
      password,
      nickname,
      avatar: getAvatarColor(nickname),
      createdAt: new Date().toISOString(),
    };
    saveUsers([...users, newUser]);
    const { password: _pw, ...userWithoutPw } = newUser;
    void _pw;
    setUser(userWithoutPw);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(userWithoutPw));
    return { success: true };
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
