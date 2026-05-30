<template>
  <ElDrawer
    v-model="drawerVisible"
    :title="t('pages.system.account.editDrawer.title')"
    :size="drawerSize"
    append-to-body
    modal-class="account-drawer-modal"
    class="account-edit-drawer"
    @opened="tabTransitionReady = true"
  >
    <template v-if="localDetail">
      <div class="account-edit-page">
        <section class="edit-hero">
          <div class="hero-identity">
            <ElUpload
              :http-request="handleAvatarUpload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
              class="hero-avatar-upload"
            >
              <div class="hero-avatar-trigger">
                <ElImage :src="heroAvatar" fit="cover" class="hero-avatar" />
                <div class="hero-avatar-overlay">
                  <VeloxSvgIcon icon="ri:edit-line" class="hero-avatar-icon" />
                </div>
              </div>
            </ElUpload>
            <div class="hero-copy">
              <div class="hero-title-row">
                <h1 class="hero-title">{{ localDetail.header.username }}</h1>
                <ElTag :type="accountStatusTag.type">{{ t(accountStatusTag.textKey) }}</ElTag>
              </div>
              <p class="hero-subtitle">
                {{ localDetail.header.nickname || localDetail.header.realName || '-' }}
              </p>
            </div>
          </div>
        </section>

        <div
          ref="tabsShellRef"
          class="tab-scroll-shell"
          :class="{ 'is-scrollable': tabsScrollable }"
          @click.capture="tabsDrag.preventClickAfterDrag"
          @pointercancel="tabsDrag.endDrag"
          @pointerdown="startTabsDrag"
          @pointermove="tabsDrag.moveDrag"
          @pointerup="tabsDrag.endDrag"
        >
          <ElButton
            v-if="tabsScrollable"
            :icon="ArrowLeft"
            :disabled="!canScrollTabsLeft"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--left"
            aria-label="向左滚动标签"
            @click="scrollTabs('left')"
          />
          <ElTabs
            v-model="activeTab"
            class="edit-tabs tab-scroll-tabs"
            :class="{ 'is-dragging': tabsDrag.isDragging.value, 'tabs-no-anim': !tabTransitionReady }"
          >
            <!-- 个人资料 -->
            <ElTabPane :label="t('pages.system.account.editDrawer.tabs.profile')" name="profile">
              <ElForm
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-position="top"
                class="edit-form"
              >
                <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.nickname')" prop="nickname">
                  <ElInput
                    v-model="profileForm.nickname"
                    :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.nickname')"
                  />
                </ElFormItem>
                <div class="form-grid">
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.realName')" prop="realName">
                    <ElInput
                      v-model="profileForm.realName"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.realName')"
                    />
                  </ElFormItem>
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.sex')" prop="gender">
                    <ElSelect
                      v-model="profileForm.gender"
                      class="w-full"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.sex')"
                    >
                      <ElOption
                        v-for="opt in genderOptions"
                        :key="opt.value"
                        :label="opt.label"
                        :value="opt.value"
                      />
                    </ElSelect>
                  </ElFormItem>
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.email')" prop="email">
                    <ElInput
                      v-model="profileForm.email"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.email')"
                    />
                  </ElFormItem>
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.mobile')" prop="mobile">
                    <ElInput
                      v-model="profileForm.mobile"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.mobile')"
                    />
                  </ElFormItem>
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.position')" prop="position">
                    <ElInput
                      v-model="profileForm.position"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.position')"
                    />
                  </ElFormItem>
                  <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.company')" prop="company">
                    <ElInput
                      v-model="profileForm.company"
                      :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.company')"
                    />
                  </ElFormItem>
                </div>
                <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.address')" prop="address">
                  <ElInput
                    v-model="profileForm.address"
                    :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.address')"
                  />
                </ElFormItem>
                <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.signature')" prop="signature">
                  <ElInput
                    v-model="profileForm.signature"
                    :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.signature')"
                  />
                </ElFormItem>
                <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.description')" prop="introduction">
                  <ElInput
                    v-model="profileForm.introduction"
                    type="textarea"
                    :rows="4"
                    :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.description')"
                  />
                </ElFormItem>
                <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.tags')" prop="tags">
                  <ElSelect
                    v-model="profileForm.tags"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    :max-collapse-tags="3"
                    class="w-full"
                    :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.tags')"
                  />
                </ElFormItem>
                <div class="form-actions">
                  <ElButton type="primary" @click="saveProfile">
                    {{ t('pages.system.account.editDrawer.save') }}
                  </ElButton>
                </div>
              </ElForm>
            </ElTabPane>

            <!-- 账号设置 -->
            <ElTabPane :label="t('pages.system.account.editDrawer.tabs.account')" name="account">
              <ElForm
                ref="accountFormRef"
                :model="accountForm"
                :rules="accountRules"
                label-position="top"
                class="edit-form"
              >
                <ElFormItem :label="t('pages.system.account.dialog.fields.username')" prop="username">
                  <ElInput v-model="accountForm.username" />
                </ElFormItem>
                <ElFormItem :label="t('pages.system.account.dialog.fields.roleCodes')" prop="roleCodes">
                  <ElSelect v-model="accountForm.roleCodes" multiple filterable class="w-full">
                    <ElOption
                      v-for="role in roleList"
                      :key="role.roleCode"
                      :value="role.roleCode"
                      :label="role.roleName"
                    />
                  </ElSelect>
                </ElFormItem>
                <ElFormItem :label="t('pages.system.account.editDrawer.fields.accountStatus')" prop="accountStatus">
                  <ElSelect v-model="accountForm.accountStatus" class="w-full">
                    <ElOption
                      v-for="opt in statusOptions"
                      :key="opt.value"
                      :label="t(opt.labelKey)"
                      :value="opt.value"
                    />
                  </ElSelect>
                </ElFormItem>
                <div class="form-actions">
                  <ElButton type="primary" @click="saveAccountSettings">
                    {{ t('pages.system.account.editDrawer.save') }}
                  </ElButton>
                </div>
              </ElForm>
            </ElTabPane>

            <!-- 账号安全 -->
            <ElTabPane :label="t('pages.system.account.editDrawer.tabs.security')" name="security">
              <div class="setting-list">
                <!-- 密码 -->
                <div class="setting-section setting-section--column">
                  <div class="setting-head">
                    <h3 class="setting-title">{{ t('pages.system.account.editDrawer.security.password.title') }}</h3>
                    <p class="setting-desc">{{ t('pages.system.account.editDrawer.security.password.desc') }}</p>
                  </div>
                  <!-- 用原生 form 包裹密码输入，消除浏览器 "Password field is not contained in a form" 警告；
                       @submit.prevent 拦截回车默认提交，改为触发重置密码 -->
                  <form class="setting-row" @submit.prevent="resetPassword">
                    <!-- 隐藏的 username 字段：供浏览器/密码管理器关联账号上下文，
                         消除 "Password forms should have (optionally hidden) username fields" 无障碍警告 -->
                    <input
                      :value="localDetail.header.username"
                      type="text"
                      name="username"
                      autocomplete="username"
                      aria-hidden="true"
                      tabindex="-1"
                      class="visually-hidden-username"
                    />
                    <ElInput
                      v-model="passwordValue"
                      type="password"
                      show-password
                      autocomplete="new-password"
                      class="setting-block"
                      :placeholder="t('pages.system.account.editDrawer.security.password.placeholder')"
                    />
                    <ElButton
                      type="primary"
                      native-type="submit"
                      class="setting-row-btn"
                    >
                      {{ t('pages.system.account.editDrawer.security.password.action') }}
                    </ElButton>
                  </form>
                </div>

                <!-- 安全邮箱 -->
                <div class="setting-section setting-section--column">
                  <div class="setting-head">
                    <h3 class="setting-title">{{ t('pages.system.account.editDrawer.security.email.title') }}</h3>
                    <p class="setting-desc">{{ t('pages.system.account.editDrawer.security.email.desc') }}</p>
                  </div>
                  <div class="setting-row">
                    <ElInput
                      v-model="emailValue"
                      class="setting-block"
                      :placeholder="t('pages.system.account.editDrawer.security.email.placeholder')"
                    />
                    <div class="setting-buttons setting-buttons--compact">
                      <ElButton
                        type="primary"
                        class="setting-email-btn"
                        @click="saveSecurityEmail"
                      >
                        {{ t('pages.system.account.editDrawer.security.email.save') }}
                      </ElButton>
                      <ElButton
                        type="danger"
                        plain
                        class="velox-batch-delete setting-email-btn"
                        :disabled="!localDetail.security.securityEmail && pending !== 'emailClear'"
                        @click="clearSecurityEmail"
                      >
                        {{ t('pages.system.account.editDrawer.security.email.clear') }}
                      </ElButton>
                    </div>
                  </div>
                </div>

                <!-- 邮箱二次验证 -->
                <div class="setting-section">
                  <div class="setting-head">
                    <h3 class="setting-title">{{ t('pages.system.account.editDrawer.security.emailMfa.title') }}</h3>
                    <p class="setting-desc">{{ t('pages.system.account.editDrawer.security.emailMfa.desc') }}</p>
                  </div>
                  <div class="setting-action">
                    <ElSwitch
                      :model-value="localDetail.security.emailMfaEnabled"
                      :disabled="emailMfaSwitchDisabled"
                      @change="onEmailMfaChange"
                    />
                  </div>
                </div>

                <!-- TOTP 二次验证（仅可关闭） -->
                <div class="setting-section">
                  <div class="setting-head">
                    <h3 class="setting-title">{{ t('pages.system.account.editDrawer.security.totp.title') }}</h3>
                    <p class="setting-desc">{{ t('pages.system.account.editDrawer.security.totp.desc') }}</p>
                  </div>
                  <div class="setting-action">
                    <ElSwitch
                      :model-value="localDetail.security.totpMfaEnabled"
                      :disabled="!localDetail.security.totpMfaEnabled"
                      @change="onTotpChange"
                    />
                  </div>
                </div>

                <!-- 登录方式 -->
                <div class="setting-section setting-section--column">
                  <div class="setting-head">
                    <h3 class="setting-title">{{ t('pages.system.account.editDrawer.security.loginMethods.title') }}</h3>
                    <p class="setting-desc">{{ t('pages.system.account.editDrawer.security.loginMethods.desc') }}</p>
                  </div>
                  <div class="login-method-list">
                    <div v-for="method in loginMethodKeys" :key="method" class="login-method-row">
                      <span class="login-method-name">{{ resolveLoginMethod(method) }}</span>
                      <ElSwitch
                        :model-value="loginMethodState[method]"
                        @change="(val) => onLoginMethodChange(method, val)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </ElTabPane>

            <!-- 第三方登录 -->
            <ElTabPane :label="t('pages.system.account.editDrawer.tabs.thirdParty')" name="thirdParty">
              <p class="tab-hint">{{ t('pages.system.account.editDrawer.thirdParty.desc') }}</p>
              <div class="provider-list">
                <div
                  v-for="provider in thirdPartyProviderRows"
                  :key="provider.key"
                  class="provider-row"
                >
                  <div class="provider-main">
                    <img
                      v-if="provider.iconUrl"
                      :src="provider.iconUrl"
                      :alt="provider.displayName"
                      class="provider-icon"
                    />
                    <VeloxSvgIcon v-else :icon="provider.icon" class="provider-svg-icon" />
                    <span class="provider-name">{{ provider.displayName }}</span>
                  </div>
                  <div class="provider-action">
                    <ElTag
                      size="small"
                      class="provider-status-tag"
                      :type="provider.bound ? 'success' : 'info'"
                    >
                      {{
                        provider.bound
                          ? t('pages.system.account.detailCard.thirdParty.bound')
                          : t('pages.system.account.detailCard.thirdParty.unbound')
                      }}
                    </ElTag>
                    <span class="provider-switch-label">
                      {{ t('pages.system.account.editDrawer.thirdParty.channelEnabled') }}
                    </span>
                    <ElSwitch
                      :model-value="!provider.disabled"
                      @change="(val) => onChannelChange(provider.key, val)"
                    />
                    <ElButton
                      link
                      type="danger"
                      @click="unbindChannel(provider.key)"
                    >
                      {{ t('pages.system.account.editDrawer.thirdParty.unbind') }}
                    </ElButton>
                  </div>
                </div>
              </div>
            </ElTabPane>

            <!-- 其它 -->
            <ElTabPane :label="t('pages.system.account.editDrawer.tabs.other')" name="other">
              <ElForm label-position="top" class="edit-form">
                <ElFormItem :label="t('pages.system.account.dialog.fields.remark')">
                  <ElInput
                    v-model="accountForm.remark"
                    type="textarea"
                    :rows="4"
                    :maxlength="255"
                    show-word-limit
                    :placeholder="t('pages.system.account.dialog.placeholders.remark')"
                  />
                </ElFormItem>
                <div class="form-actions">
                  <ElButton type="primary" @click="saveAccountSettings">
                    {{ t('pages.system.account.editDrawer.save') }}
                  </ElButton>
                </div>
              </ElForm>
            </ElTabPane>
          </ElTabs>
          <ElButton
            v-if="tabsScrollable"
            :icon="ArrowRight"
            :disabled="!canScrollTabsRight"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--right"
            aria-label="向右滚动标签"
            @click="scrollTabs('right')"
          />
        </div>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import { useWindowSize } from '@vueuse/core'
  import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useHorizontalDragScroll } from '@/hooks'
  import linuxdoIcon from '@/assets/images/svg/linuxdo.svg'
  import { fetchFileUpload } from '@/api/file'
  import {
    fetchAdminDisableTotp,
    fetchAdminResetPassword,
    fetchAdminSetSecurityEmail,
    fetchAdminToggleOauthChannel,
    fetchAdminUnbindOauth,
    fetchAdminUpdateLoginMethods,
    fetchAdminUpdateMfaEmail,
    fetchAdminUpdateProfile,
    fetchGetAccountDetailCard,
    fetchGetRoleList,
    fetchUpdateAccount
  } from '@/api/system-manage'

  type ThirdPartyProvider = Api.SystemManage.AccountDetailCard['thirdPartyProviders'][number]

  const props = defineProps<{
    visible: boolean
    detail?: Api.SystemManage.AccountDetailCard
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'saved'): void
  }>()

  const { t } = useI18n()
  const { width } = useWindowSize()
  const activeTab = ref('profile')
  // tab 切换动画开关：抽屉滑入期间关闭（避免上次选中的 tab 颜色渐变重放造成"闪一下"），
  // 抽屉完全打开后(@opened)再开启，保证正常点击切换仍有过渡。
  const tabTransitionReady = ref(false)
  // 当前进行中的操作 key（''=空闲），用于让被点击的那个按钮单独显示 loading，
  // 避免所有按钮共享一个 loading 而同时进入“被点击态”、并触发整页遮罩抖动。
  const pending = ref('')
  // 抽屉内任意保存成功后置为 true；仅在抽屉关闭时通知列表刷新一次，
  // 避免每次保存都触发列表 loading 导致表格抖动。
  const dirty = ref(false)
  const localDetail = ref<Api.SystemManage.AccountDetailCard | undefined>(props.detail)
  const roleList = ref<Api.SystemManage.RoleOption[]>([])

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const drawerSize = computed(() => {
    if (width.value <= 640) return '100%'
    if (width.value <= 960) return '86%'
    return '760px'
  })

  const profileFormRef = ref<FormInstance>()
  const accountFormRef = ref<FormInstance>()

  const profileForm = reactive({
    nickname: '',
    realName: '',
    gender: 0,
    email: '',
    mobile: '',
    position: '',
    company: '',
    address: '',
    signature: '',
    introduction: '',
    tags: [] as string[],
    avatar: ''
  })

  const accountForm = reactive({
    username: '',
    roleCodes: [] as string[],
    accountStatus: 1,
    remark: ''
  })

  const passwordValue = ref('')
  const emailValue = ref('')
  const loginMethodState = reactive<Record<string, boolean>>({})

  // ===== Tab 横向拖拽滚动（与账号信息抽屉一致，移动端/桌面端同款 touch 事件） =====
  const tabsShellRef = ref<HTMLElement>()
  const tabsScrollable = ref(false)
  const canScrollTabsLeft = ref(false)
  const canScrollTabsRight = ref(false)
  const tabsDrag = useHorizontalDragScroll()

  const getTabsScroller = (shell?: HTMLElement) =>
    shell?.querySelector<HTMLElement>('.el-tabs__nav-scroll')

  const updateTabsScrollState = () => {
    const scroller = getTabsScroller(tabsShellRef.value)
    if (!scroller) {
      tabsScrollable.value = false
      canScrollTabsLeft.value = false
      canScrollTabsRight.value = false
      return
    }
    const maxScrollLeft = scroller.scrollWidth - scroller.clientWidth
    tabsScrollable.value = maxScrollLeft > 1
    canScrollTabsLeft.value = scroller.scrollLeft > 1
    canScrollTabsRight.value = scroller.scrollLeft < maxScrollLeft - 1
  }

  const scrollTabs = (direction: 'left' | 'right') => {
    const scroller = getTabsScroller(tabsShellRef.value)
    if (!scroller) return
    const distance = Math.max(scroller.clientWidth * 0.8, 96)
    scroller.scrollBy({
      left: direction === 'left' ? -distance : distance,
      behavior: 'smooth'
    })
    window.setTimeout(updateTabsScrollState, 260)
  }

  // 仅当指针落在标签条（nav-scroll）上才发起拖拽，避免影响 Tab 内表单控件的交互。
  const startTabsDrag = (event: PointerEvent) => {
    const target = event.target as HTMLElement | null
    if (!target || !target.closest('.el-tabs__nav-scroll')) return
    tabsDrag.startDrag(event, getTabsScroller(tabsShellRef.value), updateTabsScrollState)
  }

  const genderOptions = computed(() => [
    { value: 0, label: t('pages.system.accountCenter.sex.unknown') },
    { value: 1, label: t('pages.system.accountCenter.sex.male') },
    { value: 2, label: t('pages.system.accountCenter.sex.female') },
    { value: 3, label: t('pages.system.accountCenter.sex.other') }
  ])

  const statusOptions = [
    { value: 1, labelKey: 'pages.system.account.status.enabled' },
    { value: 2, labelKey: 'pages.system.account.status.disabled' },
    { value: 3, labelKey: 'pages.system.account.status.abnormal' },
    { value: 4, labelKey: 'pages.system.account.status.cancelled' }
  ]

  const profileRules = computed<FormRules>(() => ({
    realName: [{ min: 2, max: 50, message: t('pages.system.accountCenter.basicSettings.validation.nameLength'), trigger: 'blur' }],
    nickname: [{ min: 2, max: 50, message: t('pages.system.accountCenter.basicSettings.validation.nameLength'), trigger: 'blur' }],
    signature: [{ max: 255, message: t('pages.system.accountCenter.basicSettings.validation.signatureLength'), trigger: 'blur' }],
    position: [{ max: 50, message: t('pages.system.accountCenter.basicSettings.validation.positionLength'), trigger: 'blur' }],
    company: [{ max: 100, message: t('pages.system.accountCenter.basicSettings.validation.companyLength'), trigger: 'blur' }]
  }))

  const accountRules = computed<FormRules>(() => ({
    username: [
      { required: true, message: t('pages.system.account.dialog.validation.usernameRequired'), trigger: 'blur' },
      { min: 2, max: 50, message: t('pages.system.account.dialog.validation.usernameLength'), trigger: 'blur' }
    ],
    roleCodes: [{ required: true, message: t('pages.system.account.dialog.validation.roleRequired'), trigger: 'change' }]
  }))

  const heroAvatar = computed(() => profileForm.avatar || localDetail.value?.header.avatar || '')

  const accountStatusTag = computed(() => {
    const map: Record<string, { type: 'success' | 'info' | 'warning' | 'danger'; textKey: string }> = {
      '1': { type: 'success', textKey: 'pages.system.account.status.enabled' },
      '2': { type: 'info', textKey: 'pages.system.account.status.disabled' },
      '3': { type: 'warning', textKey: 'pages.system.account.status.abnormal' },
      '4': { type: 'danger', textKey: 'pages.system.account.status.cancelled' }
    }
    return map[localDetail.value?.header.status ?? ''] || { type: 'info' as const, textKey: 'common.status.unknown' }
  })

  const emailMfaSwitchDisabled = computed(() => {
    const security = localDetail.value?.security
    if (!security) return true
    // 未绑定安全邮箱、或已开启 TOTP（互斥）时不可开启邮箱二次验证
    return !security.emailMfaEnabled && (!security.securityEmail || security.totpMfaEnabled)
  })

  const loginMethodKeys = computed(() => localDetail.value?.security.allowedLoginMethods || [])

  const resolveLoginMethod = (method: string) => {
    const mapping: Record<string, string> = {
      password: 'pages.system.account.detailCard.loginMethods.password',
      email_code: 'pages.system.account.detailCard.loginMethods.emailCode'
    }
    return t(mapping[method] || 'common.status.unknown')
  }

  const resolveThirdPartyProvider = (provider: ThirdPartyProvider) => {
    const source = `${provider.name || ''} ${provider.key || ''}`.toLowerCase()
    if (source.includes('github')) return { displayName: 'GitHub', icon: 'ri:github-fill' }
    if (source.includes('linuxdo') || source.includes('linux.do')) {
      return { displayName: 'LinuxDO', iconUrl: linuxdoIcon }
    }
    return { displayName: provider.name || provider.key || '-', icon: 'ri:login-circle-line' }
  }

  const thirdPartyProviderRows = computed(() =>
    (localDetail.value?.thirdPartyProviders || []).map((provider) => ({
      ...provider,
      ...resolveThirdPartyProvider(provider)
    }))
  )

  const accountId = computed(() => localDetail.value?.account.accountId || '')

  const syncFromDetail = () => {
    const detail = localDetail.value
    if (!detail) return
    profileForm.nickname = detail.profile.nickname || ''
    profileForm.realName = detail.profile.realName || ''
    profileForm.gender = detail.profile.gender ?? 0
    profileForm.email = detail.profile.displayEmail || ''
    profileForm.mobile = detail.profile.displayMobile || ''
    profileForm.position = detail.profile.position || ''
    profileForm.company = detail.profile.company || ''
    profileForm.address = detail.profile.address || ''
    profileForm.signature = detail.profile.signature || ''
    profileForm.introduction = detail.profile.introduction || ''
    profileForm.tags = [...(detail.profile.tags || [])]
    profileForm.avatar = detail.header.avatar || ''

    accountForm.username = detail.account.username || ''
    accountForm.roleCodes = [...(detail.account.roleCodes || [])]
    accountForm.accountStatus = Number(detail.account.status) || 1
    accountForm.remark = detail.account.remark || ''

    const disabled = detail.security.disabledLoginMethods || []
    ;(detail.security.allowedLoginMethods || []).forEach((method) => {
      loginMethodState[method] = !disabled.includes(method)
    })

    passwordValue.value = ''
    emailValue.value = detail.security.securityEmail || ''
  }

  const loadRoleList = async () => {
    const response = await fetchGetRoleList({ current: 1, size: 1000 })
    roleList.value = response.records.map((item) => ({
      roleId: item.roleId,
      roleCode: item.roleCode,
      roleName: item.roleName
    }))
  }

  const reload = async () => {
    if (!accountId.value) return
    localDetail.value = await fetchGetAccountDetailCard(accountId.value)
    syncFromDetail()
    // 仅刷新抽屉自身视图；列表统一在抽屉关闭时刷新一次。
    dirty.value = true
  }

  watch(
    () => props.detail,
    (value) => {
      localDetail.value = value
      syncFromDetail()
    }
  )

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        activeTab.value = 'profile'
        dirty.value = false
        localDetail.value = props.detail
        syncFromDetail()
        loadRoleList()
        // 内容复用（未销毁）时清除上一次的校验态，避免重新打开仍显示红色报错。
        nextTick(() => {
          profileFormRef.value?.clearValidate()
          accountFormRef.value?.clearValidate()
        })
      } else {
        // 关闭时即把 tab 复位到第一个，并关闭切换动画；下次打开时滑入动画期间不会重放
        // 上一次选中 tab 的颜色渐变（@opened 后再开启动画）。
        activeTab.value = 'profile'
        tabTransitionReady.value = false
        if (dirty.value) {
          // 关闭抽屉时若期间保存过，统一通知列表刷新一次。
          dirty.value = false
          emit('saved')
        }
      }
    },
    { immediate: true }
  )

  watch(
    () => [props.visible, activeTab.value, width.value, Boolean(localDetail.value)],
    () => {
      nextTick(updateTabsScrollState)
    },
    { immediate: true }
  )

  const beforeAvatarUpload = (file: UploadRawFile) => {
    const isImage = file.type?.startsWith('image/')
    const isLt2M = (file.size || 0) / 1024 / 1024 < 2
    if (!isImage) {
      ElMessage.error(t('pages.system.accountCenter.messages.onlyImageAllowed'))
      return false
    }
    if (!isLt2M) {
      ElMessage.error(t('pages.system.accountCenter.messages.imageSizeLimit'))
      return false
    }
    return true
  }

  const handleAvatarUpload = async (options: { file: File }) => {
    try {
      const avatarUrl = await fetchFileUpload(options.file, 'avatar')
      profileForm.avatar = avatarUrl
      ElMessage.success(t('pages.system.accountCenter.messages.avatarUploadSuccess'))
    } catch {
      ElMessage.error(t('pages.system.accountCenter.messages.avatarUploadFailed'))
    }
  }

  const saveProfile = async () => {
    if (pending.value) return
    const valid = await profileFormRef.value?.validate().catch(() => false)
    if (!valid) return
    pending.value = 'profile'
    try {
      await fetchAdminUpdateProfile(accountId.value, {
        realName: profileForm.realName.trim() || undefined,
        nickname: profileForm.nickname.trim() || undefined,
        email: profileForm.email.trim() || undefined,
        phone: profileForm.mobile.trim() || undefined,
        address: profileForm.address.trim() || undefined,
        gender: profileForm.gender,
        introduction: profileForm.introduction.trim() || undefined,
        signature: profileForm.signature.trim() || undefined,
        position: profileForm.position.trim() || undefined,
        company: profileForm.company.trim() || undefined,
        avatar: profileForm.avatar || undefined,
        tags: profileForm.tags
      })
      ElMessage.success(t('pages.system.accountCenter.messages.profileSaveSuccess'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const saveAccountSettings = async () => {
    if (pending.value) return
    const valid = await accountFormRef.value?.validate().catch(() => false)
    // 备注页没有表单引用，accountFormRef 可能为空时直接放行（字段已在账号设置页校验）
    if (accountFormRef.value && !valid) return
    pending.value = 'account'
    try {
      await fetchUpdateAccount(accountId.value, {
        username: accountForm.username.trim(),
        roleCodes: [...accountForm.roleCodes],
        accountStatus: accountForm.accountStatus,
        remark: accountForm.remark.trim() || undefined
      })
      ElMessage.success(t('pages.system.account.editDrawer.messages.success'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const resetPassword = async () => {
    if (pending.value) return
    const value = passwordValue.value.trim()
    if (value.length < 6 || value.length > 32) {
      ElMessage.error(t('pages.system.account.editDrawer.messages.passwordLength'))
      return
    }
    pending.value = 'password'
    try {
      await fetchAdminResetPassword(accountId.value, { newPassword: value })
      passwordValue.value = ''
      ElMessage.success(t('pages.system.account.editDrawer.messages.passwordReset'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const saveSecurityEmail = async () => {
    if (pending.value) return
    pending.value = 'email'
    try {
      await fetchAdminSetSecurityEmail(accountId.value, { email: emailValue.value.trim() || undefined })
      ElMessage.success(t('pages.system.account.editDrawer.messages.emailUpdated'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const clearSecurityEmail = async () => {
    if (pending.value) return
    try {
      await ElMessageBox.confirm(
        t('pages.system.account.editDrawer.security.email.clearConfirm'),
        t('pages.system.account.editDrawer.security.email.clearConfirmTitle'),
        { type: 'warning' }
      )
    } catch {
      return
    }
    pending.value = 'emailClear'
    try {
      await fetchAdminSetSecurityEmail(accountId.value, { email: undefined })
      ElMessage.success(t('pages.system.account.editDrawer.messages.emailCleared'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const onEmailMfaChange = async (value: boolean | string | number) => {
    if (pending.value) return
    pending.value = 'emailMfa'
    try {
      await fetchAdminUpdateMfaEmail(accountId.value, { enabled: Boolean(value) })
      ElMessage.success(t('pages.system.account.editDrawer.messages.mfaUpdated'))
      await reload()
    } catch {
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const onTotpChange = async (value: boolean | string | number) => {
    if (value || pending.value) return
    try {
      await ElMessageBox.confirm(
        t('pages.system.account.editDrawer.security.totp.disableConfirm'),
        t('pages.system.account.editDrawer.security.totp.disableConfirmTitle'),
        { type: 'warning' }
      )
    } catch {
      return
    }
    pending.value = 'totp'
    try {
      await fetchAdminDisableTotp(accountId.value)
      ElMessage.success(t('pages.system.account.editDrawer.messages.totpDisabled'))
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const onLoginMethodChange = async (method: string, value: boolean | string | number) => {
    if (pending.value) return
    loginMethodState[method] = Boolean(value)
    const enabledMethods = loginMethodKeys.value.filter((m) => loginMethodState[m])
    const disabledMethods = loginMethodKeys.value.filter((m) => !loginMethodState[m])
    pending.value = 'loginMethods'
    try {
      await fetchAdminUpdateLoginMethods(accountId.value, { enabledMethods, disabledMethods })
      ElMessage.success(t('pages.system.account.editDrawer.messages.loginMethodsUpdated'))
      await reload()
    } catch {
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const onChannelChange = async (channel: string, value: boolean | string | number) => {
    if (pending.value) return
    pending.value = `channel:${channel}`
    try {
      await fetchAdminToggleOauthChannel(accountId.value, channel, { disabled: !value })
      ElMessage.success(t('pages.system.account.editDrawer.messages.channelUpdated'))
      await reload()
    } catch {
      await reload()
    } finally {
      pending.value = ''
    }
  }

  const unbindChannel = async (channel: string) => {
    if (pending.value) return
    try {
      await ElMessageBox.confirm(
        t('pages.system.account.editDrawer.thirdParty.unbindConfirm'),
        t('pages.system.account.editDrawer.thirdParty.unbindConfirmTitle'),
        { type: 'warning' }
      )
    } catch {
      return
    }
    pending.value = `unbind:${channel}`
    try {
      await fetchAdminUnbindOauth(accountId.value, channel)
      ElMessage.success(t('pages.system.account.editDrawer.messages.unbound'))
      await reload()
    } finally {
      pending.value = ''
    }
  }
</script>

<style scoped>
  .account-edit-drawer :deep(.el-drawer__body) {
    padding: 0;
  }

  .account-edit-page {
    width: min(100%, 760px);
    height: 100%;
    padding: 20px 24px 28px;
    margin: 0 auto;
    overflow-y: auto;
  }

  .edit-hero {
    padding-bottom: 8px;
  }

  .hero-identity {
    display: grid;
    grid-template-columns: 80px minmax(0, 1fr);
    gap: 16px;
    align-items: center;
  }

  .hero-avatar-upload :deep(.el-upload) {
    display: block;
  }

  .hero-avatar-trigger {
    position: relative;
    width: 80px;
    height: 80px;
    overflow: hidden;
    cursor: pointer;
    border-radius: 50%;
  }

  .hero-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
  }

  .hero-avatar-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgb(0 0 0 / 0%);
    opacity: 0;
    transition:
      background-color 0.18s ease,
      opacity 0.18s ease;
  }

  .hero-avatar-trigger:hover .hero-avatar-overlay {
    background: rgb(0 0 0 / 35%);
    opacity: 1;
  }

  .hero-avatar-icon {
    font-size: 22px;
    color: #fff;
  }

  .hero-copy {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
    min-height: 80px;
  }

  .hero-title-row {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .hero-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    line-height: 32px;
    color: var(--velox-text-color);
    overflow-wrap: anywhere;
  }

  .hero-subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    line-height: 22px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
    overflow-wrap: anywhere;
  }

  .edit-form {
    max-width: 640px;
  }

  .edit-form :deep(.el-select),
  .edit-form :deep(.el-input),
  .edit-form :deep(.el-textarea) {
    width: 100%;
  }

  .form-grid {
    display: grid;
    /* 响应式：每列至少 220px，容器更窄时自动降为单列，避免输入框被无限压缩 */
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 220px), 1fr));
    column-gap: 20px;
  }

  .form-actions {
    margin-top: 12px;
  }

  .w-full {
    width: 100%;
  }

  .setting-list {
    display: flex;
    flex-direction: column;
  }

  .setting-section {
    display: flex;
    gap: 16px;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .setting-section--column {
    flex-direction: column;
    align-items: stretch;
  }

  .setting-head {
    min-width: 0;
  }

  .setting-title {
    margin: 0;
    font-size: 15px;
    font-weight: 500;
    line-height: 22px;
    color: var(--velox-text-color);
  }

  .setting-desc {
    margin: 4px 0 0;
    font-size: 12px;
    line-height: 18px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .setting-action {
    flex: 0 0 auto;
  }

  .setting-row {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    width: 100%;
    margin-top: 12px;
  }

  /* 隐藏的 username 字段：用 0 尺寸而非 display:none / visibility:hidden，
     后两者会让浏览器忽略该字段、无法与密码框关联（autofill 上下文失效）。 */
  .visually-hidden-username {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
  }

  .setting-block {
    /* 输入框不再拉伸撑满：固定一个合适基准宽度，让保存/清除按钮紧贴其右侧并整体左对齐；
       窄屏时缩到最小宽度（新边界 220px）后按钮整体换行，而不是把输入框压扁。 */
    flex: 0 1 360px;
    min-width: min(220px, 100%);
  }

  .setting-row-btn {
    flex-shrink: 0;
  }

  .setting-buttons {
    display: flex;
    flex-shrink: 0;
    gap: 8px;
  }

  /* 安全邮箱两个按钮（保存 + 清除）更紧凑 */
  .setting-buttons--compact {
    gap: 4px;
  }

  /* 安全邮箱按钮固定最小宽度：loading 时左侧多出的 spinner 不再撑宽按钮、挤压周围元素。
     min-width 预留出 spinner 占位，文字保持居中，宽度在 loading 前后保持一致。 */
  .setting-email-btn {
    min-width: 96px;
  }

  .login-method-list {
    display: flex;
    flex-direction: column;
    width: 100%;
    margin-top: 12px;
  }

  .login-method-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 0;
  }

  .login-method-name {
    line-height: 22px;
    color: var(--velox-text-color);
  }

  .tab-hint {
    margin: 0 0 8px;
    font-size: 12px;
    line-height: 18px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .provider-list {
    display: flex;
    flex-direction: column;
  }

  .provider-row {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    row-gap: 10px;
    align-items: center;
    justify-content: space-between;
    padding: 14px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .provider-main {
    display: flex;
    flex: 1 1 auto;
    gap: 10px;
    align-items: center;
    min-width: 0;
  }

  .provider-icon,
  .provider-svg-icon {
    flex: 0 0 24px;
    width: 24px;
    height: 24px;
  }

  .provider-icon {
    object-fit: contain;
  }

  .provider-svg-icon {
    font-size: 24px;
    color: var(--velox-text-color);
  }

  .provider-meta {
    display: flex;
    gap: 8px;
    align-items: center;
    min-width: 0;
  }

  .provider-name {
    font-weight: 500;
    line-height: 22px;
    color: var(--velox-text-color);
    overflow-wrap: anywhere;
  }

  .provider-action {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    justify-content: flex-end;
  }

  /* 第三方绑定状态标签：归入右侧控件组并靠右对齐 */
  .provider-status-tag {
    flex: 0 0 auto;
  }

  .provider-switch-label {
    font-size: 12px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  /* ===== Tab 横向拖拽滚动（与账号信息抽屉一致） ===== */
  .edit-tabs {
    margin-top: 14px;
  }

  .tab-scroll-shell {
    position: relative;
  }

  .tab-scroll-shell.is-scrollable .tab-scroll-tabs :deep(.el-tabs__header) {
    padding-right: 28px;
    padding-left: 28px;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-wrap.is-scrollable) {
    padding: 0;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-prev),
  .tab-scroll-tabs :deep(.el-tabs__nav-next) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll) {
    overflow-x: auto;
    scrollbar-width: none;
    touch-action: pan-y;
    cursor: grab;
    user-select: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll::-webkit-scrollbar) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav) {
    flex-wrap: nowrap;
    transform: none !important;
  }

  .tab-scroll-tabs.is-dragging :deep(.el-tabs__nav-scroll) {
    cursor: grabbing;
  }

  .tab-scroll-nav {
    position: absolute;
    top: 0;
    z-index: 2;
    width: 24px;
    height: 32px;
    padding: 0;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .tab-scroll-nav--left {
    left: 0;
  }

  .tab-scroll-nav--right {
    right: 0;
  }

  .tab-scroll-nav.is-disabled {
    opacity: 0.35;
  }

  .edit-tabs :deep(.el-tabs__header) {
    margin-bottom: 2px;
  }

  .tabs-no-anim :deep(.el-tabs__item) {
    transition: none !important;
  }

  @media (width <= 640px) {
    .account-edit-page {
      width: 100%;
      padding: 16px 16px 24px;
    }

    .form-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
