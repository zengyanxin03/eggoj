package com.eggoj;

import com.eggoj.model.ExecuteCodeRequest;
import com.eggoj.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * Java 原生代码沙箱实现(直接复用模板方法)
 */
@Component
public class JavaCodeSandboxTemplateImpl extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
