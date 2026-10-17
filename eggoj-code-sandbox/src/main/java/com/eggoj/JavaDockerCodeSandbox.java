package com.eggoj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.eggoj.model.ExecuteCodeRequest;
import com.eggoj.model.ExecuteCodeResponse;
import com.eggoj.model.ExecuteMessage;
import com.eggoj.model.JudgeInfo;
import com.eggoj.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JavaDockerCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    // 5s超时时间
    private static final long TIME_OUT = 5000L;

    private static final List<String> blackList = Arrays.asList("Files","exec");

    private static final String SECURITY_MANAGER_PATH = "D:\\code_project\\eggoj-code-sandbox\\src\\main\\resources\\security";

    public static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    private static final Boolean firstInit = true;



    public static void main(String[] args) {
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/SleepError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/ReadFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/WriteFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);

        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        System.setSecurityManager(new DefaultSecurityManager());
//        System.setSecurityManager(new DenySecurityManager());


        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();



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

        // 2.编译代码，得到class文件
        // Runtime类的exec方法：执行某一个命令行
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());

        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        // 3.创建容器，把文件复制到容器内---------------------------------------------------------------------------
        // 获取默认的Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        String image = "openjdk:8-alpine";
        if(firstInit){
            // 拉取镜像
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像:" +item.getStatus());
                    super.onNext(item);
                }
            };

            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }
        System.out.println("拉取镜像/下载完成");

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        // 限制内存 100MB
        hostConfig.withMemory(100 * 1000 * 1000L);
        // 不设置内存交换
        hostConfig.withMemorySwap(0L);
        // 限制容器cpu使用核数
        hostConfig.withCpuCount(1L);
        // 配置linux内核安全机制seccomp
//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"))
        // 本地文件夹映射到容器中的文件夹
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                // 把docker容器和你本地的终端进行连接，docker容器可以获取你在本机上的输入并且可在本机上进行输出
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                // 创建一个交互终端
                .withTty(true)
                // 禁止该创建的容器访问网络
                .withNetworkDisabled(true)
                // 禁止用户向容器环境的根目录写操作
                .withReadonlyRootfs(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 进入容器，创建命令，执行命令，获取结果----------------------------------------------------------------------------
        // docker exec 799a95 java -cp /app Main 1 3
//        String[] cmdArray = new String[] { "java", "-cp", "/app", "Main", "1", "3"};
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        final String[] message = {null};
        final String[] errorMessage = {null};

        for (String inputArgs: inputList){
            StopWatch stopWatch = new StopWatch();

            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    //拿到控制台的输出内容
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            System.out.println("创建执行命令："+execCreateCmdResponse);
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if(StreamType.STDERR.equals(streamType)){
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果:" + errorMessage[0]);
                    }else{
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果:" + message[0]);
                    }
                    super.onNext(frame);
                }
            };

            // 运行命令前，先开启内存监控
            // 获取占用的内存
            final Long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            // 该查看docker状态的命令无对应的awaitCompletion阻塞等待命令执行完的方法，可能表明内存监控是无休止的，程序运行中会一直监控下去
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用:" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });

            try {
                // 运行时间计时开始
                stopWatch.start();

                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        //阻塞等待程序结果
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                // 停止计时
                stopWatch.stop();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }

            // 获取执行命令后的输出值
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }

        //-----------------------------------------------------------------------------------------------------------------
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，用于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList){
            String errMessage = executeMessage.getErrorMessage();
            // 执行中存在错误
            if(StrUtil.isNotBlank(errMessage)){
                executeCodeResponse.setMessage(errMessage);
                // 表示用户提交的代码出现错误
                executeCodeResponse.setStatus(3);
                break;
            }
            String mess = executeMessage.getMessage();
            outputList.add(mess);

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

        // 4.文件清理
        if(userCodeFile.getParentFile( ) != null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        return executeCodeResponse;
    }

    /**
     * 代码沙箱错误响应
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