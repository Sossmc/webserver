import java.io.InputStream;
import java.util.Objects;

class RequestParser {
    public Request parse(InputStream inputStream) {
        Request request = new Request();
        // 读取请求信息
        String requestMessage = readRequestMessage(inputStream);
//        System.out.println(requestMessage);
        // 解析请求方式
        String type = parseType(requestMessage);
        request.setContentType(type);
        if(Objects.equals(type,"POST"))
            request.postData = parsePostBody(requestMessage, request);
        // 解析请求类型
        String uri = parseUri(requestMessage);
        request.setUri(uri);
        String contentType = "";
        String [] contentTypeArray = uri.split("\\.");
        if(uri.indexOf('.') != -1) contentType = contentTypeArray[contentTypeArray.length-1];
        request.setContentType(contentType);
        return request;
    }

    private String readRequestMessage(InputStream input) {
        StringBuffer requestMessage = new StringBuffer();
        int readLength = 0;
        byte[] buffer = new byte[1024];
        try {
            readLength = input.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            readLength = -1;
        }
        for(int i = 0; i < readLength; i++) {
            requestMessage.append((char) buffer[i]);
        }
        return requestMessage.toString();
    }

    private String parseType(String requestString) {
        int index = 0;
        index = requestString.indexOf(' ');
        if (index != -1) {
            return requestString.substring(0, index);
        }
        return null;
    }

    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1)
                return requestString.substring(index1 + 1, index2);
        }
        return null;
    }

    private String parsePostBody(String requestString, Request request) {
        int index1, index2;
        String [] requestArray = requestString.split("\r\n\r\n");

        if(requestArray.length >= 2){
            String [] parArray = requestArray[1].split("&");
            for(int i = 0; i < parArray.length; i++){
                request.addPostDatas(parArray[i]);
            }
            index1 = requestArray[1].indexOf('=');
            if(index1 != -1){
                index2 = requestArray[1].indexOf('&');
                if(index2 > index1) return requestArray[1].substring(index1 + 1, index2);
            }
        }
        return null;
    }

}
