package com.onion.onionserver.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");
    
    // 存储原始字符串（如 "Not Started"）
    private final String displayName;
    
    // 构造方法：初始化枚举常量时绑定显示值
    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }
    
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    @JsonCreator
    public static ProjectStatus fromDisplayValue(String displayName) {
        // 遍历所有枚举常量，匹配 displayName（忽略空值和大小写，可按需调整）
        for (ProjectStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态值：" + displayName);
    }
}
