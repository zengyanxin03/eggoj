package com.eggoj.judgeservice.judge;

import com.eggoj.judgeservice.judge.strategy.DefaultJudgeStrategy;
import com.eggoj.judgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.eggoj.judgeservice.judge.strategy.JudgeContext;
import com.eggoj.judgeservice.judge.strategy.JudgeStrategy;
import com.eggoj.model.codesandbox.JudgeInfo;
import com.eggoj.model.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 对 判题策略 进行统一管理：对不同语言调用不同的判题策略
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();

        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        // 根据不同的语言，调用不同的策略
        if("java".equals(language)){
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        JudgeInfo doneJudge = judgeStrategy.doJudge(judgeContext);
        return doneJudge;
    }
}
