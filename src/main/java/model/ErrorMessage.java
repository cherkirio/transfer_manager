package model;

/**
 * Created by kirio on 29.10.2018.
 */
public class ErrorMessage {
    private int code;
    private String text;

    public ErrorMessage() {
    }

    public ErrorMessage(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
