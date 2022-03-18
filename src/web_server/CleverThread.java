package web_server;

class CleverThread {
    private final Thread thread;

    CleverThread(AsyncQueue taskQueue) {
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    RequestHandler task;
                    //synchronized (taskQueue) {
                    task = taskQueue.remove();
                    //}
                    task.run();
                } catch (Exception e) {
                    //e.printStackTrace();
                    break;
                }
            }
        });
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}
