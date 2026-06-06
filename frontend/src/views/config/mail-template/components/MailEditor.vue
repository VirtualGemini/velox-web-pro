<template>
  <div class="mail-editor">
    <!-- ══ 顶部工具栏 ══ -->
    <header class="mail-editor__toolbar">
      <div class="mail-editor__toolbar-group mail-editor__toolbar-left">
        <ElButton text :icon="ArrowLeft" class="mail-editor__back" @click="$emit('back')">
          {{ t('pages.config.mailTemplate.editor.backToList') }}
        </ElButton>
        <span class="mail-editor__divider-v" />
        <span class="mail-editor__title">{{ props.template.name }}</span>
      </div>

      <div class="mail-editor__toolbar-group mail-editor__toolbar-center">
        <!-- 桌面/平板/手机预览切换 -->
        <div class="mail-editor__segment">
          <button
            type="button"
            class="mail-editor__segment-btn"
            :class="{ 'is-active': viewport === 'desktop' }"
            @click="viewport = 'desktop'"
          >
            <ElIcon><Monitor /></ElIcon>
            {{ t('pages.config.mailTemplate.editor.desktop') }}
          </button>
          <button
            type="button"
            class="mail-editor__segment-btn"
            :class="{ 'is-active': viewport === 'tablet' }"
            @click="viewport = 'tablet'"
          >
            <ElIcon><Notebook /></ElIcon>
            {{ t('pages.config.mailTemplate.editor.tablet') }}
          </button>
          <button
            type="button"
            class="mail-editor__segment-btn"
            :class="{ 'is-active': viewport === 'mobile' }"
            @click="viewport = 'mobile'"
          >
            <ElIcon><Cellphone /></ElIcon>
            {{ t('pages.config.mailTemplate.editor.mobile') }}
          </button>
        </div>
      </div>

      <div class="mail-editor__toolbar-group mail-editor__toolbar-right">
        <span class="mail-editor__divider-v" />

        <ElTooltip
          :content="t('pages.config.mailTemplate.editor.switchLanguage')"
          placement="bottom"
        >
          <button type="button" class="mail-editor__lang-toggle" @click="toggleLanguage">
            <ElIcon><Switch /></ElIcon>
            <span>{{ activeLanguage }}</span>
          </button>
        </ElTooltip>
        <ElButton :icon="View" @click="openPreview" :disabled="blocks.length === 0">
          {{ t('pages.config.mailTemplate.editor.preview') }}
        </ElButton>
        <ElButton type="primary" :icon="Check" :loading="saving" @click="handleSave">
          {{
            saving
              ? t('pages.config.mailTemplate.editor.saving')
              : t('pages.config.mailTemplate.editor.save')
          }}
        </ElButton>
      </div>
    </header>

    <div class="mail-editor__workspace">
      <!-- ══ 左栏：组件调色板 + 图层 ══ -->
      <aside class="mail-editor__sidebar mail-editor__sidebar--left">
        <div class="mail-editor__left-tabs">
          <button
            type="button"
            class="mail-editor__left-tab"
            :class="{ 'is-active': leftTab === 'components' }"
            @click="leftTab = 'components'"
          >
            {{ t('pages.config.mailTemplate.editor.paletteTitle') }}
          </button>
          <button
            type="button"
            class="mail-editor__left-tab"
            :class="{ 'is-active': leftTab === 'layers' }"
            @click="leftTab = 'layers'"
          >
            {{ t('pages.config.mailTemplate.editor.layersTitle') }}
          </button>
        </div>

        <div v-show="leftTab === 'components'" class="mail-editor__left-panel">
          <!-- 组件调色板（分类） -->
          <section v-for="cat in EMAIL_BLOCK_CATEGORIES" :key="cat" class="mail-editor__pane">
            <div class="mail-editor__pane-head">
              {{ t(`pages.config.mailTemplate.editor.categories.${cat}`) }}
            </div>
            <div class="mail-editor__palette">
              <button
                v-for="bt in getBlockTypesByCategory(cat)"
                :key="bt.type"
                type="button"
                class="mail-editor__palette-item"
                draggable="true"
                @click="addBlock(bt.type)"
                @dragstart="onPaletteDragStart(bt.type, $event)"
                @dragend="onPaletteDragEnd"
              >
                <span class="mail-editor__palette-icon">
                  <ElIcon><component :is="blockIcon(bt.icon)" /></ElIcon>
                </span>
                <span class="mail-editor__palette-label">{{ t(bt.labelKey) }}</span>
              </button>
            </div>
          </section>
        </div>

        <!-- 图层列表 -->
        <section v-show="leftTab === 'layers'" class="mail-editor__pane mail-editor__pane--layers">
          <div class="mail-editor__layers">
            <div v-if="blocks.length === 0" class="mail-editor__layers-empty">
              {{ t('pages.config.mailTemplate.editor.emptyLayers') }}
            </div>
            <VueDraggable
              v-else
              v-model="blocks"
              :animation="160"
              handle=".mail-editor__layer-drag"
              class="mail-editor__layer-list"
            >
              <div
                v-for="block in blocks"
                :key="block.id"
                class="mail-editor__layer"
                :class="{ 'is-selected': selectedId === block.id }"
                @click="selectBlock(block.id)"
              >
                <span class="mail-editor__layer-drag">
                  <ElIcon><Rank /></ElIcon>
                </span>
                <span class="mail-editor__layer-icon">
                  <ElIcon
                    ><component :is="blockIcon(getBlockTypeMeta(block.type)?.icon || '')"
                  /></ElIcon>
                </span>
                <span class="mail-editor__layer-label">{{ blockLabel(block) }}</span>
                <ElIcon class="mail-editor__layer-del" @click.stop="removeBlock(block.id)">
                  <Close />
                </ElIcon>
              </div>
            </VueDraggable>
          </div>
        </section>
      </aside>

      <!-- ══ 中栏：画布 ══ -->
      <main class="mail-editor__canvas" @click="selectBlock(null)">
        <div
          class="mail-editor__paper"
          :class="`is-${viewport}`"
          @dragover.prevent="onCanvasDragOver"
          @drop.prevent="onCanvasDrop"
        >
          <!-- 邮件头部信息条 -->
          <div class="mail-editor__mail-head">
            <div class="mail-editor__mail-subject">{{ renderedSubject || '—' }}</div>
            <div class="mail-editor__mail-from">
              {{ previewVars.appName }} &lt;{{ props.metadata.previewSample.fromAddress }}&gt;
            </div>
          </div>

          <div class="mail-editor__mail-body">
            <!-- 空状态 -->
            <div v-if="blocks.length === 0" class="mail-editor__empty">
              <ElIcon class="mail-editor__empty-icon"><Plus /></ElIcon>
              <p>{{ t('pages.config.mailTemplate.editor.emptyState') }}</p>
            </div>

            <!-- 渲染所有区块 -->
            <div
              v-for="(block, index) in blocks"
              v-else
              :key="block.id"
              class="mail-editor__node"
              :class="{ 'is-selected': selectedId === block.id }"
              @click.stop="selectBlock(block.id)"
            >
              <!-- 选中态浮动工具条 -->
              <div v-if="selectedId === block.id" class="mail-editor__node-tools" @click.stop>
                <span class="mail-editor__node-badge">{{ blockLabel(block) }}</span>
                <div class="mail-editor__node-actions">
                  <ElIcon :class="{ 'is-disabled': index === 0 }" @click="moveBlock(block.id, -1)">
                    <Top />
                  </ElIcon>
                  <ElIcon
                    :class="{ 'is-disabled': index === blocks.length - 1 }"
                    @click="moveBlock(block.id, 1)"
                  >
                    <Bottom />
                  </ElIcon>
                  <ElIcon @click="duplicateBlock(block.id)"><CopyDocument /></ElIcon>
                  <ElIcon class="mail-editor__node-danger" @click="removeBlock(block.id)">
                    <Delete />
                  </ElIcon>
                </div>
              </div>

              <!--
                区块内容用 @vue-email/components 渲染。
                只渲染叶子组件，不套 Html/Body/Container 外壳。
              -->

              <!-- heading -->
              <component
                :is="EmailHeading"
                v-if="block.type === 'heading'"
                :as="(block.props.level as 'h1' | 'h2' | 'h3') || 'h2'"
                :style="headingStyle(block)"
                >{{ substituteVars(block.props.content) }}</component
              >

              <!-- text -->
              <component
                :is="EmailText"
                v-else-if="block.type === 'text'"
                :style="textStyle(block)"
                >{{ substituteVars(block.props.content) }}</component
              >

              <!-- button -->
              <div v-else-if="block.type === 'button'" :style="alignWrapStyle(block)">
                <component
                  :is="EmailButton"
                  :href="block.props.href || '#'"
                  :style="buttonStyle(block)"
                  >{{ substituteVars(block.props.content) }}</component
                >
              </div>

              <!-- link -->
              <component
                :is="EmailLink"
                v-else-if="block.type === 'link'"
                :href="block.props.href || '#'"
                :style="linkStyle(block)"
                >{{ substituteVars(block.props.content || block.props.href || '') }}</component
              >

              <!-- image -->
              <div v-else-if="block.type === 'image'" :style="alignWrapStyle(block)">
                <component
                  :is="EmailImg"
                  :src="block.props.src || PLACEHOLDER_IMG"
                  :alt="block.props.alt"
                  :width="block.props.width || undefined"
                  :style="{ display: 'inline-block', maxWidth: '100%' }"
                />
              </div>

              <!-- markdown（用与序列化一致的 renderMarkdown，保证预览==产物） -->
              <component
                :is="EmailMarkdown"
                v-else-if="block.type === 'markdown'"
                :source="substituteVars(block.props.content)"
                :markdown-custom-styles="markdownStyles"
                :markdown-container-styles="markdownContainerStyle"
              />

              <!-- inline code -->
              <component
                :is="EmailCodeInline"
                v-else-if="block.type === 'inlineCode'"
                :style="inlineCodeStyle(block)"
                >{{ substituteVars(block.props.content) }}</component
              >

              <!-- container -->
              <component
                :is="EmailContainer"
                v-else-if="block.type === 'container'"
                :style="containerStyle(block)"
              >
                <div :style="containerInnerStyle(block)">
                  {{ substituteVars(block.props.content) }}
                </div>
              </component>

              <!-- section -->
              <component :is="EmailSection" v-else-if="block.type === 'section'">
                <div :style="sectionStyle(block)">
                  {{
                    substituteVars(block.props.content) ||
                    t('pages.config.mailTemplate.editor.blockTypes.section')
                  }}
                </div>
              </component>

              <!-- columns（Row + 两个 Column） -->
              <component :is="EmailRow" v-else-if="block.type === 'columns'">
                <component :is="EmailColumn">
                  <div :style="columnCellStyle(block)">{{ substituteVars(block.props.col1) }}</div>
                </component>
                <component :is="EmailColumn">
                  <div :style="columnCellStyle(block)">{{ substituteVars(block.props.col2) }}</div>
                </component>
              </component>

              <!-- divider -->
              <component
                :is="EmailHr"
                v-else-if="block.type === 'divider'"
                :style="{ borderTopColor: block.props.color || '#e5e7eb', margin: '20px 0' }"
              />

              <!-- spacer -->
              <div
                v-else-if="block.type === 'spacer'"
                :style="{
                  height: block.props.height || '24px',
                  lineHeight: block.props.height || '24px',
                  fontSize: '1px'
                }"
                >&nbsp;</div
              >

              <!-- code block -->
              <Suspense v-else-if="block.type === 'code'">
                <component
                  :is="EmailCodeBlock"
                  :code="block.props.content || ''"
                  :lang="codeLanguage(block)"
                  theme="github-dark"
                  :show-line-numbers="false"
                />
                <template #fallback>
                  <pre
                    class="mail-editor__code"
                    :style="codeStyle(block)"
                  ><code>{{ block.props.content }}</code></pre>
                </template>
              </Suspense>

              <!-- preview（preheader） -->
              <component :is="EmailPreview" v-else-if="block.type === 'preview'">{{
                block.props.content
              }}</component>
            </div>
          </div>
        </div>
      </main>

      <!-- ══ 右栏：属性 / 源码 ══ -->
      <aside class="mail-editor__sidebar mail-editor__sidebar--right">
        <div class="mail-editor__tabs">
          <button
            type="button"
            class="mail-editor__tab"
            :class="{ 'is-active': rightTab === 'props' }"
            @click="rightTab = 'props'"
          >
            {{ t('pages.config.mailTemplate.editor.propertiesTitle') }}
          </button>
          <button
            type="button"
            class="mail-editor__tab"
            :class="{ 'is-active': rightTab === 'source' }"
            @click="rightTab = 'source'"
          >
            {{ t('pages.config.mailTemplate.editor.sourceTitle') }}
          </button>
        </div>

        <!-- 属性面板 -->
        <div v-show="rightTab === 'props'" class="mail-editor__panel-scroll">
          <div v-if="!selectedBlock" class="mail-editor__placeholder">
            <ElIcon class="mail-editor__placeholder-icon"><Pointer /></ElIcon>
            <p>{{ t('pages.config.mailTemplate.editor.noBlockSelected') }}</p>
          </div>

          <template v-else>
            <!-- 内容组 -->
            <div
              v-if="
                hasAnyProp([
                  'content',
                  'level',
                  'href',
                  'src',
                  'alt',
                  'width',
                  'col1',
                  'col2',
                  'lang'
                ])
              "
              class="mail-editor__group"
            >
              <div class="mail-editor__group-title">
                {{ t('pages.config.mailTemplate.editor.groups.content') }}
              </div>

              <!-- 标题级别 -->
              <div v-if="hasProp('level')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.level') }}</label>
                <ElSelect
                  :model-value="selectedBlock.props.level || 'h2'"
                  size="small"
                  @update:model-value="setProp('level', $event)"
                >
                  <ElOption label="H1" value="h1" />
                  <ElOption label="H2" value="h2" />
                  <ElOption label="H3" value="h3" />
                </ElSelect>
              </div>

              <!-- 内容文本（通用，block 类型决定 placeholder 和样式） -->
              <div v-if="hasProp('content')" class="mail-editor__field">
                <div class="mail-editor__field-head">
                  <label>{{ t('pages.config.mailTemplate.editor.properties.content') }}</label>
                  <span v-if="selectedBlock.type === 'code'" class="mail-editor__field-badge"
                    >code</span
                  >
                  <span
                    v-else-if="selectedBlock.type === 'markdown'"
                    class="mail-editor__field-badge"
                    >md</span
                  >
                </div>
                <ElInput
                  :model-value="selectedBlock.props.content"
                  type="textarea"
                  :rows="selectedBlock.type === 'code' || selectedBlock.type === 'markdown' ? 8 : 4"
                  resize="vertical"
                  :class="{
                    'mail-editor__input-mono': selectedBlock.type === 'code'
                  }"
                  :placeholder="contentPlaceholder"
                  @update:model-value="setProp('content', $event)"
                />
                <div v-if="selectedBlock.type !== 'code'" class="mail-editor__var-chips">
                  <button
                    v-for="v in props.metadata.variables"
                    :key="v"
                    type="button"
                    class="mail-editor__chip"
                    @click="insertVariable(v)"
                  >
                    {{ variableToken(v) }}
                  </button>
                </div>
              </div>

              <!-- 链接 -->
              <div v-if="hasProp('href')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.href') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.href"
                  size="small"
                  :placeholder="t('pages.config.mailTemplate.editor.properties.hrefPlaceholder')"
                  @update:model-value="setProp('href', $event)"
                />
              </div>

              <!-- 图片地址 -->
              <div v-if="hasProp('src')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.src') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.src"
                  size="small"
                  :placeholder="t('pages.config.mailTemplate.editor.properties.srcPlaceholder')"
                  @update:model-value="setProp('src', $event)"
                />
              </div>

              <!-- 图片 alt -->
              <div v-if="hasProp('alt')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.alt') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.alt"
                  size="small"
                  :placeholder="t('pages.config.mailTemplate.editor.properties.altPlaceholder')"
                  @update:model-value="setProp('alt', $event)"
                />
              </div>

              <!-- 图片宽度 -->
              <div v-if="hasProp('width')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.width') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.width"
                  size="small"
                  :placeholder="t('pages.config.mailTemplate.editor.properties.widthPlaceholder')"
                  @update:model-value="setProp('width', $event)"
                />
              </div>

              <!-- 代码语言 -->
              <div v-if="hasProp('lang')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.lang') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.lang"
                  size="small"
                  placeholder="javascript"
                  @update:model-value="setProp('lang', $event)"
                />
              </div>

              <!-- 左列内容 -->
              <div v-if="hasProp('col1')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.col1') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.col1"
                  type="textarea"
                  :rows="3"
                  resize="vertical"
                  @update:model-value="setProp('col1', $event)"
                />
              </div>

              <!-- 右列内容 -->
              <div v-if="hasProp('col2')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.col2') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.col2"
                  type="textarea"
                  :rows="3"
                  resize="vertical"
                  @update:model-value="setProp('col2', $event)"
                />
              </div>
            </div>

            <!-- 样式组 -->
            <div
              v-if="
                hasAnyProp([
                  'textAlign',
                  'color',
                  'fontSize',
                  'backgroundColor',
                  'padding',
                  'height'
                ])
              "
              class="mail-editor__group"
            >
              <div class="mail-editor__group-title">
                {{ t('pages.config.mailTemplate.editor.groups.style') }}
              </div>

              <!-- 对齐 -->
              <div v-if="hasProp('textAlign')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.textAlign') }}</label>
                <div class="mail-editor__align">
                  <button
                    v-for="a in alignOptions"
                    :key="a.value"
                    type="button"
                    class="mail-editor__align-btn"
                    :class="{ 'is-active': (selectedBlock.props.textAlign || 'left') === a.value }"
                    :title="t(a.labelKey)"
                    @click="setProp('textAlign', a.value)"
                  >
                    <ElIcon><component :is="a.icon" /></ElIcon>
                  </button>
                </div>
              </div>

              <!-- 字号 -->
              <div v-if="hasProp('fontSize')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.fontSize') }}</label>
                <ElSelect
                  :model-value="selectedBlock.props.fontSize || '14px'"
                  size="small"
                  @update:model-value="setProp('fontSize', $event)"
                >
                  <ElOption v-for="s in fontSizes" :key="s" :label="s" :value="s" />
                </ElSelect>
              </div>

              <!-- 文本/标题颜色 -->
              <div v-if="hasProp('color')" class="mail-editor__field mail-editor__field--inline">
                <label>{{ t('pages.config.mailTemplate.editor.properties.color') }}</label>
                <ElColorPicker
                  :model-value="selectedBlock.props.color || '#374151'"
                  @update:model-value="setProp('color', $event || '')"
                />
              </div>

              <!-- 背景色 -->
              <div
                v-if="hasProp('backgroundColor')"
                class="mail-editor__field mail-editor__field--inline"
              >
                <label>{{
                  t('pages.config.mailTemplate.editor.properties.backgroundColor')
                }}</label>
                <ElColorPicker
                  :model-value="selectedBlock.props.backgroundColor || '#ffffff'"
                  @update:model-value="setProp('backgroundColor', $event || '')"
                />
              </div>

              <!-- 内边距 -->
              <div v-if="hasProp('padding')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.padding') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.padding"
                  size="small"
                  placeholder="16px"
                  @update:model-value="setProp('padding', $event)"
                />
              </div>

              <!-- 高度（spacer） -->
              <div v-if="hasProp('height')" class="mail-editor__field">
                <label>{{ t('pages.config.mailTemplate.editor.properties.height') }}</label>
                <ElInput
                  :model-value="selectedBlock.props.height"
                  size="small"
                  placeholder="24px"
                  @update:model-value="setProp('height', $event)"
                />
              </div>
            </div>
          </template>
        </div>

        <!-- 源码面板 -->
        <div v-show="rightTab === 'source'" class="mail-editor__panel-scroll">
          <div class="mail-editor__source-tip">
            {{ t('pages.config.mailTemplate.editor.sourceReadonly') }}
          </div>
          <pre class="mail-editor__source"><code>{{ sourceHtml }}</code></pre>
        </div>
      </aside>
    </div>

    <!-- 预览弹窗 -->
    <MailPreviewDialog
      v-model:visible="previewVisible"
      :metadata="props.metadata"
      :type="sendType"
      :language="activeLanguage"
      :subject="currentSubject"
      :content="sourceHtml"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, h, defineComponent, markRaw, watch } from 'vue'
  import type { CSSProperties } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElColorPicker, ElIcon } from 'element-plus'
  import {
    ArrowLeft,
    Check,
    View,
    Monitor,
    Notebook,
    Cellphone,
    EditPen,
    Document,
    Link as LinkIcon,
    Picture,
    SemiSelect,
    Grid,
    Top,
    Bottom,
    Delete,
    Close,
    Rank,
    Plus,
    Pointer,
    CopyDocument,
    Position,
    Operation,
    Cpu,
    Memo,
    Switch,
    Box,
    Tickets
  } from '@element-plus/icons-vue'
  import { VueDraggable } from 'vue-draggable-plus'
  import {
    Section as EmailSection,
    Text as EmailText,
    Heading as EmailHeading,
    Button as EmailButton,
    Hr as EmailHr,
    Img as EmailImg,
    Link as EmailLink,
    Row as EmailRow,
    Column as EmailColumn,
    Preview as EmailPreview,
    Container as EmailContainer,
    Markdown as EmailMarkdown,
    CodeBlock as EmailCodeBlock,
    CodeInline as EmailCodeInline
  } from '@vue-email/components'
  import type { MailTemplate, MailTemplateLanguage, MailTemplateMetadata } from '@/api/mailTemplate'
  import { fetchMailTemplateUpdate } from '@/api/mailTemplate'
  import {
    type EmailBlock,
    type EmailBlockType,
    EMAIL_BLOCK_CATEGORIES,
    createBlock,
    parseHtmlToBlocks,
    serializeBlocksToHtml,
    replaceVariables,
    getBlockTypeMeta,
    getBlockTypesByCategory
  } from '../editor-constants'
  import { buildPreviewVariables } from '../metadata'
  import MailPreviewDialog from './MailPreviewDialog.vue'

  const props = defineProps<{
    template: MailTemplate & { subject: string; content: string }
    metadata: MailTemplateMetadata
  }>()

  const emit = defineEmits<{
    back: []
    saved: []
  }>()

  const { t } = useI18n()

  const PLACEHOLDER_IMG = 'https://placehold.co/600x200/eef2ff/6366f1?text=Image'
  const DRAFT_PREFIX = 'velox:mail-template:draft:'

  // ── 图标映射 ──
  const ICON_MAP: Record<string, unknown> = {
    EditPen,
    Document,
    Link: LinkIcon,
    Picture,
    SemiSelect,
    Grid,
    Position,
    Operation,
    Rank,
    Cpu,
    Memo,
    View,
    Box,
    Tickets
  }
  function blockIcon(name: string) {
    return ICON_MAP[name] || Document
  }

  const alignOptions = [
    {
      value: 'left',
      icon: markRaw(defineAlignIcon('left')),
      labelKey: 'pages.config.mailTemplate.editor.properties.alignLeft'
    },
    {
      value: 'center',
      icon: markRaw(defineAlignIcon('center')),
      labelKey: 'pages.config.mailTemplate.editor.properties.alignCenter'
    },
    {
      value: 'right',
      icon: markRaw(defineAlignIcon('right')),
      labelKey: 'pages.config.mailTemplate.editor.properties.alignRight'
    }
  ]
  const fontSizes = ['12px', '13px', '14px', '16px', '18px', '20px', '24px', '28px', '32px']
  const markdownStyles = {
    p: { margin: '10px 0', fontSize: '14px', lineHeight: '24px', color: '#374151' },
    link: { color: '#2563eb', textDecoration: 'underline' },
    codeInline: {
      background: '#f1f5f9',
      padding: '1px 5px',
      borderRadius: '4px',
      fontSize: '13px'
    }
  }
  const markdownContainerStyle = {
    margin: '16px 0',
    color: '#374151'
  }

  // ── 状态 ──
  const activeLanguage = ref<MailTemplateLanguage>(props.template.language === 'en' ? 'en' : 'zh')
  const blocksByLanguage = ref<Record<MailTemplateLanguage, EmailBlock[]>>({
    zh: [],
    en: []
  })
  const subjects = ref<Record<MailTemplateLanguage, string>>({
    zh: props.template.subjectZh || '',
    en: props.template.subjectEn || ''
  })
  const selectedId = ref<string | null>(null)
  const saving = ref(false)
  const previewVisible = ref(false)
  const viewport = ref<'desktop' | 'tablet' | 'mobile'>('desktop')
  const leftTab = ref<'components' | 'layers'>('components')
  const rightTab = ref<'props' | 'source'>('props')

  function initBlocks() {
    subjects.value = {
      zh: resolveInitialSubject('zh'),
      en: resolveInitialSubject('en')
    }
    blocksByLanguage.value = {
      zh: parseHtmlToBlocks(resolveInitialContent('zh')),
      en: parseHtmlToBlocks(resolveInitialContent('en'))
    }
    restoreDraft()
  }
  initBlocks()

  function draftKey() {
    return props.template.id ? `${DRAFT_PREFIX}${props.template.id}` : ''
  }

  function resolveInitialSubject(language: MailTemplateLanguage) {
    if (language === 'en') {
      return props.template.subjectEn || props.template.subjectZh || props.template.subject || ''
    }
    return props.template.subjectZh || props.template.subjectEn || props.template.subject || ''
  }

  function resolveInitialContent(language: MailTemplateLanguage) {
    if (language === 'en') {
      return props.template.contentEn || props.template.contentZh || props.template.content || ''
    }
    return props.template.contentZh || props.template.contentEn || props.template.content || ''
  }

  function restoreDraft() {
    const key = draftKey()
    if (!key) return
    try {
      const raw = window.localStorage.getItem(key)
      if (!raw) return
      const draft = JSON.parse(raw) as {
        activeLanguage?: MailTemplateLanguage
        viewport?: 'desktop' | 'tablet' | 'mobile'
        subjects?: Record<MailTemplateLanguage, string>
        blocksByLanguage?: Record<MailTemplateLanguage, EmailBlock[]>
      }
      if (draft.activeLanguage === 'zh' || draft.activeLanguage === 'en') {
        activeLanguage.value = draft.activeLanguage
      }
      if (
        draft.viewport === 'desktop' ||
        draft.viewport === 'tablet' ||
        draft.viewport === 'mobile'
      ) {
        viewport.value = draft.viewport
      }
      if (draft.subjects) {
        subjects.value = {
          zh: draft.subjects.zh || subjects.value.zh,
          en: draft.subjects.en || subjects.value.en
        }
      }
      if (draft.blocksByLanguage) {
        blocksByLanguage.value = {
          zh: Array.isArray(draft.blocksByLanguage.zh)
            ? draft.blocksByLanguage.zh
            : blocksByLanguage.value.zh,
          en: Array.isArray(draft.blocksByLanguage.en)
            ? draft.blocksByLanguage.en
            : blocksByLanguage.value.en
        }
      }
    } catch {
      // Invalid local drafts are ignored; the server copy remains authoritative.
    }
  }

  function persistDraft() {
    const key = draftKey()
    if (!key) return
    window.localStorage.setItem(
      key,
      JSON.stringify({
        activeLanguage: activeLanguage.value,
        viewport: viewport.value,
        subjects: subjects.value,
        blocksByLanguage: blocksByLanguage.value,
        updatedAt: Date.now()
      })
    )
  }

  watch(
    [activeLanguage, viewport, subjects, blocksByLanguage],
    () => {
      persistDraft()
    },
    { deep: true }
  )

  // ── 计算属性 ──
  const sendType = computed(() => props.template.sendType || props.template.type)
  const blocks = computed<EmailBlock[]>({
    get: () => blocksByLanguage.value[activeLanguage.value],
    set: (value) => {
      blocksByLanguage.value[activeLanguage.value] = value
    }
  })

  const selectedBlock = computed(() =>
    selectedId.value ? blocks.value.find((b) => b.id === selectedId.value) || null : null
  )

  const previewVars = computed(() =>
    buildPreviewVariables(props.metadata, sendType.value, activeLanguage.value)
  )

  const renderedSubject = computed(() =>
    replaceVariables(currentSubject.value, previewVars.value, false)
  )

  const sourceHtml = computed(() => serializeBlocksToHtml(blocks.value))
  const currentSubject = computed(() => subjects.value[activeLanguage.value] || '')

  /** 根据 block 类型返回对应的 placeholder key */
  const contentPlaceholder = computed(() => {
    const type = selectedBlock.value?.type
    if (type === 'markdown')
      return t('pages.config.mailTemplate.editor.properties.markdownPlaceholder')
    if (type === 'code') return t('pages.config.mailTemplate.editor.properties.codePlaceholder')
    if (type === 'preview')
      return t('pages.config.mailTemplate.editor.properties.previewPlaceholder')
    return t('pages.config.mailTemplate.editor.properties.contentPlaceholder')
  })

  // ── Block 操作 ──
  function addBlock(type: EmailBlockType, atIndex?: number) {
    const block = createBlock(type)
    if (typeof atIndex === 'number' && atIndex >= 0 && atIndex <= blocks.value.length) {
      blocks.value.splice(atIndex, 0, block)
    } else {
      blocks.value.push(block)
    }
    selectedId.value = block.id
    rightTab.value = 'props'
  }

  function selectBlock(id: string | null) {
    selectedId.value = id
    if (id) rightTab.value = 'props'
  }

  function removeBlock(id: string) {
    const idx = blocks.value.findIndex((b) => b.id === id)
    if (idx !== -1) {
      blocks.value.splice(idx, 1)
      if (selectedId.value === id) selectedId.value = null
    }
  }

  function duplicateBlock(id: string) {
    const idx = blocks.value.findIndex((b) => b.id === id)
    if (idx === -1) return
    const src = blocks.value[idx]
    const copy: EmailBlock = {
      id: `block_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
      type: src.type,
      props: { ...src.props }
    }
    blocks.value.splice(idx + 1, 0, copy)
    selectedId.value = copy.id
  }

  function moveBlock(id: string, direction: -1 | 1) {
    const idx = blocks.value.findIndex((b) => b.id === id)
    if (idx === -1) return
    const newIdx = idx + direction
    if (newIdx < 0 || newIdx >= blocks.value.length) return
    const [block] = blocks.value.splice(idx, 1)
    blocks.value.splice(newIdx, 0, block)
  }

  function setProp(prop: string, value: string) {
    const block = selectedBlock.value
    if (block) block.props[prop] = value
  }

  function hasProp(prop: string): boolean {
    const block = selectedBlock.value
    if (!block) return false
    const meta = getBlockTypeMeta(block.type)
    return meta ? Object.prototype.hasOwnProperty.call(meta.defaultProps, prop) : false
  }

  function hasAnyProp(propsList: string[]): boolean {
    return propsList.some((p) => hasProp(p))
  }

  function blockLabel(block: EmailBlock): string {
    const meta = getBlockTypeMeta(block.type)
    if (!meta) return block.type
    const label = t(meta.labelKey)
    // 给部分类型显示预览片段
    if (block.type === 'heading' || block.type === 'text' || block.type === 'button') {
      const preview = (block.props.content || '').slice(0, 20)
      return preview ? `${label} — ${preview}` : label
    }
    return label
  }

  // ── 变量 ──
  function variableToken(name: string): string {
    return `{{${name}}}`
  }

  function insertVariable(name: string) {
    const block = selectedBlock.value
    if (!block) return
    block.props.content = (block.props.content || '') + `{{${name}}}`
  }

  function toggleLanguage() {
    activeLanguage.value = activeLanguage.value === 'zh' ? 'en' : 'zh'
    selectedId.value = null
    rightTab.value = 'props'
  }

  // ── 拖拽（调色板 → 画布） ──
  const draggingType = ref<EmailBlockType | null>(null)
  function onPaletteDragStart(type: EmailBlockType, e: DragEvent) {
    draggingType.value = type
    e.dataTransfer?.setData('text/plain', type)
    if (e.dataTransfer) e.dataTransfer.effectAllowed = 'copy'
  }
  function onPaletteDragEnd() {
    draggingType.value = null
  }
  function onCanvasDragOver(e: DragEvent) {
    if (draggingType.value && e.dataTransfer) e.dataTransfer.dropEffect = 'copy'
  }
  function onCanvasDrop() {
    if (draggingType.value) {
      addBlock(draggingType.value)
      draggingType.value = null
    }
  }

  // ── 预览渲染辅助 ──
  function substituteVars(text: string | undefined): string {
    if (!text) return ''
    return replaceVariables(text, previewVars.value, false)
  }

  // ── 组件样式映射 ──
  function headingStyle(block: EmailBlock): Record<string, string> {
    return {
      textAlign: block.props.textAlign || 'left',
      color: block.props.color || '#111827',
      margin: '16px 0 8px',
      fontWeight: '600'
    }
  }

  function textStyle(block: EmailBlock): Record<string, string> {
    return {
      textAlign: block.props.textAlign || 'left',
      color: block.props.color || '#374151',
      fontSize: block.props.fontSize || '14px',
      lineHeight: '24px',
      margin: '16px 0'
    }
  }

  function buttonStyle(block: EmailBlock): Record<string, string> {
    return {
      display: 'inline-block',
      padding: '12px 24px',
      backgroundColor: block.props.backgroundColor || '#1d4ed8',
      color: block.props.color || '#ffffff',
      borderRadius: '6px',
      textDecoration: 'none',
      fontWeight: '500',
      fontSize: '14px'
    }
  }

  function linkStyle(block: EmailBlock): Record<string, string> {
    return {
      display: 'inline-block',
      color: block.props.color || '#2563eb',
      textDecoration: 'underline',
      textAlign: block.props.textAlign || 'left',
      margin: '12px 0',
      fontSize: '14px'
    }
  }

  function normalizeTextAlign(
    value: string | undefined,
    fallback: CSSProperties['textAlign'] = 'left'
  ): CSSProperties['textAlign'] {
    return value === 'left' || value === 'center' || value === 'right' ? value : fallback
  }

  function alignWrapStyle(block: EmailBlock): CSSProperties {
    return {
      textAlign: normalizeTextAlign(block.props.textAlign, 'center'),
      margin: '16px 0'
    }
  }

  function sectionStyle(block: EmailBlock): Record<string, string> {
    return {
      backgroundColor: block.props.backgroundColor || '#f9fafb',
      padding: block.props.padding || '16px',
      borderRadius: '8px',
      fontSize: '14px',
      lineHeight: '24px',
      color: '#374151'
    }
  }

  function containerStyle(block: EmailBlock): CSSProperties {
    return {
      width: '100%',
      margin: '16px auto',
      backgroundColor: block.props.backgroundColor || '#ffffff'
    }
  }

  function containerInnerStyle(block: EmailBlock): Record<string, string> {
    return {
      padding: block.props.padding || '20px',
      fontSize: '14px',
      lineHeight: '24px',
      color: '#374151'
    }
  }

  function inlineCodeStyle(block: EmailBlock): CSSProperties {
    return {
      display: 'inline-block',
      margin: '10px 0',
      padding: '2px 7px',
      color: block.props.color || '#111827',
      backgroundColor: block.props.backgroundColor || '#f1f5f9',
      borderRadius: '5px',
      fontFamily: 'SFMono-Regular, Menlo, Consolas, monospace',
      fontSize: '13px',
      lineHeight: '20px'
    }
  }

  function columnCellStyle(block: EmailBlock): Record<string, string> {
    return {
      padding: '0 8px',
      textAlign: block.props.textAlign || 'left',
      fontSize: '14px',
      lineHeight: '24px',
      color: '#374151'
    }
  }

  function codeStyle(block: EmailBlock): Record<string, string> {
    return {
      margin: '16px 0',
      padding: '16px',
      backgroundColor: block.props.backgroundColor || '#0f172a',
      color: '#e2e8f0',
      borderRadius: '8px',
      overflowX: 'auto',
      fontFamily: 'SFMono-Regular, Menlo, Consolas, monospace',
      fontSize: block.props.fontSize || '13px',
      lineHeight: '1.6'
    }
  }

  type CodeBlockProps = InstanceType<typeof EmailCodeBlock>['$props']
  function codeLanguage(block: EmailBlock): CodeBlockProps['lang'] {
    return (block.props.lang || 'javascript') as CodeBlockProps['lang']
  }

  // ── 保存 ──
  async function handleSave() {
    saving.value = true
    try {
      await fetchMailTemplateUpdate({
        id: props.template.id,
        name: props.template.name,
        sendType: sendType.value,
        language: activeLanguage.value,
        subject: currentSubject.value,
        content: sourceHtml.value,
        subjectZh: subjects.value.zh,
        contentZh: serializeBlocksToHtml(blocksByLanguage.value.zh),
        subjectEn: subjects.value.en,
        contentEn: serializeBlocksToHtml(blocksByLanguage.value.en),
        enabled: props.template.enabled,
        remark: props.template.remark
      })
      persistDraft()
      ElMessage.success(t('pages.config.mailTemplate.editor.saveSuccess'))
      emit('saved')
    } finally {
      saving.value = false
    }
  }

  function openPreview() {
    previewVisible.value = true
  }

  // ── 对齐图标（内联 SVG 渲染函数） ──
  function defineAlignIcon(align: 'left' | 'center' | 'right') {
    const rows =
      align === 'left'
        ? ['M3 5h12', 'M3 9h8', 'M3 13h12', 'M3 17h8']
        : align === 'center'
          ? ['M3 5h14', 'M5 9h10', 'M3 13h14', 'M5 17h10']
          : ['M5 5h12', 'M9 9h8', 'M5 13h12', 'M9 17h8']
    return defineComponent({
      name: `Align${align}`,
      render() {
        return h(
          'svg',
          {
            viewBox: '0 0 20 22',
            width: '1em',
            height: '1em',
            fill: 'none',
            stroke: 'currentColor',
            'stroke-width': 1.6,
            'stroke-linecap': 'round'
          },
          rows.map((d) => h('path', { d }))
        )
      }
    })
  }
</script>

<style scoped lang="scss">
  $border: var(--el-border-color-lighter);

  .mail-editor {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    overflow: hidden;
    background: var(--el-bg-color);
    border-radius: var(--el-border-radius-base);

    // ══ 工具栏 ══
    &__toolbar {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
      gap: 12px;
      flex-shrink: 0;
      align-items: center;
      height: 52px;
      padding: 0 14px;
      background: var(--el-bg-color);
      border-bottom: 1px solid $border;
    }

    &__toolbar-group {
      display: flex;
      align-items: center;
      min-width: 0;
      gap: 10px;
    }

    &__toolbar-left {
      justify-content: flex-start;
    }

    &__toolbar-center {
      justify-content: center;
    }

    &__toolbar-right {
      justify-content: flex-end;
    }

    &__back {
      font-weight: 500;
    }

    &__divider-v {
      width: 1px;
      height: 20px;
      background: $border;
    }

    &__title {
      min-width: 0;
      overflow: hidden;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    // 桌面/手机切换段
    &__segment {
      display: flex;
      padding: 3px;
      background: var(--el-fill-color);
      border-radius: 8px;
    }

    &__segment-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 5px 12px;
      font-size: 13px;
      color: var(--el-text-color-regular);
      cursor: pointer;
      background: transparent;
      border: none;
      border-radius: 6px;
      transition:
        color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        box-shadow 0.2s cubic-bezier(0.22, 1, 0.36, 1);

      &.is-active {
        color: var(--el-color-primary);
        background: var(--el-bg-color);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      }
    }

    &__lang-toggle {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      height: 32px;
      padding: 0 10px;
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-regular);
      text-transform: uppercase;
      cursor: pointer;
      background: var(--el-bg-color);
      border: 1px solid $border;
      border-radius: 6px;
      transition:
        color 0.24s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.24s cubic-bezier(0.22, 1, 0.36, 1),
        border-color 0.24s cubic-bezier(0.22, 1, 0.36, 1),
        box-shadow 0.24s cubic-bezier(0.22, 1, 0.36, 1),
        transform 0.24s cubic-bezier(0.22, 1, 0.36, 1);

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        border-color: var(--el-color-primary-light-5);
        box-shadow: 0 4px 14px rgba(64, 158, 255, 0.14);
        transform: translateY(-1px);
      }
    }

    // ══ 工作区三栏 ══
    &__workspace {
      display: flex;
      flex: 1;
      min-height: 0;
    }

    &__sidebar {
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      background: var(--el-bg-color);

      &--left {
        width: 248px;
        border-right: 1px solid $border;
      }

      &--right {
        width: 300px;
        border-left: 1px solid $border;
      }
    }

    &__left-tabs {
      display: flex;
      flex-shrink: 0;
      border-bottom: 1px solid $border;
    }

    &__left-tab {
      flex: 1;
      padding: 11px 0;
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-regular);
      cursor: pointer;
      background: transparent;
      border: none;
      border-bottom: 2px solid transparent;
      transition:
        color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        border-color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.2s cubic-bezier(0.22, 1, 0.36, 1);

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-fill-color-lighter);
      }

      &.is-active {
        color: var(--el-color-primary);
        background: transparent;
        border-bottom-color: var(--el-color-primary);
      }
    }

    &__left-panel {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-height: 0;
      overflow-y: auto;
    }

    &__pane {
      display: flex;
      flex-direction: column;
      border-bottom: 1px solid $border;

      &--grow {
        flex: 1;
        min-height: 0;
        border-bottom: none;
      }

      &--layers {
        flex: 1;
        min-height: 0;
        border-bottom: none;
      }
    }

    &__pane-head {
      padding: 10px 14px 8px;
      font-size: 12px;
      font-weight: 600;
      color: var(--el-text-color-secondary);
      letter-spacing: 0.4px;
      text-transform: uppercase;
    }

    // 组件调色板
    &__palette {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      padding: 0 12px 14px;
    }

    &__palette-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 12px 6px;
      cursor: grab;
      background: var(--el-bg-color);
      border: 1px solid $border;
      border-radius: 8px;
      will-change: transform, box-shadow;
      transition:
        color 0.26s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.26s cubic-bezier(0.22, 1, 0.36, 1),
        border-color 0.26s cubic-bezier(0.22, 1, 0.36, 1),
        box-shadow 0.26s cubic-bezier(0.22, 1, 0.36, 1),
        transform 0.26s cubic-bezier(0.22, 1, 0.36, 1);

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-fill-color-light);
        border-color: var(--el-color-primary-light-5);
        box-shadow: 0 8px 20px rgba(31, 41, 55, 0.08);
        transform: translateY(-2px);
      }

      &:focus-visible {
        outline: 2px solid var(--el-color-primary-light-5);
        outline-offset: 2px;
      }

      &:active {
        cursor: grabbing;
        transform: translateY(0) scale(0.99);
      }
    }

    &__palette-icon {
      display: flex;
      font-size: 18px;
    }

    &__palette-label {
      font-size: 12px;
      color: inherit;
    }

    // 图层列表
    &__layers {
      flex: 1;
      min-height: 0;
      padding: 0 8px 8px;
      overflow-y: auto;
    }

    &__layers-empty {
      padding: 20px 12px;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      text-align: center;
    }

    &__layer-list {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    &__layer {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 7px 8px;
      font-size: 13px;
      color: var(--el-text-color-regular);
      cursor: pointer;
      border-radius: 6px;
      transition:
        color 0.22s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.22s cubic-bezier(0.22, 1, 0.36, 1),
        transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);

      &:hover {
        background: var(--el-fill-color-light);
        transform: translateX(2px);

        .mail-editor__layer-del {
          opacity: 1;
        }
      }

      &.is-selected {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }
    }

    &__layer-drag {
      display: flex;
      color: var(--el-text-color-placeholder);
      cursor: grab;

      &:active {
        cursor: grabbing;
      }
    }

    &__layer-icon {
      display: flex;
      font-size: 14px;
    }

    &__layer-label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__layer-del {
      font-size: 13px;
      color: var(--el-text-color-placeholder);
      opacity: 0;
      transition: all 0.14s;

      &:hover {
        color: var(--el-color-danger);
      }
    }

    // ══ 画布 ══
    &__canvas {
      flex: 1;
      min-width: 0;
      padding: 24px;
      overflow-y: auto;
      background: var(--el-fill-color);
      background-image: radial-gradient(var(--el-border-color) 1px, transparent 0);
      background-size: 18px 18px;
    }

    &__paper {
      margin: 0 auto;
      overflow: hidden;
      background: #fff;
      border-radius: 10px;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
      transition: max-width 0.32s cubic-bezier(0.22, 1, 0.36, 1);

      &.is-desktop {
        max-width: 640px;
      }

      &.is-tablet {
        max-width: 520px;
      }

      &.is-mobile {
        max-width: 380px;
      }
    }

    &__mail-head {
      padding: 14px 20px;
      background: #f9fafb;
      border-bottom: 1px solid #eef0f2;
    }

    &__mail-subject {
      font-size: 15px;
      font-weight: 600;
      color: #111827;
    }

    &__mail-from {
      margin-top: 3px;
      font-size: 12px;
      color: #9ca3af;
    }

    &__mail-body {
      padding: 12px 20px 28px;
    }

    &__empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      min-height: 220px;
      color: #9ca3af;
      text-align: center;
    }

    &__empty-icon {
      font-size: 32px;
      color: #cbd5e1;
    }

    // 画布上的区块节点
    &__node {
      position: relative;
      padding: 2px;
      cursor: pointer;
      border: 1px dashed transparent;
      border-radius: 6px;
      transition: border-color 0.14s;

      &:hover {
        border-color: var(--el-color-primary-light-5);
      }

      &.is-selected {
        border-style: solid;
        border-color: var(--el-color-primary);
      }

      // 中和 @vue-email 组件外层 table 的默认间距
      :deep(table) {
        width: 100%;
      }
    }

    &__node-tools {
      position: absolute;
      top: -13px;
      right: 8px;
      z-index: 5;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &__node-badge {
      padding: 2px 8px;
      font-size: 11px;
      font-weight: 500;
      color: #fff;
      background: var(--el-color-primary);
      border-radius: 4px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
    }

    &__node-actions {
      display: flex;
      gap: 2px;
      padding: 3px 5px;
      background: var(--el-bg-color);
      border: 1px solid $border;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      .el-icon {
        padding: 3px;
        font-size: 14px;
        color: var(--el-text-color-regular);
        cursor: pointer;
        border-radius: 4px;
        transition: all 0.14s;

        &:hover {
          color: var(--el-color-primary);
          background: var(--el-fill-color);
        }

        &.is-disabled {
          color: var(--el-text-color-placeholder);
          pointer-events: none;
          opacity: 0.4;
        }
      }
    }

    &__node-danger:hover {
      color: var(--el-color-danger) !important;
      background: var(--el-color-danger-light-9) !important;
    }

    // ══ 右栏 tabs ══
    &__tabs {
      display: flex;
      flex-shrink: 0;
      border-bottom: 1px solid $border;
    }

    &__tab {
      flex: 1;
      padding: 11px 0;
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-regular);
      cursor: pointer;
      background: transparent;
      border: none;
      border-bottom: 2px solid transparent;
      transition:
        color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        border-color 0.2s cubic-bezier(0.22, 1, 0.36, 1),
        background-color 0.2s cubic-bezier(0.22, 1, 0.36, 1);

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-fill-color-lighter);
      }

      &.is-active {
        color: var(--el-color-primary);
        border-bottom-color: var(--el-color-primary);
      }
    }

    &__panel-scroll {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
    }

    &__placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 48px 24px;
      color: var(--el-text-color-placeholder);
      text-align: center;
    }

    &__placeholder-icon {
      font-size: 28px;
      color: var(--el-text-color-disabled);
    }

    // 属性组
    &__group {
      padding: 14px 14px 6px;
      border-bottom: 1px solid $border;
    }

    &__group-title {
      margin-bottom: 12px;
      font-size: 12px;
      font-weight: 600;
      color: var(--el-text-color-secondary);
      letter-spacing: 0.4px;
      text-transform: uppercase;
    }

    &__field {
      margin-bottom: 14px;

      > label {
        display: block;
        margin-bottom: 5px;
        font-size: 12px;
        color: var(--el-text-color-regular);
      }

      &--inline {
        display: flex;
        align-items: center;
        justify-content: space-between;

        > label {
          margin-bottom: 0;
        }
      }
    }

    &__field-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &__field-badge {
      padding: 1px 6px;
      font-family: 'JetBrains Mono', 'Fira Code', Menlo, monospace;
      font-size: 10px;
      font-weight: 600;
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      border-radius: 3px;
      text-transform: uppercase;
    }

    &__input-mono {
      :deep(textarea) {
        font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, monospace;
        font-size: 12px;
      }
    }

    &__var-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      margin-top: 7px;
    }

    &__chip {
      padding: 3px 7px;
      font-family: 'JetBrains Mono', 'Fira Code', Menlo, monospace;
      font-size: 11px;
      color: var(--el-color-primary);
      cursor: pointer;
      background: var(--el-color-primary-light-9);
      border: 1px solid var(--el-color-primary-light-7);
      border-radius: 4px;
      transition: all 0.14s;

      &:hover {
        background: var(--el-color-primary-light-8);
      }
    }

    // 对齐按钮组
    &__align {
      display: inline-flex;
      padding: 2px;
      background: var(--el-fill-color);
      border-radius: 6px;
    }

    &__align-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 26px;
      color: var(--el-text-color-regular);
      cursor: pointer;
      background: transparent;
      border: none;
      border-radius: 4px;
      transition: all 0.14s;

      &:hover {
        color: var(--el-color-primary);
      }

      &.is-active {
        color: var(--el-color-primary);
        background: var(--el-bg-color);
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }

    // 源码
    &__source-tip {
      padding: 12px 14px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background: var(--el-fill-color-lighter);
    }

    &__source {
      padding: 14px;
      margin: 0;
      overflow-x: auto;
      font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, monospace;
      font-size: 12px;
      line-height: 1.7;
      color: var(--el-text-color-regular);
      white-space: pre-wrap;
      word-break: break-all;
    }

    // 代码块（画布内，与序列化的 <pre> 一致）
    &__code {
      margin: 16px 0;
      padding: 16px;
      overflow-x: auto;
      font-family: SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 13px;
      line-height: 1.6;
      color: #e2e8f0;
      white-space: pre-wrap;
      word-break: break-word;
      background: #0f172a;
      border-radius: 8px;
    }

    // markdown 块（画布内）—— 邮件正文固定浅色观感
    &__markdown {
      margin: 16px 0;
      color: #374151;

      :deep(a) {
        color: #2563eb;
      }

      :deep(code) {
        font-family: SFMono-Regular, Menlo, Consolas, monospace;
      }
    }

    // 内容字段标记（md / code 徽标）
    &__field-badge {
      padding: 1px 6px;
      font-size: 10px;
      font-weight: 600;
      color: var(--el-color-info);
      text-transform: uppercase;
      letter-spacing: 0.4px;
      background: var(--el-fill-color);
      border-radius: 4px;
    }

    &__input-mono :deep(.el-textarea__inner) {
      font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, monospace;
      font-size: 12.5px;
      line-height: 1.6;
    }

    // ══ 响应式 ══
    @media (width <= 1100px) {
      &__sidebar--left {
        width: 200px;
      }

      &__sidebar--right {
        width: 260px;
      }
    }

    @media (width <= 860px) {
      height: auto;

      &__toolbar {
        grid-template-columns: 1fr;
        height: auto;
        min-height: 52px;
        padding: 8px;
      }

      &__toolbar-left,
      &__toolbar-center,
      &__toolbar-right {
        justify-content: center;
      }

      &__toolbar-left {
        justify-content: flex-start;
      }

      &__toolbar-right {
        .mail-editor__divider-v {
          display: none;
        }
      }

      &__workspace {
        flex-direction: column;
      }

      &__sidebar--left,
      &__sidebar--right {
        width: 100%;
        border: none;
        border-bottom: 1px solid $border;
      }

      &__palette {
        grid-template-columns: repeat(3, 1fr);
      }

      &__canvas {
        min-height: 360px;
      }
    }
  }

  /*
 * 暗色模式：仅画布"背景"随主题，邮件"纸张"始终保持浅色。
 * 原因：邮件正文颜色是固定值（如 #374151），收件方收到的也是浅底邮件，
 * 编辑器应忠实呈现真实观感，故纸张不跟随暗色（与 easy-email 一致）。
 */
</style>
