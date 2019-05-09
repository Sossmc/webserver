import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ServiceDispatcher {
    private String phpPath  = "C:\\Users\\sossm\\scoop\\shims\\php.exe";

    /**
     * 根据请求类型及URI等请求信息，找到并执行对应的控制器方法后返回
     * 此处直接返回一个控制器，模拟查找和执行控制器方法的过程
     */
    void execController(Request request, Response response) {
        if(request.postData != null || request.getContentType().equals("php")){
            response.writeText(postToPHP(request.postData, request.getUri(), request), "200", request.getContentType());//运行php程序返回
            return;
        }
        byte[] text = getControllerResult(request, response);
        if(text == null) text = "".getBytes();
        else if(new String(text).equals("404")) {
            response.writeText("没有找到对应路径的文件！".getBytes(), "404", request.getContentType());
            return;
        }
        // 输出控制器返回结果
        response.writeText(text, "200", request.getContentType());
    }

    /**
     * 模拟查找和执行控制器方法并返回结果
     */
    private byte[] getControllerResult(Request request, Response response) {
        byte[] context = null;
        String uri = request.getUri();
        String [] uriArray = uri.split("/");
        String path = "."+uri;
        File requestFile = new File(path);
        Path path1 = Paths.get(path);
        try{
            if(requestFile.exists()){
                context =  Files.readAllBytes(path1);
            }

        }catch (FileNotFoundException e){
            System.out.println("没有找到路径");
            context = "404".getBytes();
        }catch (IOException e){
            e.printStackTrace();
        }
        return context;
    }

    private byte[] postToPHP(String data, String path, Request request){
        String commandB = "";
        for(int i = 0; i < request.getPostDatas().size(); i++){
            String[] parameter = request.getPostDatas().get(i).split("=");
            if(parameter.length == 2) commandB = commandB + "$_POST['" + parameter[0] + "'] = '" + parameter[1] + "';";
            if(parameter.length == 1) commandB = commandB + "$_POST['" + parameter[0] + "'] = null;";
        }
        String commandF = "./web" + path;
        byte[] buff =  new byte[1024];
        ProcessBuilder pb = new ProcessBuilder(phpPath, "-B", commandB, "-F", commandF);

        try{
            Process p = pb.start();
            OutputStream out = p.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            String outStr = "\r\n";
            writer.write(outStr);
            writer.flush();
            writer.close();
            InputStream in = p.getInputStream();
            int length = in.read(buff);
            return Arrays.copyOfRange(buff,0, length);
        }catch (IOException e){
            System.out.println("PHP文件执行出错！");
            e.printStackTrace();
        }
        return null;
    }
}