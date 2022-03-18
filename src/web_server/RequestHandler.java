package web_server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

class RequestHandler implements Runnable{
    protected Socket clientSocket;
    private static String DEFAULT_FILES_DIR;
    private static final int BUFFER_SIZE = 2048;
    private final static char CR  = (char) 0x0D;
    private final static char LF  = (char) 0x0A;
    private final static String CRLF  = "" + CR + LF;

    RequestHandler(Socket clientSocket, String path) {
        this.clientSocket = clientSocket;
        DEFAULT_FILES_DIR = System.getProperty("user.dir") + path;
    }

    @Override
    public void run() {
        InputStream input = null;
        OutputStream output = null;

        try {
            clientSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            //e.printStackTrace();
        }
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();

            String readRequest = readRequest(input);
            String method = getRequestMethod(readRequest);

            if (method == null)
                throw new IOException();

            switch (method) {
                case "GET": {
                    String url = getRequestURL(readRequest);
                    sendFile(url, output, false);
                    break;
                }
                case "HEAD": {
                    String url = getRequestURL(readRequest);
                    sendFile(url, output, true);
                    break;
                }
                default:
                    writeResponseHeader(output, 405, null, 0);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (output != null)
                    output.close();
                else
                    clientSocket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    private String readRequest(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String ln = null;
        while (true) {
            ln = reader.readLine();
            if (ln == null || ln.isEmpty()) {
                break;
            }
            builder.append(ln).append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    private String getRequestURL(String header) {
        int from = header.indexOf(" ") + 1;
        if (from == 0)
            return DEFAULT_FILES_DIR+"/index.html";

        int to = header.indexOf(" ", from);
        if (to == -1)
            return DEFAULT_FILES_DIR+"/index.html";

        String uri = header.substring(from, to);
        uri = java.net.URLDecoder.decode(uri, StandardCharsets.UTF_8);
        if (uri.lastIndexOf("/") == uri.length() - 1)
            if (!uri.contains("."))
                return DEFAULT_FILES_DIR + uri+"index.html";
            else uri += "badPath";

        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1)
            uri = uri.substring(0, paramIndex);

        if (isURLDangerous(uri))
            return null;

        return DEFAULT_FILES_DIR + uri;
    }

    private static String getRequestMethod(String header) {
        int to = header.indexOf(" ");
        if (to == -1) {
            return null;
        }
        return header.substring(0,to);
    }

    private void sendFile(String url, OutputStream out, Boolean isHead) {
        if (url == null) {
            writeResponseHeader(out, 403, null, 0);
            return;
        }
        int code = 200;
        String mime = null;
        int size = 0;

        try {
            File file = new File(url);
            mime = getContentType(file);
            size = (int)file.length();
            FileInputStream fin = new FileInputStream(file);

            writeResponseHeader(out, code, mime, size);

            if (!isHead) {
                int count;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = fin.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
            }
            fin.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            code = url.contains("/index.html") ? 403 : 404;
        }
        if (code != 200)
            writeResponseHeader(out, code, mime, size);
    }

    private void writeResponseHeader(OutputStream out, int code, String mime, int size) {
        String header = createResponseHeader(code, mime, size);
        PrintStream answer = new PrintStream(out, true, StandardCharsets.UTF_8);
        answer.print(header);
    }

    private String createResponseHeader(int code, String contentType, int contentLength) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("HTTP/1.1 ").append(code).append(" ").append(getAnswer(code)).append(CRLF);
        buffer.append("Server: Java web-server" + CRLF);
        buffer.append("Connection: close" + CRLF);
        buffer.append("Date: ").append(new Date()).append(CRLF);
        buffer.append("Accept-Ranges: none " + CRLF);
        if (code == 200) {
            if (contentType != null)
                buffer.append("Content-Type: ").append(contentType).append(CRLF);
            if (contentLength != 0)
                buffer.append("Content-Length: ").append(contentLength).append(CRLF);
        }
        buffer.append(CRLF);
        return buffer.toString();
    }

    private String getContentType(File file) throws IOException {
        int index = file.getPath().lastIndexOf('.');
        if (index > 0) {
            if (file.getPath().substring(index + 1).equals("swf")) {
                return "application/x-shockwave-flash";
            }
        }

        return Files.probeContentType(file.toPath());
    }

    private String getAnswer(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method not allowed";
            default:
                return "Internal Server Error";
        }
    }

    private int subStrInStr(String origin, String subStr) {
        int count = 0;
        while (origin.contains(subStr)){
            origin = origin.replaceFirst(subStr, "");
            count++;
        }
        return count ;
    }

    private Boolean isURLDangerous(String url) {
        int backnesting = subStrInStr(url, "/..");
        if (backnesting > 0) {
            int nesting = subStrInStr(url, "/") - 2 * backnesting;
            return nesting < 0;
        }
        return false;
    }
}
