import javax.swing.text.Style;
import java.awt.*;
import java.io.*;

class Piece { // descriptor
    long len; // length of this piece
    File file; // file containing this piece
    long filePos; // offset from beginning of file
    Piece next;
    Font font;
    Style style;

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
}


/**
 * Created by Madalina Diaconu on 03.01.17.
 * Data structure used to manage text
 */
public class PieceListText extends Text {

    Piece firstPiece;
    File scratch;

    public PieceListText(String fn) {
        super(fn);
        scratch = new File("scratch_file");
        try {
            FileInputStream s = new FileInputStream(fn);
            len = s.available();
            firstPiece = new Piece(len, new File(fn),0);
        } catch (IOException e) {
            len = 0;
            firstPiece = new Piece();
        }
    }

    public void insert (int pos, String s) {
        Piece p = split(pos); // split piece at pos
        if (!isLastPieceOnScratchFile(p)) {
            Piece q = new Piece(0, scratch, scratch.length());
            q.next = p.next; p.next = q;
            p = q;
        }
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(scratch, true), "UTF-8"));
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.len++; len++;
        notify(new UpdateEvent(pos, pos, s));
    }

    private Piece split(int pos) {
        if (pos == 0) return firstPiece;
        //--- set p to piece containing pos
        Piece p = firstPiece;
        long len = p.len;
        while (pos > len && p.next != null) {
            p = p.next;
            len = len + p.len;
        }
        //--- split piece p
        if (pos != len) {
            long len2 = len - pos;
            long len1 = p.len - len2;
            p.len = len1;
            Piece q = new Piece(len2, p.file, p.filePos + len1);
            q.next = p.next;
            p.next = q;
        }
        return p;
    }

    private boolean isLastPieceOnScratchFile(Piece p) {
        return p.next == null || p.next.file != scratch;
    }

    public void delete (int from, int to) {
        Piece a = split(from);
        Piece b = split(to);
        a.next = b.next;
        notify(new UpdateEvent(from, to, null));
    }

    @Override
    public char charAt(int pos) {
        if (pos < 0 || pos >= len) return '\0';
        Piece p = getPieceForPosition(pos);
        InputStream in;
        try {
            in = new FileInputStream(p.file);
            Reader reader = new InputStreamReader(in, "UTF-8");
            int r, i=0;
            while ((r = reader.read()) != -1 && i<p.filePos+pos) {
                i++;
            }
            return (char) r;
        } catch (IOException e) {
            e.printStackTrace();
            return '\0';
        }
    }

    private Piece getPieceForPosition (int pos) {
        Piece p = firstPiece;
        int currentPos = 0;
        while (p.next != null && currentPos + p.len < pos) {
            currentPos += p.len;
            p = p.next;
        }
        return p;
    }
}
