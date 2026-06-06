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

  const blocks: EmailBlock[] = []
  let remaining = html.trim()

  const bodyMatch = /<body[^>]*>([\s\S]*)<\/body>/i.exec(remaining)
  if (bodyMatch) {
    remaining = bodyMatch[1].trim()
  } else {
    remaining = remaining
      .replace(/<!DOCTYPE[^>]*>/gi, '')
      .replace(/<html[^>]*>/gi, '')
      .replace(/<\/html>/gi, '')
      .replace(/<head[^>]*>[\s\S]*?<\/head>/gi, '')
      .replace(/<meta[^>]*>/gi, '')
      .replace(/<title>[^<]*<\/title>/gi, '')
      .replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '')
      .replace(/<link[^>]*>/gi, '')
      .replace(/<body[^>]*>/gi, '')
      .replace(/<\/body>/gi, '')
      .trim()
  }

  if (!remaining) return []

  const BLOCK_TAG_RE =
    /(<h[1-6][^>]*>[\s\S]*?<\/h[1-6]>|<pre[^>]*>[\s\S]*?<\/pre>|<code[^>]*>[\s\S]*?<\/code>|<p[^>]*>[\s\S]*?<\/p>|<a[^>]*href[^>]*>[\s\S]*?<\/a>|<img[^>]*\/?>|<hr[^>]*\/?>|<table[^>]*>[\s\S]*?<\/table>)/gi

  let match: RegExpExecArray | null
  let lastIndex = 0
  const tagRe = new RegExp(BLOCK_TAG_RE.source, 'gi')

  while ((match = tagRe.exec(remaining)) !== null) {
    if (match.index > lastIndex) {
      const before = remaining.slice(lastIndex, match.index).trim()
      if (before) {
        blocks.push(...extractTextBlocks(before))
      }
    }

    const tagHtml = match[0]
    const block = tagToBlock(tagHtml)
    if (block) blocks.push(block)
    lastIndex = tagRe.lastIndex
  }

  if (lastIndex < remaining.length) {
    const tail = remaining.slice(lastIndex).trim()
    if (tail) {
      blocks.push(...extractTextBlocks(tail))
    }
  }

  if (blocks.length === 0) {
    blocks.push({
      id: `block_custom_${Date.now()}`,
      type: 'text',
      props: { content: remaining }
    })
  }

  return blocks
}

