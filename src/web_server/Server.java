package web_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final int serverPort;
    private static final int TASKS_PER_THREAD = 10;
    private final String path;
    private ServerSocket serverSocket;
    private boolean isStopped = false;
    private final ThreadPool threadPool;

    public Server(int serverPort, int threadLimit, String path) {
        this.serverPort = serverPort;
        this.threadPool = new ThreadPool(threadLimit, threadLimit * TASKS_PER_THREAD);
        this.path = path;
    }

    // запуск сервера
    @Override
    public void run() {
        openServerSocket();

        while (!isStopped()) {
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept(); // ожидание и получение соединения с клиентом
            } catch (IOException e) {
                if (isStopped()) {
                    break;
                }
                throw new RuntimeException("Error while accepting client connection", e);
            }

            this.threadPool.addTask(new RequestHandler(clientSocket, path));
        }
        System.out.println("server stopped");
    }

    // получение текущего состояния сервера
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    // остановка сервера
    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
            this.threadPool.stop();
        } catch (IOException e) {
            throw new RuntimeException("Error while stopping server", e);
        }
    }

    // открытие сокета сервера на указанном порту
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            //e.printStackTrace();
            System.exit(-1);
        }
    }
}
