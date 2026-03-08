import { create } from 'zustand'

interface AuthState {
  loggedIn: boolean
  username: string
  login: (username: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  loggedIn: true,
  username: 'admin',
  login: (username) => set({ loggedIn: true, username }),
  logout: () => set({ loggedIn: false, username: '' }),
}))
