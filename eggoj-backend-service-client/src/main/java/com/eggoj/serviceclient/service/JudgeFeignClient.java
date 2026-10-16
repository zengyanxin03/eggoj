package com.eggoj.serviceclient.service;


import com.eggoj.model.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务
 */
@FeignClient(name = "eggoj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     *
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("/questionSubmitId") long questionSubmitId);
}
