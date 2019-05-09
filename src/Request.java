import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Request {
    private String requestType;
    private String uri;
    private String contentType;
    private final List<String> postDatas = new ArrayList<>();
    String postData = null;

    String getType(){ return requestType; }
    void setRequestType(String requestType){ this.requestType = requestType; }
    String getUri(){ return uri; }
    private void setUri(String uri){ this.uri = uri; }
    String getContentType(){ return contentType; }
    private void setContentType(String contentType){ this.contentType = contentType; }
    List<String> getPostDatas(){ return postDatas; }
    private void addPostDatas(String postData){ if(postData != null) this.postDatas.add(postData); }

    Request(InputStream inputStream) {
        String requestMessage = readRequestMessage(inputStream);
//        System.out.println(requestMessage);
        // 解析请求方式
        String type = parseType(requestMessage);
        setContentType(type);
        if(Objects.equals(type,"POST"))
            postData = parsePostBody(requestMessage);
        // 解析请求类型
        String uri = parseUri(requestMessage);
        setUri(uri);
        String contentType = "";
        String [] contentTypeArray = uri != null ? uri.split("\\.") : new String[0];
        if(uri == null || uri.indexOf('.') != -1) contentType = contentTypeArray[contentTypeArray.length-1];
        setContentType(contentType);
    }

    private String readRequestMessage(InputStream input) {
        StringBuilder requestMessage = new StringBuilder();
        int readLength;
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
        int index = requestString.indexOf(' ');
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

    private String parsePostBody(String requestString) {
        int index1, index2;
        String [] requestArray = requestString.split("\r\n\r\n");

        if(requestArray.length >= 2){
            String [] parArray = requestArray[1].split("&");
            for (String s : parArray) {
                addPostDatas(s);
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
