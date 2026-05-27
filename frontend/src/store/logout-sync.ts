import { useUserStore } from './modules/user'
import { StorageConfig } from '@/utils/storage/storage-config'
import { router } from '@/router'
import { RoutesAlias } from '@/router/routesAlias'

let listenerRegistered = false
const USER_STORE_KEY = StorageConfig.generateStorageKey('user')

function handleLogoutSignal(event: StorageEvent) {
  if (event.key !== StorageConfig.LOGOUT_SIGNAL_KEY || !event.newValue) {
    return
  }

  useUserStore().logOut({ remote: false, broadcast: false })
}

function handleUserStoreChange(event: StorageEvent) {
  if (event.key !== USER_STORE_KEY || !event.newValue) {
    return
  }

  if (router.currentRoute.value.path !== RoutesAlias.Login) {
    return
  }

  try {
    const snapshot = JSON.parse(event.newValue) as Partial<Record<string, unknown>>
    useUserStore().syncLoginStateFromPersisted(snapshot)
  } catch (error) {
    console.warn('[AuthSync] Failed to parse persisted user snapshot', error)
  }
}

function handleStorageEvent(event: StorageEvent) {
  handleLogoutSignal(event)
  handleUserStoreChange(event)
}

export function setupLogoutSync(): void {
  if (listenerRegistered || typeof window === 'undefined') {
    return
  }

  window.addEventListener('storage', handleStorageEvent)
  listenerRegistered = true
}
