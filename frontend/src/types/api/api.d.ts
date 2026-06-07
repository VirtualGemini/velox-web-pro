/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Velox Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
      /** 滑块完成后由后端签发的一次性 captcha 票据 */
      captchaTicket?: string
    }

    /** captcha 票据响应 */
    interface CaptchaTicketResponse {
      captchaTicket: string
      ttl: number
    }

    /** 验证码登录的渠道类型 */
    type LoginCodeChannel = 'email'

    /** 发送登录验证码参数 */
    interface LoginCodeSendParams {
      type: LoginCodeChannel
      target: string
    }

    /** 验证码登录参数 */
    interface CodeLoginParams {
      type: LoginCodeChannel
      target: string
      code: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      mfaChallenge?: string
      mfaType?: 'email' | 'totp'
      mfaEmailMasked?: string
      mfaTotpDigits?: number
      pendingDeletion?: boolean
      accountId?: string
      userName?: string
      avatar?: string
      email?: string
      deletionRequestedAt?: string
      deletionExpiresAt?: string
    }

    interface MfaChallengeSendParams {
      challenge: string
    }

    interface MfaChallengeVerifyParams {
      challenge: string
      code: string
    }

    /** 注册参数 */
    interface RegisterParams {
      username: string
      password: string
      confirmPassword: string
    }

    interface ForgotPasswordCodeParams {
      email: string
    }

    interface ResetPasswordParams {
      email: string
      code: string
      newPassword: string
      confirmPassword: string
    }

    /** 登录页访问控制配置（匿名读取） */
    interface AccessConfig {
      generalRegisterEnabled: boolean
      forgotPasswordEnabled: boolean
      loginMethods: string[]
      thirdPartyLoginChannels: string[]
      thirdPartyRegisterChannels: string[]
    }

    /** 用户基础信息（仅保留支撑系统运行所需字段） */
    interface AccountInfo {
      buttons: string[]
      roles: string[]
      accountId: string
      userName: string
      email: string
      phone?: string
      avatar?: string
      language?: string
    }

    /** 用户详细信息（账号中心专用） */
    interface AccountDetail {
      buttons: string[]
      roles: string[]
      tags?: string[]
      accountId: string
      userName: string
      email: string
      securityEmail?: string
      phone?: string
      avatar?: string
      nickname?: string
      gender?: number
      realName?: string
      address?: string
      introduction?: string
      signature?: string
      position?: string
      company?: string
      language?: string
    }

    interface AccountProfileUpdateCommand {
      realName?: string
      nickname?: string
      email?: string
      phone?: string
      address?: string
      gender?: number
      introduction?: string
      signature?: string
      position?: string
      company?: string
      tags?: string[]
    }

    interface AccountPasswordUpdateCommand {
      currentPassword: string
      newPassword: string
      confirmPassword: string
      mfaType?: 'email' | 'totp'
      mfaEmailCode?: string
      mfaTotpCode?: string
    }

    interface AvatarUpdateCommand {
      avatarUrl: string
    }

    interface AccountTabInfo {
      accountId: string
      username: string
      remark?: string
      securityEmail?: string
      emailMfaEnabled: boolean
      totpMfaEnabled: boolean
      pendingDeletion: boolean
      deletionRequestedAt?: string
      deletionExpiresAt?: string
    }

    interface AccountUsernameUpdateCommand {
      username: string
    }

    interface AccountDeletionCommand {
      username: string
      emailCode?: string
      currentPassword?: string
    }

    interface AccountRecoveryCommand {
      username: string
    }

    type UserInfo = AccountInfo
    type UserDetail = AccountDetail
    type UserProfileUpdateCommand = AccountProfileUpdateCommand
    type UserPasswordUpdateCommand = AccountPasswordUpdateCommand
  }

  /** 系统管理类型 */
  namespace SystemManage {
    interface RoleOption {
      roleId: string
      roleCode: string
      roleName: string
      roleLevel?: number
    }

    /** 账号列表 */
    type AccountList = Api.Common.PaginatedResponse<AccountListItem>

    /** 账号列表项 */
    interface AccountListItem {
      accountId: string
      avatar: string
      status: string
      activeStatus: string
      username: string
      email: string
      remark: string
      createTime: string
      updateTime: string
    }

    interface AccountDetailCard {
      header: {
        avatar: string
        username: string
        nickname?: string
        realName?: string
        status: string
        activeStatus: string
        remark?: string
        roleCodes: string[]
        createTime?: string
        updateTime?: string
      }
      profile: {
        nickname?: string
        gender?: number
        realName?: string
        displayEmail?: string
        displayMobile?: string
        address?: string
        position?: string
        company?: string
        signature?: string
        introduction?: string
        tags: string[]
      }
      account: {
        accountId: string
        username: string
        remark?: string
        status: string
        activeStatus: string
        pendingDeletion: boolean
        deletionRequestedAt?: string
        deletionExpiresAt?: string
        loginFailCount?: number
        loginFailTime?: string
        roleCodes: string[]
      }
      security: {
        securityEmail?: string
        emailMfaEnabled: boolean
        totpMfaEnabled: boolean
        loginMethods: string[]
        disabledLoginMethods: string[]
        allowedLoginMethods: string[]
        emailVerifiedAt?: string
        lastPasswordChangeAt?: string
      }
      thirdPartyProviders: Array<{
        key: string
        name: string
        bound: boolean
        disabled: boolean
      }>
    }

    /** 账号搜索参数 */
    type AccountSearchParams = Partial<
      Pick<AccountListItem, 'username' | 'email' | 'remark' | 'status' | 'activeStatus'> &
        Api.Common.CommonSearchParams & {
          createTimeStart: string | null
          createTimeEnd: string | null
          updateTimeStart: string | null
          updateTimeEnd: string | null
        }
    >

    interface AccountSaveCommand {
      username: string
      password?: string
      remark?: string
      roleCodes: string[]
      /** 账号状态(1-启用 2-禁用 3-异常 4-注销)，仅编辑生效 */
      accountStatus?: number
    }

    /** 管理员编辑账号资料命令（含头像，全部可选） */
    interface AdminProfileUpdateCommand {
      realName?: string
      nickname?: string
      email?: string
      phone?: string
      address?: string
      gender?: number
      introduction?: string
      signature?: string
      position?: string
      company?: string
      avatar?: string
      tags?: string[]
    }

    /** 管理员重置密码命令 */
    interface AdminPasswordResetCommand {
      newPassword: string
    }

    /** 管理员设置/清除安全邮箱命令（email 为空表示清除） */
    interface AdminSecurityEmailCommand {
      email?: string
    }

    /** 管理员开启/关闭邮箱二次验证命令 */
    interface AdminMfaEmailCommand {
      enabled: boolean
    }

    /** 管理员设置登录方式命令 */
    interface AdminLoginMethodsCommand {
      enabledMethods: string[]
      disabledMethods: string[]
    }

    /** 管理员开启/禁用第三方渠道命令 */
    interface AdminOauthChannelCommand {
      disabled: boolean
    }

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: string
      roleName: string
      roleCode: string
      description: string
      type: number
      typeName: string
      enabled: boolean
      createTime: string
      updateTime: string
    }

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleCode' | 'description' | 'enabled'> &
        Api.Common.CommonSearchParams & {
          type: number
          createTimeStart: string | null
          createTimeEnd: string | null
          updateTimeStart: string | null
          updateTimeEnd: string | null
          startTime: string | null
          endTime: string | null
        }
    >

    interface RoleSaveCommand {
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
    }

    interface MenuSaveCommand {
      parentId?: string
      menuType: 'menu' | 'button'
      name: string
      title: string
      path?: string
      component?: string
      redirect?: string
      icon?: string
      authMark: string
      isEnable: boolean
      sort: number
      keepAlive?: boolean
      isHide?: boolean
      isHideTab?: boolean
      link?: string
      isIframe?: boolean
      showBadge?: boolean
      showTextBadge?: string
      fixedTab?: boolean
      activePath?: string
      isFullPage?: boolean
    }

    interface RoleMenuPermissionUpdateCommand {
      menuIds: string[]
    }
  }

  namespace User {
    namespace Security {
      interface MfaStatus {
        email: boolean
        totp: boolean
      }

      interface SecurityStatus {
        email: string
        emailMasked: string
        loginMethods: string[]
        effectiveLoginMethods: string[]
        allowedLoginMethods: string[]
        adminDisabledLoginMethods: string[]
        passwordRequired: boolean
        mfa: MfaStatus
        emailVerifiedAt?: string
        lastPasswordChangeAt?: string
      }

      interface LoginMethodsUpdateCommand {
        methods: string[]
      }

      type EmailRebindProofType = 'current_email_code' | 'totp' | 'password'

      interface EmailRebindProofVerifyCommand {
        proofType: EmailRebindProofType
        currentEmailCode?: string
        totpCode?: string
        currentPassword?: string
      }

      interface EmailRebindProof {
        proofTicket: string
        expiresInSeconds: number
      }

      interface EmailRebindSendCodeCommand {
        newEmail: string
        proofTicket: string
      }

      interface EmailRebindCommand {
        newEmail: string
        newEmailCode: string
        proofTicket: string
      }

      interface EmailUnbindCommand {
        currentEmailCode: string
        totpCode?: string
      }

      interface MfaEmailUpdateCommand {
        enabled: boolean
        code?: string
      }

      interface MfaTotpProvision {
        secret: string
        otpAuthUri: string
        issuer: string
        accountName: string
        digits: number
        periodSeconds: number
        algorithm: string
      }

      interface MfaTotpEnableCommand {
        secret: string
        code: string
      }

      interface MfaTotpEnableResult {
        recoveryCodes: string[]
      }

      interface MfaTotpDisableCommand {
        code?: string
        recoveryCode?: string
      }
    }
  }
}
