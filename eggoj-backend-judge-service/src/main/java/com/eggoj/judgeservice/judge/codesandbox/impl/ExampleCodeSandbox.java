package com.eggoj.judgeservice.judge.codesandbox.impl;

import com.eggoj.judgeservice.judge.codesandbox.CodeSandbox;
import com.eggoj.model.codesandbox.ExecuteCodeRequest;
import com.eggoj.model.codesandbox.ExecuteCodeResponse;
import com.eggoj.model.codesandbox.JudgeInfo;
import com.eggoj.model.model.enums.JudgeInfoMessageEnum;
import com.eggoj.model.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱(仅为了跑通业务流程)
 */
public class ExampleCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("示例代码沙箱");
        List<String> inputList = executeCodeRequest.getInputList();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功" );
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getText());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setTime(100L);
        judgeInfo.setMemory(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
