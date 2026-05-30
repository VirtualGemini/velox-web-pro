import { ref } from 'vue'

interface DragState {
  captureTarget?: HTMLElement
  scrollTarget?: HTMLElement
  pointerId?: number
  startX: number
  scrollLeft: number
  moved: boolean
  onScroll?: () => void
}

const DRAG_THRESHOLD = 4
const CLICK_SUPPRESS_MS = 240

export function useHorizontalDragScroll() {
  const isDragging = ref(false)
  const shouldSuppressClick = ref(false)
  const state: DragState = {
    startX: 0,
    scrollLeft: 0,
    moved: false
  }

  const resetState = () => {
    state.captureTarget = undefined
    state.scrollTarget = undefined
    state.pointerId = undefined
    state.startX = 0
    state.scrollLeft = 0
    state.moved = false
    state.onScroll = undefined
    isDragging.value = false
  }

  const startDrag = (
    event: PointerEvent,
    scrollTarget?: HTMLElement | null,
    onScroll?: () => void
  ) => {
    if (!scrollTarget) return
    if (event.pointerType === 'mouse' && event.button !== 0) return
    if (scrollTarget.scrollWidth <= scrollTarget.clientWidth + 1) return

    const captureTarget = event.currentTarget as HTMLElement | null
    state.captureTarget = captureTarget || scrollTarget
    state.scrollTarget = scrollTarget
    state.pointerId = event.pointerId
    state.startX = event.clientX
    state.scrollLeft = scrollTarget.scrollLeft
    state.moved = false
    state.onScroll = onScroll

    // 关键：不要在 pointerdown 时就调用 setPointerCapture。一旦捕获指针，
    // 后续派发的 click 事件会被重定向到捕获元素（这里是外层 shell），
    // 导致点击 tab 标签时 ElTabs 收不到 click、无法切换（仅在可横向滚动/窄屏时出现）。
    // 改为在 moveDrag 中确认越过拖拽阈值后再捕获，纯点击（未移动）便能正常落到 tab 上。
  }

  const moveDrag = (event: PointerEvent) => {
    if (!state.scrollTarget || state.pointerId !== event.pointerId) return

    const deltaX = event.clientX - state.startX
    if (Math.abs(deltaX) > DRAG_THRESHOLD) {
      if (!state.moved) {
        // 真正进入拖拽后再捕获指针，确保指针移出元素也能持续滚动
        state.captureTarget?.setPointerCapture?.(event.pointerId)
      }
      state.moved = true
      isDragging.value = true
    }

    if (!state.moved) return

    state.scrollTarget.scrollLeft = state.scrollLeft - deltaX
    state.onScroll?.()
    event.preventDefault()
  }

  const endDrag = (event: PointerEvent) => {
    if (state.pointerId !== event.pointerId) return

    if (state.moved) {
      // 仅在确实捕获过（发生拖拽）时释放，避免对未捕获的指针调用 releasePointerCapture 抛错
      state.captureTarget?.releasePointerCapture?.(event.pointerId)
      shouldSuppressClick.value = true
      window.setTimeout(() => {
        shouldSuppressClick.value = false
      }, CLICK_SUPPRESS_MS)
    }
    state.onScroll?.()
    resetState()
  }

  const preventClickAfterDrag = (event: MouseEvent) => {
    if (!shouldSuppressClick.value) return

    event.preventDefault()
    event.stopPropagation()
  }

  return {
    isDragging,
    startDrag,
    moveDrag,
    endDrag,
    preventClickAfterDrag
  }
}
