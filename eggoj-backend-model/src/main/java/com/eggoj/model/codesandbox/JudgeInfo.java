package com.eggoj.model.codesandbox;

import lombok.Data;

/**
 * 题目用例
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗时间
     */
    private Long time;

    /**
     * 消耗内存(KB)
     */
    private Long memory;

}
