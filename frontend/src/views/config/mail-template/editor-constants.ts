/**
 * 邮件编辑器本地配置。
 *
 * 仅包含前端编辑器自身的 block 定义与 HTML/变量替换工具；
 * 所有可由后端返回的模板元数据（类型、语言、变量、预览样例）均不在此硬编码。
 */

/** 全部类型共用的模板变量（仅占位名，展示文案由后端 metadata 返回） */
export interface MailTemplateVariableMeta {
  name: string
  label: string
}

// ─── 邮件可视化编辑器：Block 数据模型 ────────────────────────────────────────

export type EmailBlockType =
  | 'heading'
  | 'text'
  | 'button'
  | 'link'
  | 'image'
  | 'markdown'
  | 'container'
  | 'section'
  | 'columns'
  | 'divider'
  | 'spacer'
  | 'code'
  | 'inlineCode'
  | 'preview'

export type EmailBlockCategory = 'content' | 'layout' | 'advanced'

export interface EmailBlock {
  id: string
  type: EmailBlockType
  props: Record<string, string>
}

export interface EmailBlockTypeMeta {
  type: EmailBlockType
  labelKey: string
  icon: string
  category: EmailBlockCategory
  defaultProps: Record<string, string>
}

export const EMAIL_BLOCK_TYPES: EmailBlockTypeMeta[] = [
  {
    type: 'heading',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.heading',
    icon: 'EditPen',
    category: 'content',
    defaultProps: { level: 'h2', content: 'Heading', textAlign: 'left', color: '#111827' }
  },
  {
    type: 'text',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.text',
    icon: 'Document',
    category: 'content',
    defaultProps: {
      content: 'Your text here.',
      textAlign: 'left',
      color: '#374151',
      fontSize: '14px'
    }
  },
  {
    type: 'button',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.button',
    icon: 'Position',
    category: 'content',
    defaultProps: {
      content: 'Button',
      href: 'https://',
      textAlign: 'center',
      backgroundColor: '#1d4ed8',
      color: '#ffffff'
    }
  },
  {
    type: 'link',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.link',
    icon: 'Link',
    category: 'content',
    defaultProps: { content: 'Link text', href: 'https://', textAlign: 'left', color: '#2563eb' }
  },
  {
    type: 'image',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.image',
    icon: 'Picture',
    category: 'content',
    defaultProps: { src: '', alt: '', width: '', textAlign: 'center' }
  },
  {
    type: 'markdown',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.markdown',
    icon: 'Memo',
    category: 'content',
    defaultProps: { content: '**Bold** and _italic_.\n\n- First item\n- Second item' }
  },
  {
    type: 'inlineCode',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.inlineCode',
    icon: 'Tickets',
    category: 'content',
    defaultProps: { content: 'inlineCode', color: '#111827', backgroundColor: '#f1f5f9' }
  },
  {
    type: 'container',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.container',
    icon: 'Box',
    category: 'layout',
    defaultProps: { content: 'Container content', backgroundColor: '#ffffff', padding: '20px' }
  },
  {
    type: 'section',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.section',
    icon: 'Grid',
    category: 'layout',
    defaultProps: { backgroundColor: '#f9fafb', padding: '16px', content: '' }
  },
  {
    type: 'columns',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.columns',
    icon: 'Operation',
    category: 'layout',
    defaultProps: { col1: 'Column one', col2: 'Column two', textAlign: 'left' }
  },
  {
    type: 'divider',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.divider',
    icon: 'SemiSelect',
    category: 'layout',
    defaultProps: { color: '#e5e7eb' }
  },
  {
    type: 'spacer',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.spacer',
    icon: 'Rank',
    category: 'layout',
    defaultProps: { height: '24px' }
  },
  {
    type: 'code',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.code',
    icon: 'Cpu',
    category: 'advanced',
    defaultProps: {
      content: 'const hello = "world"',
      lang: 'javascript',
      fontSize: '13px',
      backgroundColor: '#0f172a'
    }
  },
  {
    type: 'preview',
    labelKey: 'pages.config.mailTemplate.editor.blockTypes.preview',
    icon: 'View',
    category: 'advanced',
    defaultProps: { content: 'This preheader appears in the inbox preview.' }
  }
]

export const EMAIL_BLOCK_CATEGORIES: EmailBlockCategory[] = ['content', 'layout', 'advanced']

export function getBlockTypesByCategory(category: EmailBlockCategory): EmailBlockTypeMeta[] {
  return EMAIL_BLOCK_TYPES.filter((meta) => meta.category === category)
}

