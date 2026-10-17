package com.eggoj.utils;

import com.eggoj.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * 与进程有关的工具类
 */
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName){
        ExecuteMessage executeMessage = new ExecuteMessage();
        // 运行时间计时开始
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 判断结束码，得知是正常退出还是异常退出
            if(exitValue == 0){
                // 正常退出
                System.out.println( opName +"成功");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compileOutputLine;
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                while ((compileOutputLine = bufferedReader.readLine()) != null){
                    // System.out.println(compileOutputLine);
                    compileOutputStringBuilder.append(compileOutputLine);
//                    compileOutputStringBuilder.append(compileOutputLine).append("\n");
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
                System.out.println(compileOutputStringBuilder);
            }else{
                // 异常退出
                System.out.println(opName + "失败,错误码："+ exitValue);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compileOutputLine;
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                while ((compileOutputLine = bufferedReader.readLine()) != null){
                    // System.out.println(compileOutputLine);
                    compileOutputStringBuilder.append(compileOutputLine);
//                    compileOutputStringBuilder.append(compileOutputLine).append("\n");
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());


                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                String errorCompileOutputLine;
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null){
                    // System.out.println(errorCompileOutputLine);
                    errorCompileOutputStringBuilder.append(errorCompileOutputLine);
//                    errorCompileOutputStringBuilder.append(errorCompileOutputLine).append("\n");
                }
                executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());

            }
            // 停止计时
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        }catch (Exception e){
            e.printStackTrace();
        }
        return executeMessage;
    }
}
