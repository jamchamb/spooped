package me.spoop.spoopdroid.items;

/**
 * JSend response. Contains a status and message.
 */
public class JSendResponse {

    private String status;
    private String message;

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

    public boolean succeeded() {
        return "success".equals(status);
    }

    public boolean failed() {
        return !succeeded();
    }

}
