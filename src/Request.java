import java.util.ArrayList;
import java.util.List;

class Request {
    private String requestType;
    private String uri;
    private String contentType;
    private List<String> postDatas = new ArrayList<String>();
    String postData = null;

    String getType(){ return requestType; }
    void setRequestType(String requestType){ this.requestType = requestType; }
    String getUri(){ return uri; }
    void setUri(String uri){ this.uri = uri; }
    String getContentType(){ return contentType; }
    void setContentType(String contentType){ this.contentType = contentType; }
    List<String> getPostDatas(){ return postDatas; }
    void addPostDatas(String postData){ if(postData != null) this.postDatas.add(postData); }
}
