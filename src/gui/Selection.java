package gui;

/**
 * Created by Diaconu Madalina on 06.01.17.
 */
public class Selection {
    private Position beg, end;

    public Selection (Position a, Position b) {
        beg = a; end = b;
    }

    public Position getBeg() {
        return beg;
    }

    public void setBeg(Position beg) {
        this.beg = beg;
    }

    public Position getEnd() {
        return end;
    }

    public void setEnd(Position end) {
        this.end = end;
    }
}