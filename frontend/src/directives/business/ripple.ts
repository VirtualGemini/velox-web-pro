/**
 * v-ripple 水波纹效果指令
 *
 * 为元素添加 Material Design 风格的水波纹点击效果。
 * 点击时从点击位置扩散出圆形水波纹动画，提升交互体验。
 *
 * ## 主要功能
 *
 * - 水波纹动画 - 点击时从点击位置扩散圆形波纹
 * - 自适应大小 - 根据元素尺寸自动调整波纹大小和动画时长
 * - 智能配色 - 自动识别按钮类型，使用合适的波纹颜色
 * - 自定义颜色 - 支持通过参数自定义波纹颜色
 * - 性能优化 - 使用 requestAnimationFrame 和自动清理机制
 *
 * ## 使用示例
 *
 * ```vue
 * <template>
 *   <!-- 基础用法 - 使用默认颜色 -->
 *   <el-button v-ripple>点击我</el-button>
 *
 *   <!-- 自定义颜色 -->
 *   <el-button v-ripple="{ color: 'rgba(255, 0, 0, 0.3)' }">自定义颜色</el-button>
 *
 *   <!-- 应用到任意元素 -->
 *   <div v-ripple class="custom-card">卡片内容</div>
 * </template>
 * ```
 *
 * ## 颜色规则
 *
 * - 有色按钮（primary、success、warning 等）：使用白色半透明波纹
 * - 默认按钮：使用主题色半透明波纹
 * - 自定义：通过 color 参数指定任意颜色
 *
 * @module directives/ripple
 * @author Velox Team
 */

import type { App, Directive, DirectiveBinding } from 'vue'

export interface RippleOptions {
  /** 水波纹颜色 */
  color?: string
}

export type RippleDirective = Directive<HTMLElement, RippleOptions>

const MANUAL_RIPPLE_MARK = 'veloxRippleManual'
let autoRippleBound = false

function prepareRippleContainer(el: HTMLElement) {
  if (!el.style.position) {
    el.style.position = 'relative'
  }
  el.style.overflow = 'hidden'
}

function createRipple(el: HTMLElement, e: MouseEvent, options: RippleOptions = {}) {
  const rect = el.getBoundingClientRect()
  const left = e.clientX - rect.left
  const top = e.clientY - rect.top

  const ripple = document.createElement('div')
  const diameter = Math.max(el.clientWidth, el.clientHeight)
  const radius = diameter / 2

  const baseTime = 600
  const scaleFactor = 0.5
  const animationDuration = baseTime + diameter * scaleFactor

  ripple.style.width = ripple.style.height = `${diameter}px`
  ripple.style.left = `${left - radius}px`
  ripple.style.top = `${top - radius}px`
  ripple.style.position = 'absolute'
  ripple.style.borderRadius = '50%'
  ripple.style.pointerEvents = 'none'

  const buttonTypes = ['primary', 'info', 'warning', 'danger', 'success'].map(
    (type) => `el-button--${type}`
  )
  const isColoredButton = buttonTypes.some((type) => el.classList.contains(type))
  const defaultColor = isColoredButton
    ? 'rgba(255, 255, 255, 0.25)'
    : 'var(--el-color-primary-light-7)'

  ripple.style.backgroundColor = options.color || defaultColor
  ripple.style.transform = 'scale(0)'
  ripple.style.transition = `transform ${animationDuration}ms cubic-bezier(0.3, 0, 0.2, 1), opacity ${animationDuration}ms cubic-bezier(0.3, 0, 0.5, 1)`
  ripple.style.zIndex = '1'

  el.appendChild(ripple)

  requestAnimationFrame(() => {
    ripple.style.transform = 'scale(2)'
    ripple.style.opacity = '0'
  })

  setTimeout(() => {
    ripple.remove()
  }, animationDuration + 500)
}

function bindRipple(el: HTMLElement, options: RippleOptions = {}) {
  prepareRippleContainer(el)
  el.addEventListener('mousedown', (e: MouseEvent) => {
    createRipple(el, e, options)
  })
}

function isAutoRippleButton(el: HTMLElement) {
  if (!el.classList.contains('el-button')) return false
  if (el.dataset[MANUAL_RIPPLE_MARK] === 'true') return false
  if (el.classList.contains('is-disabled') || (el as HTMLButtonElement).disabled) return false
  return el.textContent?.trim().length
    ? true
    : false
}

export const vRipple: RippleDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const options: RippleOptions = binding.value || {}
    el.dataset[MANUAL_RIPPLE_MARK] = 'true'
    bindRipple(el, options)
  }
}

export function setupRippleDirective(app: App) {
  app.directive('ripple', vRipple)

  if (autoRippleBound) return
  autoRippleBound = true

  document.addEventListener('mousedown', (e: MouseEvent) => {
    const target = e.target as HTMLElement | null
    const button = target?.closest('.el-button') as HTMLElement | null
    if (!button || !isAutoRippleButton(button)) return
    prepareRippleContainer(button)
    createRipple(button, e)
  })
}
