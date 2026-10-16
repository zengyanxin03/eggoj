package com.eggoj.judgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.eggoj.common.common.ErrorCode;
import com.eggoj.common.exception.BusinessException;
import com.eggoj.judgeservice.judge.codesandbox.CodeSandbox;
import com.eggoj.judgeservice.judge.codesandbox.CodeSandboxFactory;
import com.eggoj.judgeservice.judge.codesandbox.CodeSandboxProxy;
import com.eggoj.judgeservice.judge.strategy.JudgeContext;
import com.eggoj.model.codesandbox.ExecuteCodeRequest;
import com.eggoj.model.codesandbox.ExecuteCodeResponse;
import com.eggoj.model.codesandbox.JudgeInfo;
import com.eggoj.model.model.dto.question.JudgeCase;
import com.eggoj.model.model.entity.Question;
import com.eggoj.model.model.entity.QuestionSubmit;
import com.eggoj.model.model.enums.QuestionSubmitStatusEnum;
import com.eggoj.serviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;


    @Value("${codesandbox.type:example}")
    private String type;

    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 传入提交题目的id，获取对应的和提交信息题目信息（包含代码/编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if(questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if(question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        if(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交题目正在判题中");
        }
        // 修改数据库中提交题目信息中的状态字段
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"提交题目状态更新错误");
        }
        // 调用代码沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);

        // 根据沙箱的执行结果，设置题目的判题状态和信息
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();

        // 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setInputList(inputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeInfo(judgeInfo);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo doneJudge = judgeManager.doJudge(judgeContext);

        // 判题结束之后，修改数据库中该条提交题目信息的判题结果judgeInfo
        // 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(doneJudge));
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"提交题目状态更新错误");
        }
        // 再查询数据库，获取最新的提交题目信息返回给前端
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        return questionSubmitResult;
    }
}
