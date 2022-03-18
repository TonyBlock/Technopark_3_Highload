package web_server;

import java.util.ArrayList;
import java.util.List;

class ThreadPool {
    private final AsyncQueue tasks;
    private final List<CleverThread> threads;
    boolean isStopped = false;

    ThreadPool(int threadsAmount, int tasksAmount) {
        tasks = new AsyncQueue(tasksAmount);
        threads = new ArrayList<>();
        for (int i = 0; i < threadsAmount; i++) {
            threads.add(new CleverThread(tasks));
        }
    }

    public synchronized void addTask(RequestHandler newTask) {
        if (isStopped)
            return;
        tasks.add(newTask);
    }

    public synchronized void stop() {
        isStopped = true;
        for (CleverThread thread : threads)
            thread.stop();
    }
}
