//package com.yupi.eggoj;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.io.resource.ResourceUtil;
//import cn.hutool.core.util.ArrayUtil;
//import cn.hutool.core.util.StrUtil;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.model.*;
//import com.github.dockerjava.core.DockerClientBuilder;
//import com.github.dockerjava.core.command.ExecStartResultCallback;
//import com.yupi.eggoj.model.ExecuteCodeRequest;
//import com.yupi.eggoj.model.ExecuteCodeResponse;
//import com.yupi.eggoj.model.ExecuteMessage;
//import com.yupi.eggoj.model.JudgeInfo;
//import com.yupi.eggoj.utils.ProcessUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class JavaDockerCodeSandboxTemplateImpl extends JavaCodeSandboxTemplate {
//    // 5s超时时间
//    private static final long TIME_OUT = 5000L;
//
//    private static final Boolean firstInit = true;
//
//    public static void main(String[] args) {
//        JavaDockerCodeSandboxTemplateImpl javaDockerCodeSandboxTemplate = new JavaDockerCodeSandboxTemplateImpl();
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
//        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage("java");
//        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandboxTemplate.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//
//    @Override
//    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
//        // 3.创建容器，把文件复制到容器内---------------------------------------------------------------------------
//        // 获取默认的Docker Client
//        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
//
//        String image = "openjdk:8-alpine";
//        if(firstInit){
//            // 拉取镜像
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//                @Override
//                public void onNext(PullResponseItem item) {
//                    System.out.println("下载镜像:" +item.getStatus());
//                    super.onNext(item);
//                }
//            };
//
//            try {
//                pullImageCmd
//                        .exec(pullImageResultCallback)
//                        .awaitCompletion();
//            } catch (InterruptedException e) {
//                System.out.println("拉取镜像异常");
//                throw new RuntimeException(e);
//            }
//        }
//        System.out.println("拉取镜像/下载完成");
//
//        // 创建容器
//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        HostConfig hostConfig = new HostConfig();
//        // 限制内存 100MB
//        hostConfig.withMemory(100 * 1000 * 1000L);
//        // 不设置内存交换
//        hostConfig.withMemorySwap(0L);
//        // 限制容器cpu使用核数
//        hostConfig.withCpuCount(1L);
//        // 配置linux内核安全机制seccomp
////        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"))
//        // 本地文件夹映射到容器中的文件夹
//        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
//
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withHostConfig(hostConfig)
//                // 把docker容器和你本地的终端进行连接，docker容器可以获取你在本机上的输入并且可在本机上进行输出
//                .withAttachStdin(true)
//                .withAttachStdout(true)
//                .withAttachStderr(true)
//                // 创建一个交互终端
//                .withTty(true)
//                // 禁止该创建的容器访问网络
//                .withNetworkDisabled(true)
//                // 禁止用户向容器环境的根目录写操作
//                .withReadonlyRootfs(true)
//                .exec();
//        System.out.println(createContainerResponse);
//        String containerId = createContainerResponse.getId();
//
//        // 启动容器
//        dockerClient.startContainerCmd(containerId).exec();
//
//        // 进入容器，创建命令，执行命令，获取结果----------------------------------------------------------------------------
//        // docker exec 799a95 java -cp /app Main 1 3
////        String[] cmdArray = new String[] { "java", "-cp", "/app", "Main", "1", "3"};
//        List<ExecuteMessage> executeMessageList = new ArrayList<>();
//        final String[] message = {null};
//        final String[] errorMessage = {null};
//
//        for (String inputArgs: inputList){
//            StopWatch stopWatch = new StopWatch();
//
//            String[] inputArgsArray = inputArgs.split(" ");
//            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
//            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                    .withCmd(cmdArray)
//                    //拿到控制台的输出内容
//                    .withAttachStdin(true)
//                    .withAttachStdout(true)
//                    .withAttachStderr(true)
//                    .exec();
//            System.out.println("创建执行命令："+execCreateCmdResponse);
//            String execId = execCreateCmdResponse.getId();
//            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//                @Override
//                public void onNext(Frame frame) {
//                    StreamType streamType = frame.getStreamType();
//                    if(StreamType.STDERR.equals(streamType)){
//                        errorMessage[0] = new String(frame.getPayload());
//                        System.out.println("输出错误结果:" + errorMessage[0]);
//                    }else{
//                        message[0] = new String(frame.getPayload());
//                        System.out.println("输出结果:" + message[0]);
//                    }
//                    super.onNext(frame);
//                }
//            };
//
//            // 运行命令前，先开启内存监控
//            // 获取占用的内存
//            final Long[] maxMemory = {0L};
//            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
//            // 该查看docker状态的命令无对应的awaitCompletion阻塞等待命令执行完的方法，可能表明内存监控是无休止的，程序运行中会一直监控下去
//            statsCmd.exec(new ResultCallback<Statistics>() {
//                @Override
//                public void onStart(Closeable closeable) {
//
//                }
//
//                @Override
//                public void onNext(Statistics statistics) {
//                    System.out.println("内存占用:" + statistics.getMemoryStats().getUsage());
//                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//
//                }
//
//                @Override
//                public void onComplete() {
//
//                }
//
//                @Override
//                public void close() throws IOException {
//
//                }
//            });
//
//            try {
//                // 运行时间计时开始
//                stopWatch.start();
//
//                dockerClient.execStartCmd(execId)
//                        .exec(execStartResultCallback)
//                        //阻塞等待程序结果
//                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
//                // 停止计时
//                stopWatch.stop();
//            } catch (InterruptedException e) {
//                System.out.println("程序执行异常");
//                throw new RuntimeException(e);
//            }
//
//            // 获取执行命令后的输出值
//            ExecuteMessage executeMessage = new ExecuteMessage();
//            executeMessage.setMessage(message[0]);
//            executeMessage.setErrorMessage(errorMessage[0]);
//            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
//            executeMessage.setMemory(maxMemory[0]);
//            executeMessageList.add(executeMessage);
//        }
//        //-----------------------------------------------------------------------------------------------------------------
//        return executeMessageList;
//    }
//}
