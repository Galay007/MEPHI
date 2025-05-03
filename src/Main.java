import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.jmx.JmxConfig;

class Main {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tT.%1$tL %4$s %5$s%6$s%n");


        JmxConfig jmxConfig = k -> null;
        MeterRegistry registry = new JmxMeterRegistry(jmxConfig, Clock.SYSTEM);

        Timer allTasksTimer = registry.timer("threadpool.all.tasks.execution.time");

        MyPoolExecutor myExecutor = new MyPoolExecutor(
                2, // corePoolSize
                4, // maxPoolSize
                5, // keepAliveTime
                TimeUnit.SECONDS,
                2, // queueSize
                1 // minSpareThreads
        );

        for (int i = 0; i < 20; i++) {
            final int taskNumber = i;
            myExecutor.execute(() -> {
                long startTime = System.nanoTime();
                System.out.println(Thread.currentThread().getName() + ": Task " + taskNumber + " started");
                try {
                    Thread.sleep(1000); // Simulate task execution
                } catch (InterruptedException e) {
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;
                    allTasksTimer.record(duration, TimeUnit.NANOSECONDS); // Record the time
                    Thread.currentThread().interrupt();
                } finally {
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;
                    allTasksTimer.record(duration, TimeUnit.NANOSECONDS); // Record the time
                    System.out.println(Thread.currentThread().getName() + ": Task " + taskNumber + " finished");
                }
            });
        }

        Thread.sleep(5000);
        myExecutor.shutdown();
    }
}