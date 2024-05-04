import java.io.*;

public class ChatClient implements Serializable {

    protected static final long serialVersionUID = 1112122200L;
    static final int MESSAGE = 0, LOGOUT = 1;
    private int type;
    private String message;

    ChatClient(int type, String message) {
        this.type = type;
        this.message = message;
    }
    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
}