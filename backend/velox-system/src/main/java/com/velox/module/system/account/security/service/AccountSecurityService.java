package com.velox.module.system.account.security.service;

import com.velox.module.system.account.security.dto.EmailRebindCommand;
import com.velox.module.system.account.security.dto.EmailRebindProofDTO;
import com.velox.module.system.account.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.account.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.account.security.dto.EmailUnbindCommand;
import com.velox.module.system.account.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.account.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.account.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.account.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.account.security.dto.MfaTotpEnableResultDTO;
import com.velox.module.system.account.security.dto.MfaTotpProvisionDTO;
import com.velox.module.system.account.security.dto.SecurityStatusDTO;

public interface AccountSecurityService {

    SecurityStatusDTO getStatus();

    void sendEmailUnbindCode();

    void sendEmailRebindProofCode();

    EmailRebindProofDTO verifyEmailRebindProof(EmailRebindProofVerifyCommand command);

    void sendEmailRebindCode(EmailRebindSendCodeCommand command);

    Boolean rebindEmail(EmailRebindCommand command);

    Boolean unbindEmail(EmailUnbindCommand command);

    Boolean updateLoginMethods(LoginMethodsUpdateCommand command);

    void sendMfaEmailCode();

    Boolean updateMfaEmail(MfaEmailUpdateCommand command);

    MfaTotpProvisionDTO provisionMfaTotp();

    MfaTotpEnableResultDTO enableMfaTotp(MfaTotpEnableCommand command);

    Boolean disableMfaTotp(MfaTotpDisableCommand command);
}
