package ThreadControl;

public interface Scheduler {
    void execute(Runnable task);
    public void shutdown();

}
