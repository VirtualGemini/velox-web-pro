<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchUpdateUserProfile } from '@/api/auth'

  const props = defineProps<{
    detail: Partial<Api.Auth.UserDetail>
  }>()

  const emit = defineEmits<{
    (e: 'saved', patch: Partial<Api.Auth.UserDetail>): void
  }>()

  const { t } = useI18n()
  const isEdit = ref(false)
  const ruleFormRef = ref<FormInstance>()

  const form = reactive({
    realName: '',
    nickname: '',
    email: '',
    mobile: '',
    address: '',
    sex: 0,
    des: '',
    signature: '',
    position: '',
    company: '',
    tags: [] as string[]
  })

  const syncForm = () => {
    form.realName = props.detail.realName || ''
    form.nickname = props.detail.nickname || props.detail.userName || ''
    form.email = props.detail.email || ''
    form.mobile = props.detail.phone || ''
    form.address = props.detail.address || ''
    form.sex = props.detail.gender ?? 0
    form.des = props.detail.introduction || ''
    form.signature = props.detail.signature || ''
    form.position = props.detail.position || ''
    form.company = props.detail.company || ''
    form.tags = [...(props.detail.tags || [])]
  }

  watch(
    () => props.detail,
    () => {
      if (!isEdit.value) syncForm()
    },
    { immediate: true, deep: true }
  )

  const options = computed(() => [
    { value: 0, label: t('pages.system.userCenter.sex.unknown') },
    { value: 1, label: t('pages.system.userCenter.sex.male') },
    { value: 2, label: t('pages.system.userCenter.sex.female') },
    { value: 3, label: t('pages.system.userCenter.sex.other') }
  ])

  const rules = reactive<FormRules>({
    realName: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.realNameRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: t('pages.system.userCenter.basicSettings.validation.nameLength'),
        trigger: 'blur'
      }
    ],
    nickname: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.nicknameRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: t('pages.system.userCenter.basicSettings.validation.nameLength'),
        trigger: 'blur'
      }
    ],
    email: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.emailRequired'),
        trigger: 'blur'
      }
    ],
    mobile: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.mobileRequired'),
        trigger: 'blur'
      }
    ],
    address: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.addressRequired'),
        trigger: 'blur'
      }
    ],
    sex: [
      {
        required: true,
        message: t('pages.system.userCenter.basicSettings.validation.sexRequired'),
        trigger: 'change'
      }
    ],
    signature: [
      {
        max: 255,
        message: t('pages.system.userCenter.basicSettings.validation.signatureLength'),
        trigger: 'blur'
      }
    ],
    position: [
      {
        max: 50,
        message: t('pages.system.userCenter.basicSettings.validation.positionLength'),
        trigger: 'blur'
      }
    ],
    company: [
      {
        max: 100,
        message: t('pages.system.userCenter.basicSettings.validation.companyLength'),
        trigger: 'blur'
      }
    ]
  })

  const validateTags = (): boolean => {
    if (form.tags.length > 10) {
      ElMessage.error(t('pages.system.userCenter.basicSettings.validation.tagsLimit'))
      return false
    }
    if (form.tags.some((tag) => (tag || '').length > 16)) {
      ElMessage.error(t('pages.system.userCenter.basicSettings.validation.tagLength'))
      return false
    }
    return true
  }

  const save = async () => {
    if (!isEdit.value) {
      isEdit.value = true
      return
    }

    const valid = await ruleFormRef.value?.validate().catch(() => false)
    if (!valid) return
    if (!validateTags()) return

    const payload: Api.Auth.UserProfileUpdateCommand = {
      realName: form.realName.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim() || undefined,
      phone: form.mobile.trim(),
      address: form.address.trim() || undefined,
      gender: form.sex,
      introduction: form.des.trim() || undefined,
      signature: form.signature.trim() || undefined,
      position: form.position.trim() || undefined,
      company: form.company.trim() || undefined,
      tags: form.tags
    }

    await fetchUpdateUserProfile(payload)

    emit('saved', {
      email: payload.email || '',
      phone: payload.phone,
      nickname: payload.nickname,
      gender: payload.gender,
      realName: payload.realName,
      address: payload.address || '',
      introduction: payload.introduction || '',
      signature: payload.signature || '',
      position: payload.position || '',
      company: payload.company || '',
      tags: payload.tags
    })

    ElMessage.success(t('pages.system.userCenter.messages.profileSaveSuccess'))
    isEdit.value = false
  }
