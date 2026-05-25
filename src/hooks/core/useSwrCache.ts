import { ref, type Ref } from 'vue'

/**
 * sessionStorage 持久化 + SWR (stale-while-revalidate) 数据缓存
 *
 * - 模块级单例：同一 key 在多个组件中共享
 * - sessionStorage 持久化：F5 刷新后立即从缓存恢复，渲染零闪烁
 * - SWR 语义：load() 命中缓存时立即返回，背后再静默拉新；无缓存时等待请求
 * - 登出时调用 clearSwrCache 清理全部数据
 */

interface CacheEntry<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  inflight: Promise<void> | null
}

const memoryCache = new Map<string, CacheEntry<unknown>>()
const SS_PREFIX = 'velox-swr:'

function readStorage<T>(key: string): T | null {
  try {
    const raw = sessionStorage.getItem(SS_PREFIX + key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

function writeStorage<T>(key: string, value: T | null): void {
  try {
    if (value === null || value === undefined) {
      sessionStorage.removeItem(SS_PREFIX + key)
    } else {
      sessionStorage.setItem(SS_PREFIX + key, JSON.stringify(value))
    }
  } catch {
    // quota exceeded or serialization failure — degrade silently
  }
}

export function useSwrCache<T>(key: string, fetcher: () => Promise<T>) {
  let entry = memoryCache.get(key) as CacheEntry<T> | undefined
  if (!entry) {
    entry = {
      data: ref(readStorage<T>(key)) as Ref<T | null>,
      loading: ref(false),
      inflight: null
    }
    memoryCache.set(key, entry as CacheEntry<unknown>)
  }
  const self = entry

  const load = (): Promise<void> => {
    const hasCache = self.data.value !== null
    if (self.inflight) {
      return hasCache ? Promise.resolve() : self.inflight
    }
    self.loading.value = !hasCache
    self.inflight = fetcher()
      .then((res) => {
        self.data.value = res
        writeStorage(key, res)
      })
      .finally(() => {
        self.loading.value = false
        self.inflight = null
      })
    return hasCache ? Promise.resolve() : self.inflight
  }

  const mutate = (next: T | ((prev: T | null) => T)): void => {
    const updated =
      typeof next === 'function' ? (next as (prev: T | null) => T)(self.data.value) : next
    self.data.value = updated
    writeStorage(key, updated)
  }

  return {
    data: self.data,
    loading: self.loading,
    load,
    refresh: load,
    mutate
  }
}

/**
 * 清掉所有 SWR 缓存（内存 + sessionStorage）。
 * 应在登出 / 切换账号时调用。
 */
export function clearSwrCache(): void {
  memoryCache.clear()
  const stale: string[] = []
  for (let i = 0; i < sessionStorage.length; i++) {
    const k = sessionStorage.key(i)
    if (k && k.startsWith(SS_PREFIX)) stale.push(k)
  }
  stale.forEach((k) => sessionStorage.removeItem(k))
}
