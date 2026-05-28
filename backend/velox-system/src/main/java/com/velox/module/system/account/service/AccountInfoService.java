package com.velox.module.system.account.service;

import com.velox.module.system.domain.model.CurrentUserInfo;
import com.velox.module.system.account.dto.AccountDeletionCommand;
import com.velox.module.system.account.dto.AccountInfoBasicDTO;
import com.velox.module.system.account.dto.AccountInfoDTO;
import com.velox.module.system.account.dto.AccountPasswordUpdateCommand;
import com.velox.module.system.account.dto.AccountProfileUpdateCommand;
import com.velox.module.system.account.dto.AccountRecoveryCommand;
import com.velox.module.system.account.dto.AccountTabInfoDTO;
import com.velox.module.system.account.dto.AccountUsernameUpdateCommand;

public interface AccountInfoService {

    CurrentUserInfo getCurrentUserInfo();

    AccountInfoBasicDTO getAccountInfoBasicDTO();

    AccountInfoDTO getAccountInfoDTO();

    AccountTabInfoDTO getCurrentAccountTabInfo();

    Boolean updateCurrentUserProfile(AccountProfileUpdateCommand command);

    Boolean updateCurrentUsername(AccountUsernameUpdateCommand command);

    Boolean updateCurrentUserPassword(AccountPasswordUpdateCommand command);

    void updateCurrentUserAvatar(String avatarUrl);

    Boolean updateCurrentUserLanguage(String language);

    Boolean requestAccountDeletion(AccountDeletionCommand command);

    Boolean recoverCurrentAccount(AccountRecoveryCommand command);
}