</script>

<template>
  <div class="velox-card-sm">
    <h1 class="p-4 text-xl font-normal border-b border-g-300">
      {{ t('pages.system.userCenter.basicSettings.title') }}
    </h1>

    <ElForm
      :model="form"
      class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
      ref="ruleFormRef"
      :rules="rules"
      label-width="86px"
      label-position="top"
    >
      <ElRow>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.realName')"
          prop="realName"
        >
          <ElInput v-model="form.realName" :disabled="!isEdit" />
        </ElFormItem>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.sex')"
          prop="sex"
          class="ml-5"
        >
          <ElSelect
            v-model="form.sex"
            :placeholder="t('pages.system.userCenter.basicSettings.placeholders.sex')"
            :disabled="!isEdit"
          >
            <ElOption
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
      </ElRow>

      <ElRow>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.nickname')"
          prop="nickname"
        >
          <ElInput v-model="form.nickname" :disabled="!isEdit" />
        </ElFormItem>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.email')"
          prop="email"
          class="ml-5"
        >
          <ElInput v-model="form.email" :disabled="!isEdit" />
        </ElFormItem>
      </ElRow>

      <ElRow>
        <ElFormItem :label="t('pages.system.userCenter.basicSettings.fields.mobile')" prop="mobile">
          <ElInput v-model="form.mobile" :disabled="!isEdit" />
        </ElFormItem>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.address')"
          prop="address"
          class="ml-5"
        >
          <ElInput v-model="form.address" :disabled="!isEdit" />
        </ElFormItem>
      </ElRow>

      <ElRow>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.position')"
          prop="position"
        >
          <ElInput
            v-model="form.position"
            :disabled="!isEdit"
            :placeholder="t('pages.system.userCenter.basicSettings.placeholders.position')"
          />
        </ElFormItem>
        <ElFormItem
          :label="t('pages.system.userCenter.basicSettings.fields.company')"
          prop="company"
          class="ml-5"
        >
          <ElInput
            v-model="form.company"
            :disabled="!isEdit"
            :placeholder="t('pages.system.userCenter.basicSettings.placeholders.company')"
          />
        </ElFormItem>
      </ElRow>

      <ElFormItem
        :label="t('pages.system.userCenter.basicSettings.fields.signature')"
        prop="signature"
      >
        <ElInput
          v-model="form.signature"
          :disabled="!isEdit"
          :placeholder="t('pages.system.userCenter.basicSettings.placeholders.signature')"
        />
      </ElFormItem>

      <ElFormItem :label="t('pages.system.userCenter.basicSettings.fields.tags')" prop="tags">
        <ElSelect
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          :disabled="!isEdit"
          :max-collapse-tags="3"
          :placeholder="t('pages.system.userCenter.basicSettings.placeholders.tags')"
          class="w-full"
        />
      </ElFormItem>

      <ElFormItem
        :label="t('pages.system.userCenter.basicSettings.fields.description')"
        prop="des"
        class="h-32"
      >
        <ElInput type="textarea" :rows="4" v-model="form.des" :disabled="!isEdit" />
      </ElFormItem>

      <div class="flex-c justify-end [&_.el-button]:!w-27.5">
        <ElButton type="primary" class="w-22.5" v-ripple @click="save">
          {{
            isEdit
              ? t('pages.system.userCenter.actions.save')
              : t('pages.system.userCenter.actions.edit')
          }}
        </ElButton>
      </div>
    </ElForm>
  </div>
</template>
