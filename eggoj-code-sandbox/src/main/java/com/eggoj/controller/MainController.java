package com.eggoj.controller;

import com.eggoj.JavaCodeSandboxTemplate;
import com.eggoj.model.ExecuteCodeRequest;
import com.eggoj.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    @Resource
    private JavaCodeSandboxTemplate javaCodeSandboxTemplate;

    // 定义鉴权请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }

        if(executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaCodeSandboxTemplate.executeCode(executeCodeRequest);

    }

    @GetMapping("/health")
    public String healthCheck(){
        return "ok";
    }
}
