package me.spoop.spoopdroid.items;

/**
 * JSend response. Contains a status and message.
 */
public class JSendResponse {

    private String status;
    private JSendData data;

    private static class JSendData {
        String message;
    }

    public JSendResponse(String status, String message) {
        this.status = status;
        this.data = new JSendData();
        this.data.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return data.message;
    }

    public boolean succeeded() {
        return "success".equals(status);
    }

    public boolean failed() {
        return !succeeded();
    }

}