const BLOCK_TYPE_META_BY_TYPE: Record<string, EmailBlockTypeMeta> = EMAIL_BLOCK_TYPES.reduce(
  (acc, meta) => {
    acc[meta.type] = meta
    return acc
  },
  {} as Record<string, EmailBlockTypeMeta>
)

export function getBlockTypeMeta(type: EmailBlockType): EmailBlockTypeMeta | undefined {
  return BLOCK_TYPE_META_BY_TYPE[type]
}

export function createBlock(type: EmailBlockType): EmailBlock {
  const meta = getBlockTypeMeta(type)
  const props = meta ? { ...meta.defaultProps } : {}
  return {
    id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    type,
    props
  }
}

const VARIABLE_RE = /\{\{\s*([a-zA-Z0-9_]+)\s*\}\}/g

export function hasVariables(text: string): boolean {
  VARIABLE_RE.lastIndex = 0
  return VARIABLE_RE.test(text)
}

export function replaceVariables(
  template: string,
  vars: Record<string, string>,
  escapeHtml = false
): string {
  return (template || '').replace(VARIABLE_RE, (_match, key: string) => {
    const value = Object.prototype.hasOwnProperty.call(vars, key) ? String(vars[key]) : ''
    if (escapeHtml) return escapeHtmlEntities(value)
    return value
  })
}

