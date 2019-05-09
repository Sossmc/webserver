import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Server {
    private ServerSocket serverSocket;
    private Server(int TCP_PORT){
        try{
            serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Listening Port " + TCP_PORT + "...");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void listen() throws IOException {
        while(true){
            final Socket socket = serverSocket.accept();    //listening to the port
            new Thread(() -> service(socket)).start();      //new a thread for the connection
        }
    }

    private void service(Socket socket) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            /* 读取请求信息内容 */
            Request request = new RequestParser().parse(inputStream);
            Response response = new Response(outputStream);
            ServiceDispatcher serviceDispatcher = new ServiceDispatcher();
            serviceDispatcher.execController(request, response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Connected: " + socket.getInetAddress() + ":" + socket.getPort());
    }

    private void dispatcher(Request request,Response response){

    }

    public static void main(String[] args){
        try {
            new Server(4206).listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
