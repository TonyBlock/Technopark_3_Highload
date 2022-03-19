package web_server;

import java.util.LinkedList;

class AsyncQueue {
    private final int limit;
    private final LinkedList<Runnable> queue;

    AsyncQueue(int limit) {
        this.limit = limit;
        this.queue = new LinkedList<>();
    }

    public synchronized void add(Runnable item) {
        while (queue.size() == limit) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        queue.addLast(item);
        notify();
    }

    public synchronized Runnable remove() {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Runnable handler = queue.removeFirst();
        notify();
        return handler;
    }
}
