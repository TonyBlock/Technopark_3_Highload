package web_server;

class ThreadPool {
    private final AsyncQueue tasks;
   private volatile boolean isStopped = false;

    ThreadPool(int threadsAmount, int tasksAmount) {
        tasks = new AsyncQueue(tasksAmount);
        for (int i = 0; i < threadsAmount; i++) {
            new Thread(new ServerThread()).start();
        }
    }

    public void addTask(Runnable newTask) {
        if (!isStopped)
            tasks.add(newTask);
    }

    public void stop() {
        isStopped = true;
    }

    private final class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped) {
                try {
                    tasks.remove().run();
                } catch (Exception e) {
                    break;
                }
            }
        }
    }
}
