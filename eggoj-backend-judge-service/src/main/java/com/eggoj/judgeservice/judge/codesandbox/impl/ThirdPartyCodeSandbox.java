package com.eggoj.judgeservice.judge.codesandbox.impl;


import com.eggoj.judgeservice.judge.codesandbox.CodeSandbox;
import com.eggoj.model.codesandbox.ExecuteCodeRequest;
import com.eggoj.model.codesandbox.ExecuteCodeResponse;

/**
 * 第三方代码沙箱(调用现成的代码沙箱,如：go-judge)
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
