package com.eggoj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.eggoj.model.ExecuteCodeRequest;
import com.eggoj.model.ExecuteCodeResponse;
import com.eggoj.model.ExecuteMessage;
import com.eggoj.model.JudgeInfo;
import com.eggoj.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Java 代码沙箱模板方法的实现
 * 定义模板方法抽象类，定义方法的流程，而不是具体的实现
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    // 5s超时时间
    private static final long TIME_OUT = 5000L;

//    private static final String SECURITY_MANAGER_PATH = "D:\\code_project\\eggoj-code-sandbox\\src\\main\\resources\\security";

//    public static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)  {

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 1.把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2.编译代码，得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);


        // 3.执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);


        // 4.整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);


        // 5.文件清理
        boolean isDelete = deleteFile(userCodeFile);
        if(!isDelete){
            log.error("deleteFile error, userCodeFilePath = {}",userCodeFile.getAbsolutePath());
        }

        return outputResponse;
    }


    /**
     * 1.把用户的代码保存为文件
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code){
        String userDir = System.getProperty("user.dir");
        // 判断全局代码目录是否存在,没有则新建
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if(!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        // 因为所有的文件中的类名都是Main，所以需要把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        // 1.把用户的代码放入一个文件中
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }


    /**
     * 2.编译代码，得到class文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());

        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if(executeMessage.getExitValue() != 0 ){
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3.执行代码，得到输出的结果
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile ,List<String> inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for(String inputArgs: inputList){
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            try {
                //Runtime.getRuntime()会开启一个新的子进程,即runProcess子进程和下面的线程共同运行着，不会阻塞在这里导致分开运行
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 建立一个监视线程，超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时啦，中断");
                        // 超时，立刻杀掉
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误",e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4.获取输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，用于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList){
            String errorMessage = executeMessage.getErrorMessage();
            // 执行中存在错误
            if(StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                // 表示用户提交的代码出现错误
                executeCodeResponse.setStatus(3);
                break;
            }
            String message = executeMessage.getMessage();
            outputList.add(message);

            Long time = executeMessage.getTime();
            if(time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        executeCodeResponse.setOutputList(outputList);
//        executeCodeResponse.setMessage();
        if(outputList.size() == executeMessageList.size()) {
            // 表示用户提交的代码正确
            executeCodeResponse.setStatus(1);
        }
//        executeCodeResponse.setStatus();
        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMessage();
        judgeInfo.setTime(maxTime);
        // 要借助第三方库来获取内存占用，这里不做实现
        // judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5.删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if(userCodeFile.getParentFile( ) != null){
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6.获取代码沙箱错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱出现错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
