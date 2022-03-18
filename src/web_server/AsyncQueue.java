package web_server;

import java.util.LinkedList;

class AsyncQueue {
    private final int limit;
    private final LinkedList<RequestHandler> queue;

    AsyncQueue(int limit) {
        this.limit = limit;
        this.queue = new LinkedList<>();
    }

    public synchronized void add(RequestHandler item) {
        while (this.queue.size() == limit) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.queue.addLast(item);
        notify();
    }

    public synchronized RequestHandler remove() {
        while (this.queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        RequestHandler handler = queue.removeFirst();
        notify();
        return handler;
    }
}