function escapeHtmlEntities(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function renderMarkdown(src: string): string {
  if (!src || !src.trim()) return ''
  const lines = src.replace(/\r\n/g, '\n').split('\n')
  const html: string[] = []
  let listType: 'ul' | 'ol' | null = null

  const closeList = () => {
    if (listType) {
      html.push(`</${listType}>`)
      listType = null
    }
  }

  const inline = (text: string): string => {
    let out = escapeHtmlEntities(text)
    out = out.replace(
      /`([^`]+)`/g,
      '<code style="background:#f1f5f9;padding:1px 5px;border-radius:4px;font-size:13px;">$1</code>'
    )
    out = out.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    out = out.replace(/(^|[^*])\*([^*]+)\*/g, '$1<em>$2</em>')
    out = out.replace(/_([^_]+)_/g, '<em>$1</em>')
    out = out.replace(
      /\[([^\]]+)\]\(([^)]+)\)/g,
      '<a href="$2" style="color:#2563eb;text-decoration:underline;">$1</a>'
    )
    return out
  }

  for (const raw of lines) {
    const line = raw.trimEnd()
    const heading = /^(#{1,6})\s+(.*)$/.exec(line)
    const ulItem = /^[-*]\s+(.*)$/.exec(line)
    const olItem = /^\d+\.\s+(.*)$/.exec(line)

    if (heading) {
      closeList()
      const level = heading[1].length
      html.push(
        `<h${level} style="margin:14px 0 8px;font-weight:600;">${inline(heading[2])}</h${level}>`
      )
    } else if (ulItem) {
      if (listType !== 'ul') {
        closeList()
        html.push('<ul style="margin:8px 0;padding-left:22px;">')
        listType = 'ul'
      }
      html.push(`<li style="margin:4px 0;">${inline(ulItem[1])}</li>`)
    } else if (olItem) {
      if (listType !== 'ol') {
        closeList()
        html.push('<ol style="margin:8px 0;padding-left:22px;">')
        listType = 'ol'
      }
      html.push(`<li style="margin:4px 0;">${inline(olItem[1])}</li>`)
    } else if (!line.trim()) {
      closeList()
    } else {
      closeList()
      html.push(`<p style="margin:10px 0;font-size:14px;line-height:24px;">${inline(line)}</p>`)
    }
  }
  closeList()
  return html.join('')
}

export function parseHtmlToBlocks(html: string): EmailBlock[] {
  if (!html || !html.trim()) return []

  if (typeof DOMParser === 'undefined') {
    return extractTextBlocks(html)
  }

  const doc = new DOMParser().parseFromString(html, 'text/html')
  const root = resolveEditableRoot(doc)
  const headCodeNodes = Array.from(doc.head.querySelectorAll('style, script'))
  const blocks = [...nodesToBlocks(headCodeNodes), ...nodesToBlocks(Array.from(root.childNodes))]

  if (blocks.length === 0) {
    blocks.push({
      id: `block_custom_${Date.now()}`,
      type: 'text',
      props: { content: normalizeText(textWithLineBreaks(root)) }
    })
  }

  return blocks
}

function resolveEditableRoot(doc: Document): HTMLElement {
  let root = doc.body
  while (root.children.length === 1 && isWrapperElement(root.children[0])) {
    root = root.children[0] as HTMLElement
  }
  return root
}

function isWrapperElement(element: Element): boolean {
  const tag = element.tagName.toLowerCase()
  if (tag !== 'div' && tag !== 'main' && tag !== 'section') return false
  const style = element.getAttribute('style') || ''
  return (
    element.children.length > 0 &&
    /max-width|margin|padding|font-family|line-height|color/i.test(style)
  )
}

function nodesToBlocks(nodes: Node[]): EmailBlock[] {
  const blocks: EmailBlock[] = []
  let textBuffer = ''

  const flushText = () => {
    const text = normalizeText(textBuffer)
    if (text) blocks.push(createTextBlock(text))
    textBuffer = ''
  }

  for (const node of nodes) {
    if (node.nodeType === Node.TEXT_NODE) {
      textBuffer += node.textContent || ''
      continue
    }

    if (node.nodeType !== Node.ELEMENT_NODE) {
      continue
    }

    flushText()
    blocks.push(...elementToBlocks(node as HTMLElement))
  }

  flushText()
  return blocks
}

function elementToBlocks(element: HTMLElement): EmailBlock[] {
  const block = elementToBlock(element)
  if (block) return [block]

  if (hasBlockElementChildren(element)) {
    return nodesToBlocks(Array.from(element.childNodes))
  }

  const text = normalizeText(textWithLineBreaks(element))
  return text ? [createTextBlock(text, element)] : []
}

function elementToBlock(element: HTMLElement): EmailBlock | null {
  const id = `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
  const tag = element.tagName.toLowerCase()

  if (tag === 'script' || tag === 'style') {
    return {
      id,
      type: 'code',
      props: {
        content: element.textContent || '',
        lang: tag === 'script' ? 'javascript' : 'css',
        fontSize: styleValue(element, 'font-size') || '13px',
        backgroundColor: styleValue(element, 'background-color') || '#0f172a'
      }
    }
  }

  if (tag === 'pre') {
    const code = element.querySelector('code')
    const className = code?.className || element.className || ''
    return {
      id,
      type: 'code',
      props: {
        content: code?.textContent || element.textContent || '',
        lang: extractCodeLanguage(className),
        fontSize: styleValue(element, 'font-size') || '13px',
        backgroundColor: styleValue(element, 'background-color') || '#0f172a'
      }
    }
  }

  if (tag === 'code') {
    return {
      id,
      type: 'inlineCode',
      props: {
        content: element.textContent || '',
        color: styleValue(element, 'color') || '#111827',
        backgroundColor: styleValue(element, 'background-color') || '#f1f5f9'
      }
    }
  }

  if (/^h[1-6]$/.test(tag)) {
    return {
      id,
      type: 'heading',
      props: {
        level: tag,
        content: normalizeText(textWithLineBreaks(element)),
        textAlign: styleValue(element, 'text-align') || 'left',
        color: styleValue(element, 'color') || '#111827'
      }
    }
  }

  if (tag === 'hr') {
    return {
      id,
      type: 'divider',
      props: { color: styleValue(element, 'border-top-color') || '#e5e7eb' }
    }
  }

  if (tag === 'img') {
    return {
      id,
      type: 'image',
      props: {
        src: element.getAttribute('src') || '',
        alt: element.getAttribute('alt') || '',
        width: element.getAttribute('width') || styleValue(element, 'width') || '',
        textAlign: styleValue(element.parentElement, 'text-align') || 'center'
      }
    }
  }

  if (tag === 'a') {
    const text = normalizeText(textWithLineBreaks(element))
    const href = element.getAttribute('href') || ''
    const backgroundColor = styleValue(element, 'background-color')
    if (backgroundColor) {
      return {
        id,
        type: 'button',
        props: {
          content: text,
          href,
          textAlign: styleValue(element.parentElement, 'text-align') || 'center',
          backgroundColor,
          color: styleValue(element, 'color') || '#ffffff'
        }
      }
    }
    return {
      id,
      type: 'link',
      props: {
        content: text || href,
        href,
        textAlign: styleValue(element, 'text-align') || styleValue(element.parentElement, 'text-align') || 'left',
        color: styleValue(element, 'color') || '#2563eb'
      }
    }
  }

  if (tag === 'ul' || tag === 'ol') {
    return {
      id,
      type: 'markdown',
      props: { content: listToMarkdown(element, tag === 'ol') }
    }
  }

  if (tag === 'p' || tag === 'span' || (tag === 'div' && !hasBlockElementChildren(element))) {
    if (hasRichInlineMarkup(element)) {
      return {
        id,
        type: 'markdown',
        props: { content: normalizeText(textWithLineBreaks(element)) }
      }
    }
    return {
      id,
      type: 'text',
      props: {
        content: normalizeText(textWithLineBreaks(element)),
        textAlign: styleValue(element, 'text-align') || 'left',
        color: styleValue(element, 'color') || '#374151',
        fontSize: styleValue(element, 'font-size') || '14px'
      }
    }
  }

  if (tag === 'table') {
    const cells = Array.from(element.querySelectorAll('td'))
    if (cells.length === 2) {
      const first = cells[0] as HTMLElement
      const second = cells[1] as HTMLElement
      return {
        id,
        type: 'columns',
        props: {
          col1: normalizeText(textWithLineBreaks(first)),
          col2: normalizeText(textWithLineBreaks(second)),
          textAlign: styleValue(first, 'text-align') || 'left'
        }
      }
    }
    const cell = cells[0] as HTMLElement | undefined
    return {
      id,
      type: 'section',
      props: {
        backgroundColor: styleValue(cell, 'background-color') || styleValue(element, 'background-color') || '#f9fafb',
        padding: styleValue(cell, 'padding') || '16px',
        content: normalizeText(textWithLineBreaks(cell || element))
      }
    }
  }

  return null
}

function createTextBlock(content: string, element?: HTMLElement): EmailBlock {
  return {
    id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    type: 'text',
    props: {
      content,
      textAlign: styleValue(element, 'text-align') || 'left',
      color: styleValue(element, 'color') || '#374151',
      fontSize: styleValue(element, 'font-size') || '14px'
    }
  }
}

function extractTextBlocks(html: string): EmailBlock[] {
  const plain = decodeHtml(stripTagsWithLineBreaks(html)).trim()
  if (!plain) return []
  return [
    {
      id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
      type: 'text',
      props: { content: plain, textAlign: 'left', color: '#374151', fontSize: '14px' }
    }
  ]
}

function hasBlockElementChildren(element: HTMLElement): boolean {
  return Array.from(element.children).some((child) =>
    /^(div|p|h[1-6]|table|section|main|article|header|footer|ul|ol|li|pre|script|style|hr|img)$/i.test(
      child.tagName
    )
  )
}

function hasRichInlineMarkup(element: HTMLElement): boolean {
  return Array.from(element.children).some((child) =>
    /^(strong|b|em|i|u|s|del|ul|ol|li|code|a)$/i.test(child.tagName)
  )
}

function styleValue(element: Element | null | undefined, name: string): string {
  if (!element || !(element instanceof HTMLElement)) return ''
  return element.style.getPropertyValue(name).trim() || extractCssValue(element.getAttribute('style') || '', name)
}

function textWithLineBreaks(node: Node): string {
  if (node.nodeType === Node.TEXT_NODE) return node.textContent || ''
  if (node.nodeType !== Node.ELEMENT_NODE) return ''

  const element = node as HTMLElement
  const tag = element.tagName.toLowerCase()
  if (tag === 'br') return '\n'
  if (tag === 'script' || tag === 'style') return element.textContent || ''

  let value = ''
  element.childNodes.forEach((child) => {
    value += textWithLineBreaks(child)
  })
  if (/^(p|div|section|li|tr)$/i.test(tag)) value += '\n'
  return value
}

function normalizeText(value: string): string {
  return value
    .replace(/\r\n/g, '\n')
    .replace(/\u00a0/g, ' ')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function listToMarkdown(element: HTMLElement, ordered: boolean): string {
  return Array.from(element.children)
    .filter((child) => child.tagName.toLowerCase() === 'li')
    .map((child, index) => `${ordered ? `${index + 1}.` : '-'} ${normalizeText(textWithLineBreaks(child))}`)
    .join('\n')
}

function extractCodeLanguage(className: string): string {
  const match = /(?:language|lang)-([a-z0-9_+-]+)/i.exec(className)
  return match?.[1] || ''
}

function extractCssValue(style: string, name: string): string {
  const match = new RegExp(`${name}\\s*:\\s*([^;]+)`, 'i').exec(style)
  return match?.[1]?.trim() || ''
}

function stripTagsWithLineBreaks(html: string): string {
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/(p|div|li)>/gi, '\n')
    .replace(/<li[^>]*>/gi, '- ')
    .replace(/<[^>]+>/g, '')
}

function decodeHtml(value: string): string {
  return value
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&amp;/g, '&')
}

