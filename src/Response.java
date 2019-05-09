import java.io.FileInputStream;
import java.io.OutputStream;

class Response {

    private OutputStream output;

    Response(OutputStream output) {
        this.output = output;
    }

    void writeText(byte[] text, String status, String type) {
        try {
            String line = "";
            line = "HTTP/1.1 " + status + " OK\n";
            output.write(line.getBytes());
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

            output.write(("Accept-Ranges: bytes\n").getBytes());
            output.write(("Content-Type: "+type+"; charset=UTF-8\n\n").getBytes());
            if(text != null) output.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{ ;}
    }
}
