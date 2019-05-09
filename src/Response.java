import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

class Response {

    private static final String phpPath = "C:\\Users\\sossm\\scoop\\shims\\php.exe";
    private static final String rootPath = "./web";
    private Socket socket;

    /**
     * 根据请求类型及URI等请求信息，找到并执行对应的控制器方法后返回
     * 此处直接返回一个控制器，模拟查找和执行控制器方法的过程
     */
    Response(Socket socket)throws IOException{
        System.out.println("Connected from Port " + socket.getPort());
        this.socket = socket;
        Request request = new Request(socket.getInputStream());
        if(request.postData != null || request.getContentType().equals("php")){
            writeText(postToPHP(request), "200", request.getContentType());//运行php程序返回
            return;
        }
        byte[] text = getControllerResult(request);
        if(text == null) text = "".getBytes();
        else if(new String(text).equals("404")) {
            writeText("没有找到对应路径的文件！".getBytes(), "404", request.getContentType());
            return;
        }
        // 输出控制器返回结果
        writeText(text, "200", request.getContentType());
    }

    /**
     * 模拟查找和执行控制器方法并返回结果
     */
    private byte[] getControllerResult(Request request) {
        byte[] context = null;
        String uri = request.getUri();
        String path = "."+uri;
        File requestFile = new File(path);
        Path path1 = Paths.get(path);
        try{
            if(requestFile.exists()) context = Files.readAllBytes(path1);
        }catch (FileNotFoundException e){
            System.out.println("Could not find the file");
            context = "404".getBytes();
        }catch (IOException e){
            e.printStackTrace();
        }
        return context;
    }

    private byte[] postToPHP(Request request){
        StringBuilder phpCommands = new StringBuilder();
        byte[] buff =  new byte[1024];

        for (var i:buff){
            System.out.print(i==0? "" : (char)i);
        }
        for(int i = 0; i < request.getPostDatas().size(); i++){
            String[] parameter = request.getPostDatas().get(i).split("=");
            if(parameter.length == 2)
                phpCommands.append("$_POST['").append(parameter[0]).append("'] = '").append(parameter[1]).append("';");
            if(parameter.length == 1)
                phpCommands.append("$_POST['").append(parameter[0]).append("'] = null;");
        }
        try{
            Process p = new ProcessBuilder(phpPath,"-B",phpCommands.toString(),"-F",rootPath + request.getUri()).start();
            System.out.println(phpCommands.toString());
            OutputStream out = p.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write("\r\n");
            writer.flush();
            writer.close();
            InputStream in = p.getInputStream();
            int length = in.read(buff);
            System.out.println(new String(buff));
            return Arrays.copyOfRange(buff,0, length);
        }catch (IOException e){
            System.out.println("PHP ERROR！");
            e.printStackTrace();
        }
        return "".getBytes();
    }

    private void writeText(byte[] text, String status, String type) {
        try {
            OutputStream output = socket.getOutputStream();
            String line = "HTTP/1.1 " + status + " OK\n";
            output.write(line.getBytes());
            switch (type) {
                case "php":
                case "html":
                    type = "text/html"; break;
                case "js":
                    type = "application/javascript"; break;
                case "css":
                    type = "text/css"; break;
                case "jpeg":
                case "jpg":
                    type = "image/jpeg"; break;
                default:
                    type = "text/plain";
            }

            output.write(("Accept-Ranges: bytes\n").getBytes());
            output.write(("Content-Type: "+type+"; charset=UTF-8\n\n").getBytes());
            if(text != null) output.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}