import ThreadControl.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadControlTest {
    private Scheduler ioScheduler;
    private Scheduler computationScheduler;
    private Scheduler singleThreadScheduler;

    @BeforeEach
    void setUp() {
        ioScheduler = new IOThreadScheduler();
        computationScheduler = new ComputationScheduler();
        singleThreadScheduler = new SingleThreadScheduler();
    }

    @AfterEach
    void tearDown() {
        ioScheduler.shutdown();
        computationScheduler.shutdown();
        singleThreadScheduler.shutdown();
    }

    @Test
    void ioToComputationSchedulerTest() throws InterruptedException {
        MyThreadControl<String> observable = new MyThreadControl<>("Data IO");
        observable.subscribeOn(ioScheduler)
                .observeOn(ioScheduler)
                .doOnNext(data -> System.out.println("Processing: " + data + " Thread: " + Thread.currentThread().getName()))
                .subscribe(data -> System.out.println("Received: " + data + " Thread: " + Thread.currentThread().getName()));

        Thread.sleep(100); // Wait for the threads to complete

        assertEquals("Data IO",observable.getData());
    }

    @Test
    void computationToSingleSchedulerTest() throws InterruptedException {
        MyThreadControl<String> observable = new MyThreadControl<>("Data Computation");
        observable.subscribeOn(computationScheduler)
                .observeOn(computationScheduler)
                .doOnNext(data -> System.out.println("Processing: " + data + " Thread: " + Thread.currentThread().getName()))
                .subscribe(data -> System.out.println("Received: " + data + " Thread: " + Thread.currentThread().getName()));

        Thread.sleep(100); // Wait for the threads to complete

        assertEquals("Data Computation",observable.getData());
    }

    @Test
    void singleToIOSchedulerTest() throws InterruptedException {
        MyThreadControl<String> observable = new MyThreadControl<>("Data Single");
        observable.subscribeOn(singleThreadScheduler)
                .observeOn(singleThreadScheduler)
                .doOnNext(data -> System.out.println("Processing: " + data + " Thread: " + Thread.currentThread().getName()))
                .subscribe(data -> System.out.println("Received: " + data + " Thread: " + Thread.currentThread().getName()));

        Thread.sleep(100); // Wait for the threads to complete

        assertEquals("Data Single",observable.getData());
    }
}