function tagToBlock(html: string): EmailBlock | null {
  const id = `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`

  const preMatch = /<pre([^>]*)>([\s\S]*?)<\/pre>/i.exec(html)
  if (preMatch) {
    const style = extractAttr(preMatch[1], 'style')
    const inner = /<code[^>]*>([\s\S]*?)<\/code>/i.exec(preMatch[2])
    return {
      id,
      type: 'code',
      props: {
        content: decodeHtml(stripTags(inner ? inner[1] : preMatch[2])),
        lang: '',
        fontSize: extractCssValue(style, 'font-size') || '13px',
        backgroundColor: extractCssValue(style, 'background') || '#0f172a'
      }
    }
  }

  const inlineCodeMatch = /^<code([^>]*)>([\s\S]*?)<\/code>$/i.exec(html.trim())
  if (inlineCodeMatch) {
    const style = extractAttr(inlineCodeMatch[1], 'style')
    return {
      id,
      type: 'inlineCode',
      props: {
        content: decodeHtml(stripTags(inlineCodeMatch[2])),
        color: extractCssValue(style, 'color') || '#111827',
        backgroundColor: extractCssValue(style, 'background') || '#f1f5f9'
      }
    }
  }

  const headingMatch = /<h([1-6])([^>]*)>([\s\S]*?)<\/h\1>/i.exec(html)
  if (headingMatch) {
    const style = extractAttr(headingMatch[2], 'style')
    return {
      id,
      type: 'heading',
      props: {
        level: `h${headingMatch[1]}`,
        content: decodeHtml(stripTags(headingMatch[3])),
        textAlign: extractCssValue(style, 'text-align') || 'left',
        color: extractCssValue(style, 'color') || '#111827'
      }
    }
  }

  const hrMatch = /<hr([^>]*)\/?>/i.exec(html)
  if (hrMatch) {
    const style = extractAttr(hrMatch[1], 'style')
    return {
      id,
      type: 'divider',
      props: { color: extractCssValue(style, 'border-top-color') || '#e5e7eb' }
    }
  }

  const imgMatch = /<img([^>]*)\/?>/i.exec(html)
  if (imgMatch) {
    const attrs = imgMatch[1]
    return {
      id,
      type: 'image',
      props: {
        src: extractAttr(attrs, 'src') || '',
        alt: extractAttr(attrs, 'alt') || '',
        width: extractAttr(attrs, 'width') || '',
        textAlign: 'center'
      }
    }
  }

  const anchorMatch = /<a([^>]*)>([\s\S]*?)<\/a>/i.exec(html)
  if (anchorMatch) {
    const style = extractAttr(anchorMatch[1], 'style')
    const text = decodeHtml(stripTags(anchorMatch[2]))
    const href = extractAttr(anchorMatch[1], 'href') || ''
    const backgroundColor = extractCssValue(style, 'background-color')
    if (backgroundColor) {
      return {
        id,
        type: 'button',
        props: {
          content: text,
          href,
          textAlign: 'center',
          backgroundColor,
          color: extractCssValue(style, 'color') || '#ffffff'
        }
      }
    }
    return {
      id,
      type: 'link',
      props: {
        content: text || href,
        href,
        textAlign: extractCssValue(style, 'text-align') || 'left',
        color: extractCssValue(style, 'color') || '#2563eb'
      }
    }
  }

  const paragraphMatch = /<(p|div)([^>]*)>([\s\S]*?)<\/\1>/i.exec(html)
  if (paragraphMatch) {
    const style = extractAttr(paragraphMatch[2], 'style')
    const inner = paragraphMatch[3].trim()
    if (/<(strong|em|ul|ol|li|code|a)\b/i.test(inner)) {
      return {
        id,
        type: 'markdown',
        props: { content: decodeHtml(stripTagsWithLineBreaks(inner)).trim() }
      }
    }
    return {
      id,
      type: 'text',
      props: {
        content: decodeHtml(stripTags(inner)),
        textAlign: extractCssValue(style, 'text-align') || 'left',
        color: extractCssValue(style, 'color') || '#374151',
        fontSize: extractCssValue(style, 'font-size') || '14px'
      }
    }
  }

  const tableMatch = /<table[^>]*>([\s\S]*?)<\/table>/i.exec(html)
  if (tableMatch) {
    return {
      id,
      type: 'section',
      props: { backgroundColor: '#f9fafb', padding: '16px', content: decodeHtml(stripTags(tableMatch[1])) }
    }
  }

  return null
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

function extractAttr(attrs: string, name: string): string {
  const match = new RegExp(`${name}="([^"]*)"|${name}='([^']*)'|${name}=([^\\s>]+)`, 'i').exec(attrs)
  return match?.[1] || match?.[2] || match?.[3] || ''
}

function extractCssValue(style: string, name: string): string {
  const match = new RegExp(`${name}\\s*:\\s*([^;]+)`, 'i').exec(style)
  return match?.[1]?.trim() || ''
}

function stripTags(html: string): string {
  return html.replace(/<[^>]+>/g, '')
}

function stripTagsWithLineBreaks(html: string): string {
  return html
    .replace(/<(br|\/p|\/div|\/li)>/gi, '\n')
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
          return `<p style="margin:16px 0;text-align:${block.props.textAlign || 'left'};color:${block.props.color || '#374151'};font-size:${block.props.fontSize || '14px'};line-height:24px;">${escapeHtmlEntities(block.props.content || '')}</p>`
        case 'button':
          return `<div style="text-align:${block.props.textAlign || 'center'};margin:16px 0;"><a href="${block.props.href || '#'}" style="display:inline-block;padding:12px 24px;background-color:${block.props.backgroundColor || '#1d4ed8'};color:${block.props.color || '#ffffff'};border-radius:6px;text-decoration:none;font-weight:500;font-size:14px;">${escapeHtmlEntities(block.props.content || '')}</a></div>`
        case 'link':
          return `<a href="${block.props.href || '#'}" style="display:inline-block;color:${block.props.color || '#2563eb'};text-decoration:underline;text-align:${block.props.textAlign || 'left'};margin:12px 0;font-size:14px;">${escapeHtmlEntities(block.props.content || block.props.href || '')}</a>`
        case 'image':
          return `<div style="text-align:${block.props.textAlign || 'center'};margin:16px 0;"><img src="${block.props.src || ''}" alt="${escapeHtmlEntities(block.props.alt || '')}"${block.props.width ? ` width="${block.props.width}"` : ''} style="display:inline-block;max-width:100%;" /></div>`
        case 'markdown':
          return renderMarkdown(block.props.content || '')
        case 'container':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="width:100%;margin:16px auto;background-color:${block.props.backgroundColor || '#ffffff'};"><tr><td style="padding:${block.props.padding || '20px'};font-size:14px;line-height:24px;color:#374151;">${escapeHtmlEntities(block.props.content || '')}</td></tr></table>`
        case 'section':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td style="background-color:${block.props.backgroundColor || '#f9fafb'};padding:${block.props.padding || '16px'};border-radius:8px;font-size:14px;line-height:24px;color:#374151;">${escapeHtmlEntities(block.props.content || '')}</td></tr></table>`
        case 'columns':
          return `<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"><tr><td style="padding:0 8px;text-align:${block.props.textAlign || 'left'};font-size:14px;line-height:24px;color:#374151;">${escapeHtmlEntities(block.props.col1 || '')}</td><td style="padding:0 8px;text-align:${block.props.textAlign || 'left'};font-size:14px;line-height:24px;color:#374151;">${escapeHtmlEntities(block.props.col2 || '')}</td></tr></table>`
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
