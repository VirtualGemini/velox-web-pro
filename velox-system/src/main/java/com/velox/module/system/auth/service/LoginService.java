package com.velox.module.system.auth.service;

import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;

public interface LoginService {

    CaptchaDTO generateCaptcha();

    TokenDTO login(LoginCommand command);

    void register(RegisterCommand command);

    void sendResetPasswordCode(ForgotPasswordCodeCommand command);

    void resetPassword(ResetPasswordCommand command);

    void logout();
}
