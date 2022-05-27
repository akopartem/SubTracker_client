package xyz.akopartem.subtracker;

public class Response {
    private final int code;
    private final String description;
    public Response(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
