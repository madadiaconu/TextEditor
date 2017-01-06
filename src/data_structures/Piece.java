package data_structures;

import javax.swing.text.Style;
import java.awt.*;
import java.io.File;

/**
 * Created by mada on 06.01.17.
 */
public class Piece { // descriptor
    private long len; // length of this piece
    private File file; // file containing this piece
    private long filePos; // offset from beginning of file
    private Piece next;
    private Font font;
    private Style style;

    public Piece() {
        this.len = 0;
        this.file = null;
        this.filePos = 0;
        this.next = null;
    }

    public Piece (long len, File file, long filePos) {
        this.len = len;
        this.file = file;
        this.filePos = filePos;
        this.next = null;
    }

    public Piece (long len, File file, long filePos, Font font, Style style) {
        this(len, file, filePos);
        this.font = font;
        this.style = style;
    }

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getFilePos() {
        return filePos;
    }

    public void setFilePos(long filePos) {
        this.filePos = filePos;
    }

    public Piece getNext() {
        return next;
    }

    public void setNext(Piece next) {
        this.next = next;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }
}
