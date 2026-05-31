package com.velox.module.system.auth.service;

import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.CodeLoginCommand;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCodeSendCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.MfaChallengeSendCodeCommand;
import com.velox.module.system.auth.dto.MfaChallengeVerifyCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;

public interface LoginService {

    CaptchaDTO generateCaptcha();

    /** 登录页等匿名页面读取的访问控制配置（决定展示哪些入口）。 */
    AccessControlRespVO getAccessConfig();

    TokenDTO login(LoginCommand command);

    void register(RegisterCommand command);

    void sendResetPasswordCode(ForgotPasswordCodeCommand command);

    void resetPassword(ResetPasswordCommand command);

    void sendLoginCode(LoginCodeSendCommand command);

    TokenDTO loginByCode(CodeLoginCommand command);

    void logout();

    void sendMfaChallengeCode(MfaChallengeSendCodeCommand command);

    TokenDTO verifyMfaChallenge(MfaChallengeVerifyCommand command);
}
