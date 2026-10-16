package com.eggoj.judgeservice.judge.codesandbox;


import com.eggoj.model.codesandbox.ExecuteCodeRequest;
import com.eggoj.model.codesandbox.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