export function serializeBlocksToHtml(blocks: EmailBlock[]): string {
  if (!blocks.length) return ''
  const body = blocks
    .map((block) => {
      switch (block.type) {
        case 'heading':
          return `<${block.props.level || 'h2'} style="margin:16px 0 8px;font-weight:600;text-align:${block.props.textAlign || 'left'};color:${block.props.color || '#111827'};">${escapeHtmlEntities(block.props.content || '')}</${block.props.level || 'h2'}>`
        case 'text':
          return `<p style="margin:16px 0;text-align:${block.props.textAlign || 'left'};color:${block.props.color || '#374151'};font-size:${block.props.fontSize || '14px'};line-height:24px;">${renderTextContent(block.props.content || '')}</p>`
        case 'button':
          return `<div style="text-align:${block.props.textAlign || 'center'};margin:16px 0;"><a href="${block.props.href || '#'}" style="display:inline-block;padding:12px 24px;background-color:${block.props.backgroundColor || '#1d4ed8'};color:${block.props.color || '#ffffff'};border-radius:6px;text-decoration:none;font-weight:500;font-size:14px;">${escapeHtmlEntities(block.props.content || '')}</a></div>`
        case 'link':
          return `<a href="${block.props.href || '#'}" style="display:inline-block;color:${block.props.color || '#2563eb'};text-decoration:underline;text-align:${block.props.textAlign || 'left'};margin:12px 0;font-size:14px;">${escapeHtmlEntities(block.props.content || block.props.href || '')}</a>`
        case 'image':
          return `<div style="text-align:${block.props.textAlign || 'center'};margin:16px 0;"><img src="${block.props.src || ''}" alt="${escapeHtmlEntities(block.props.alt || '')}"${block.props.width ? ` width="${block.props.width}"` : ''} style="display:inline-block;max-width:100%;" /></div>`
        case 'markdown':
          return renderMarkdown(block.props.content || '')
        case 'container':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="width:100%;margin:16px auto;background-color:${block.props.backgroundColor || '#ffffff'};"><tr><td style="padding:${block.props.padding || '20px'};font-size:14px;line-height:24px;color:#374151;">${renderTextContent(block.props.content || '')}</td></tr></table>`
        case 'section':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td style="background-color:${block.props.backgroundColor || '#f9fafb'};padding:${block.props.padding || '16px'};border-radius:8px;font-size:14px;line-height:24px;color:#374151;">${renderTextContent(block.props.content || '')}</td></tr></table>`
        case 'columns':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td style="padding:0 8px;text-align:${block.props.textAlign || 'left'};font-size:14px;line-height:24px;color:#374151;">${renderTextContent(block.props.col1 || '')}</td><td style="padding:0 8px;text-align:${block.props.textAlign || 'left'};font-size:14px;line-height:24px;color:#374151;">${renderTextContent(block.props.col2 || '')}</td></tr></table>`
        case 'divider':
          return `<hr style="border:0;border-top:1px solid ${block.props.color || '#e5e7eb'};margin:20px 0;" />`
        case 'spacer':
          return `<div style="height:${block.props.height || '24px'};line-height:${block.props.height || '24px'};font-size:1px;">&nbsp;</div>`
        case 'code':
          return `<pre style="margin:16px 0;padding:16px;background-color:${block.props.backgroundColor || '#0f172a'};color:#e2e8f0;border-radius:8px;overflow-x:auto;font-family:SFMono-Regular,Menlo,Consolas,monospace;font-size:${block.props.fontSize || '13px'};line-height:1.6;"><code>${escapeHtmlEntities(block.props.content || '')}</code></pre>`
        case 'inlineCode':
          return `<code style="display:inline-block;margin:10px 0;padding:2px 7px;color:${block.props.color || '#111827'};background-color:${block.props.backgroundColor || '#f1f5f9'};border-radius:5px;font-family:SFMono-Regular,Menlo,Consolas,monospace;font-size:13px;line-height:20px;">${escapeHtmlEntities(block.props.content || '')}</code>`
        case 'preview':
          return `<div style="display:none;max-height:0;overflow:hidden;">${escapeHtmlEntities(block.props.content || '')}</div>`
        default:
          return ''
      }
    })
    .join('')

  return `<!DOCTYPE html><html lang="en"><head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" /></head><body><div style="max-width:600px;margin:0 auto;padding:20px;">${body}</div></body></html>`
}

function renderTextContent(value: string): string {
  return escapeHtmlEntities(value).replace(/\r\n|\r|\n/g, '<br />')
}
