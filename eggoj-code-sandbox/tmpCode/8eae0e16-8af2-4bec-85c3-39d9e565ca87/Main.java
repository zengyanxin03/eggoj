/**
 * 无线睡眠: 执行阻塞，占用资源不释放
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
    }
}
