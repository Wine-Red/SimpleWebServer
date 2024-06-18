package com.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleWebServer {

    // 服务器监听的端口号
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动，监听端口：" + PORT);

            while (true) {
                // 等待客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接：" + clientSocket.getInetAddress());

                // 处理客户端请求
                handleClientRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (
                // 输入流，用于读取客户端发送的请求
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // 输出流，用于向客户端发送响应
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream())
        ) {
            // 读取请求的第一行（请求行）
            String requestLine = in.readLine();
            if (requestLine != null && !requestLine.isEmpty()) {
                System.out.println("请求行：" + requestLine);

                // 解析请求行
                String[] tokens = requestLine.split(" ");
                if (tokens.length < 2) {
                    sendErrorResponse(out, 400, "400 Bad Request");
                    return;
                }

                String method = tokens[0]; // 请求方法
                String requestedFile = tokens[1]; // 请求的文件路径

                // 如果请求方法不是 GET，返回 405 方法不允许
                if (!method.equals("GET")) {
                    sendErrorResponse(out, 405, "405 Method Not Allowed");
                    return;
                }

                // 构建文件路径
                File file = new File("www" + requestedFile);
                if (file.exists() && !file.isDirectory()) {
                    sendFileResponse(out, file);
                } else {
                    sendErrorResponse(out, 404, "404 Not Found");
                }
            } else {
                sendErrorResponse(out, 400, "400 Bad Request");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendFileResponse(PrintWriter out, File file) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            // 发送响应行和头部
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + file.length());
            out.println(); // 空行表示头部结束
            out.flush();

            // 发送文件内容
            String line;
            while ((line = fileReader.readLine()) != null) {
                out.println(line);
            }
            out.flush();
        }
    }

    private static void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        // 发送错误响应行和头部
        out.println("HTTP/1.0 " + statusCode + " " + message);
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>" + message + "</h1></body></html>");
        out.flush();
    }
}
