package edu.rutgers.jamchamb.spooped.items;

/**
 * Created by James on 10/12/2014.
 */
public class JSendResponse {
    String status;
    String message;

    public JSendResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
