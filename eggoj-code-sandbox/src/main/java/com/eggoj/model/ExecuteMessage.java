package com.eggoj.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    // 退出码
    private Integer exitValue;

    // 正常输出
    private String message;

    // 异常输出
    private String errorMessage;

    // 进程执行时间
    private Long time;

    // 占用内存量
    private Long memory;
}
