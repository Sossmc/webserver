import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

class Response {

    //MARK: php可执行文件的路径 可以用 `which php` 或者 `whereis php`寻找
    private static final String phpPath = "/usr/bin/php";
    //监听的服务器的映射的根目录
    private static final String rootPath = "./web";
    private Socket socket;

    //利用子线程的socket 得到 Response Message
    Response(Socket socket)throws IOException{
        System.out.println("Connecting from Port " + socket.getPort());
        this.socket = socket;
        Request request = new Request(socket.getInputStream());
        byte[] text = findFile(request);
        if (request.postResponse != null || request.getContentType().equals("php"))
            writeText(postToPHP(request), "200", request.getContentType());//运行php程序返回
        else if(new String(text).equals("404"))
                writeText("没有找到对应路径的文件！".getBytes(), "404", request.getContentType());
        else writeText(text, "200", request.getContentType());
    }

    // 查找文件是否存在
    private byte[] findFile(Request request) throws IOException{
        String path = "." + request.getUri();
        File requestFile = new File(path);
        Path path1 = Paths.get(path);
        if(requestFile.exists()) return Files.readAllBytes(path1);
        else{
            System.out.println("404 File Not Found.");
            return "404".getBytes();
        }
    }

    private byte[] postToPHP(Request request){
        StringBuilder phpCommands = new StringBuilder();
        byte[] buff =  new byte[1024];
        for(int i = 0; i < request.getPostDatas().size(); i++){
            String[] parameter = request.getPostDatas().get(i).split("=");
            phpCommands.append("$_POST['").append(parameter[0]).append("'] = ")
                    .append(parameter.length==2 ? "'"+parameter[1]+"';" : "null;");
        }
        try{
            Process p = new ProcessBuilder(phpPath,"-B",phpCommands.toString(),"-F",rootPath + request.getUri()).start();
            System.out.println(phpCommands.toString());
            OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream());
            writer.write("\r\n");
            writer.flush();
            writer.close();
            InputStream in = p.getInputStream();
            int length = in.read(buff);
            System.out.println("." +request.getUri() +"\t收到请求\t"+ phpCommands.toString()+ "\n\t\t\t返回结果\t" + new String(buff));
            return Arrays.copyOfRange(buff,0, length);
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("PHP错误！请检查php解释器路径是否正确");
        }
        return "".getBytes();
    }

    private void writeText(byte[] text, String status, String type)throws IOException {
            OutputStream output = socket.getOutputStream();
            output.write((status.equals("200") ? "HTTP/1.1 200 OK" : "HTTP/1.1 404 NOT FOUND").getBytes());
            switch (type) {
                case "php":
                case "html":
                    type = "text/html";
                    break;
                case "js":
                    type = "application/javascript";
                    break;
                case "css":
                    type = "text/css";
                    break;
                case "jpeg":
                case "jpg":
                    type = "image/jpeg";
                    break;
                default:
                    type = "text/plain";
            }
            output.write(("Accept-Ranges: bytes\nContent-Type: "+type+"; charset=UTF-8\n\n").getBytes());
            output.write(text);
    }
}