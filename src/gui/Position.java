package gui;

/**
 * Created by Diaconu Madalina on 06.01.17.
 */
public class Position {
    private Line line; // line containing this position
    private int x, y;  // base line point corresponding to this position
    private int posInText;  // text position (relative to start of text)
    private int posFirstCharInText;   // origin (text position of first character in this line)
    private int posInLine;   // text offset from posFirstCharInText

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
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

    public int getPosInText() {
        return posInText;
    }

    public void setPosInText(int posInText) {
        this.posInText = posInText;
    }

    public int getPosFirstCharInText() {
        return posFirstCharInText;
    }

    public void setPosFirstCharInText(int posFirstCharInText) {
        this.posFirstCharInText = posFirstCharInText;
    }

    public int getPosInLine() {
        return posInLine;
    }

    public void setPosInLine(int posInLine) {
        this.posInLine = posInLine;
    }
}