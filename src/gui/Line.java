package gui;

/**
 * Created by Diaconu Madalina on 06.01.17.
 */
public class Line {
    private String text;    // text of this line
    private int len;        // length of this line (including CRLF)
    private int x, y, w, h; // top left corner, width, height
    private int base;       // base line
    private Line prev, next;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public Line getPrev() {
        return prev;
    }

    public void setPrev(Line prev) {
        this.prev = prev;
    }

    public Line getNext() {
        return next;
    }

    public void setNext(Line next) {
        this.next = next;
    }
}