package gui.event_handling;

/**
 * Created by Diaconu Madalina on 06.01.17.
 */
public class UpdateEvent {  // [from..to[ was replaced by text
    private int from;
    private int to;
    private String text;

    public UpdateEvent(int a, int b, String t) {
        from = a;
        to = b;
        text = t;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getText() {
        return text;
    }
}