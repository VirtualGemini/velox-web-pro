package com.velox.module.system.mail.vo;

import java.util.ArrayList;
import java.util.List;

public class MailGroupBatchDeleteResultVO {

    private List<String> inUseNames = new ArrayList<>();

    public List<String> getInUseNames() {
        return inUseNames;
    }

    public void setInUseNames(List<String> inUseNames) {
        this.inUseNames = inUseNames;
    }
}
