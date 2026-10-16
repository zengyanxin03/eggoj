package com.eggoj.model.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 *
 */
public enum JudgeInfoMessageEnum {
    // value这栏是保存到数据库的
    ACCEPTED("成功", "Accepted"),
    Wrong_ANSWER("答案错误", "Wrong Answer"),
    MEMORY_LIMIT_EXCEEDED("内存溢出", "MEMORY LIMIT EXCEEDED"),
    TIME_LIMIT_EXCEEDED("超时", "TIME LIMIT EXCEEDED"),
    PRESENTATION_ERROR("展示错误", "PRESENTATION ERROR"),
    WAITING("等待中", "WAITING"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出", "OUTPUT LIMIT EXCEEDED"),
    DANGEROUS_OPERATION("危险操作", "DANGEROUS OPERATION"),
    RUNNING_ERROR("运行错误", "RUNNING ERROR"),
    SYSTEM_ERROR("系统错误", "SYSTEM ERROR"),
    COMPILE_ERROR("编译错误", "COMPILE ERROR");

    private final String text;

    private final String value;

    JudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
