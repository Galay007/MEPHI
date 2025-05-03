import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

interface MyExecutor extends Executor {
    void execute(Runnable command);

    <T> Future<T> submit(Callable<T> callable);

    void shutdown();

    void shutdownNow();
}

class MyPoolExecutor implements MyExecutor {

    private static final Logger logger = Logger.getLogger("compactLogger");

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final RejectedExecutionHandler rejectionHandler;
    private final ThreadFactory threadFactory;
    private final List<BlockingQueue<Runnable>> taskQueues;
    private final List<Worker> workers;
    private final AtomicInteger poolSize = new AtomicInteger(0);
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);
    private volatile boolean isShutdown = false;
    private final Object monitor = new Object();
    private int nextWorkerIndex = 0; // For Round Robin
    private final String poolName; //Add poolName
    private final Object shutdownMonitor = new Object(); // Monitor for shutdown-related operations
    private volatile boolean isTerminated = false;

    public MyPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueSize,
                          int minSpareThreads) {
        this(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, queueSize, minSpareThreads,
                (runnable, executor) -> System.err.print(""),  // Default rejection handler
                new MyThreadFactory("MyPool") // Default thread factory
        );
    }


    public MyPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                          int queueSize, int minSpareThreads, RejectedExecutionHandler rejectionHandler, ThreadFactory threadFactory) {
        if (corePoolSize < 0 || maxPoolSize <= 0 || maxPoolSize < corePoolSize || keepAliveTime < 0 || queueSize <= 0 ||
                minSpareThreads < 0 || minSpareThreads > corePoolSize) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.rejectionHandler = rejectionHandler;
        this.threadFactory = threadFactory;
        this.taskQueues = new ArrayList<>();
        this.workers = new ArrayList<>();
        this.poolName = ((MyThreadFactory) threadFactory).getNamePrefix();

        // Initialize task queues (one per core thread)
        for (int i = 0; i < corePoolSize; i++) {
            taskQueues.add(new LinkedBlockingQueue<>(queueSize));
        }

        // Start initial core threads
        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Command cannot be null");
        }

        if (isShutdown) {
            rejectionHandler.rejectedExecution(command, new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                    keepAliveTime, timeUnit, new LinkedBlockingQueue<Runnable>(queueSize), threadFactory, rejectionHandler));
            return;
        }

        int taskIndex = submittedTaskCount.getAndIncrement() % corePoolSize;
        BlockingQueue<Runnable> queue = taskQueues.get(taskIndex);

        try {
            if (queue.offer(command)) {
                logger.log(Level.INFO, "[Pool] Task accepted into queue # " +
                        taskIndex + ": (task " + command.toString() + ")");
            } else {
                // Queue is full, attempt to add a new worker if possible
                synchronized (monitor) {
                    if (poolSize.get() < maxPoolSize) {
                        addWorker(); //Attempt to create a new worker to deal with the overage.
                        taskIndex = submittedTaskCount.getAndIncrement() % corePoolSize;
                        queue = taskQueues.get(taskIndex);
                        if (queue.offer(command)) {
                            logger.log(Level.INFO, "[Pool] Task accepted into queue # " + taskIndex +
                                    ": (task " + command.toString() + ")");
                        } else {
                            rejectionHandler.rejectedExecution(command, new ThreadPoolExecutor(corePoolSize,
                                    maxPoolSize, keepAliveTime, timeUnit, new LinkedBlockingQueue<Runnable>(queueSize),
                                    threadFactory, rejectionHandler));
                        }
                    } else {
                        // All workers busy, queue full, reject the task
                        logger.log(Level.INFO, "[Rejected] Task (" + command.toString() + ") was rejected due to overload!");
                        rejectionHandler.rejectedExecution(command, new ThreadPoolExecutor(corePoolSize,
                                maxPoolSize, keepAliveTime, timeUnit, new LinkedBlockingQueue<Runnable>(queueSize),
                                threadFactory, rejectionHandler));
                    }
                }
            }
        } catch (RejectedExecutionException e) {
            logger.log(Level.SEVERE, "Task rejected", e);
            rejectionHandler.rejectedExecution(command, new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                    keepAliveTime, timeUnit, new LinkedBlockingQueue<Runnable>(queueSize), threadFactory, rejectionHandler));
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<>(callable);
        execute(futureTask);
        return futureTask;
    }

    private void addWorker() {
        synchronized (monitor) {
            if (poolSize.get() >= maxPoolSize || isShutdown) {
                return;
            }

            int workerIndex = nextWorkerIndex % corePoolSize;
            nextWorkerIndex++;

            Worker worker = new Worker(taskQueues.get(workerIndex));
            Thread thread = threadFactory.newThread(worker);
            workers.add(worker);
            thread.start();
            poolSize.incrementAndGet();
        }
    }

    private void ensureMinSpareThreads() {
        synchronized (monitor) {
            long availableWorkers = workers.stream().filter(w -> w.currentState == Worker.WorkerState.READY).count();
            if (availableWorkers < minSpareThreads) {
                int threadsToAdd = (int) Math.min(maxPoolSize - poolSize.get(), minSpareThreads - availableWorkers);
                for (int i = 0; i < threadsToAdd; i++) {
                    addWorker();
                }
            }
        }
    }


    @Override
    public void shutdown() {
        synchronized (shutdownMonitor) {
            if (isShutdown) {
                return;  // Already shut down
            }
            isShutdown = true;

            // Prevent accepting new tasks
            for (BlockingQueue<Runnable> queue : taskQueues) {
                queue.clear(); // Option 1: Remove pending tasks
                // queue.drainTo(new ArrayList<>()); // Option 2: Save pending tasks
            }

            // Interrupt idle workers to speed up shutdown
            for (Worker worker : workers) {
                worker.interrupt();
            }
        }
    }

    @Override
    public void shutdownNow() {
        synchronized (shutdownMonitor) {
            isShutdown = true;
            for (BlockingQueue<Runnable> queue : taskQueues) {
                queue.clear();
            }
            for (Worker worker : workers) {
                worker.interrupt();
            }
            workers.clear();
            poolSize.set(0);
        }
    }

    private class Worker implements Runnable {

        private final BlockingQueue<Runnable> taskQueue;
        private volatile Thread currentThread;

        enum WorkerState {
            READY,
            RUNNING,
            TERMINATED
        }

        private volatile WorkerState currentState = WorkerState.READY;


        public Worker(BlockingQueue<Runnable> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {
            currentThread = Thread.currentThread();
            String threadName = Thread.currentThread().getName();
            try {
                Runnable task;
                while (!isShutdown) {
                    currentState = WorkerState.READY; // Set state to READY before polling
                    try {
                        task = taskQueue.poll(keepAliveTime, timeUnit);
                        if (task != null) {
                            currentState = WorkerState.RUNNING; // Set state to RUNNING when task is about to execute
                            logger.log(Level.INFO, "[Worker] " + threadName +
                                    " executes (task " + task.toString() + ")");
                            task.run();
                        } else {
                            // No task received within keepAliveTime, check if we need to terminate
                            synchronized (monitor) {
                                if (poolSize.get() > corePoolSize) {
                                    workers.remove(this);
                                    poolSize.decrementAndGet();
                                    logger.log(Level.INFO, "[Worker] " + threadName + " idle timeout, stopping.");
                                    break; // Terminate the thread
                                } else {
                                    // Ensure minSpareThreads before continuing
                                    ensureMinSpareThreads();
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.out.println("Task was failed " + e);
                    } finally {
                        currentState = WorkerState.READY;
                    }
                }
            } finally {
                currentState = WorkerState.TERMINATED;
                logger.log(Level.INFO, "[Worker] " + poolName + "-" + threadName + " terminated");
                synchronized (shutdownMonitor) {
                    workers.remove(this);
                    if (workers.isEmpty()) {
                        isTerminated = true;
                        shutdownMonitor.notifyAll();  // Wake up awaitTermination
                    }
                }
            }
        }

        public void interrupt() {
            if (currentThread != null) {
                currentThread.interrupt();
            }
        }
    }

    public static class MyThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public MyThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-worker-" + threadNumber.getAndIncrement());
            logger.log(Level.INFO, "[ThreadFactory] Creating new thread: " + thread.getName());
            return thread;
        }

        public String getNamePrefix() {
            return namePrefix;
        }
    }
}
