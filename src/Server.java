import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Server {

    public static void main(String[] args){
        int TCP_PORT = 80;
        System.out.println("Starting Server...\r\nListening On Port: " + TCP_PORT);
        try {
            new Server(TCP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Server(int TCP_PORT) throws IOException{
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("http://127.0.0.1:80/web/index.html");
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            //noinspection LoopConditionNotUpdatedInsideLoop
            while (serverSocket != null) {
                final Socket socket = serverSocket.accept();    //listening to the port
                new Thread(() -> {
                    try{
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        System.out.println("Connected from Port " + socket.getPort());
                        /* 读取请求信息内容 */
                        Request request = new Request().parse(inputStream);
                        new Response(outputStream).execController(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (socket != null) try {
                            socket.close();
                            System.out.println("Port " + socket.getPort() + " Closed");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();      //new a thread for the connection
            }
        }
    }
}
