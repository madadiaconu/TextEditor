package data_structures;

import gui.event_handling.UpdateEvent;
import java.io.*;


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
            q.setNext(p.getNext()); p.setNext(q);
            p = q;
        }
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(scratch, true), "UTF-8"));
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.setLen(p.getLen()+1); len++;
        notify(new UpdateEvent(pos, pos, s));
    }

    private Piece split(int pos) {
        if (pos == 0) return firstPiece;
        //--- set p to piece containing pos
        Piece p = firstPiece;
        long len = p.getLen();
        while (pos > len && p.getNext() != null) {
            p = p.getNext();
            len = len + p.getLen();
        }
        //--- split piece p
        if (pos != len) {
            long len2 = len - pos;
            long len1 = p.getLen() - len2;
            p.setLen(len1);
            Piece q = new Piece(len2, p.getFile(), p.getFilePos() + len1);
            q.setNext(p.getNext());
            p.setNext(q);
        }
        return p;
    }

    private boolean isLastPieceOnScratchFile(Piece p) {
        return p.getNext() == null || p.getNext().getFile() != scratch;
    }

    public void delete (int from, int to) {
        Piece a = split(from);
        Piece b = split(to);
        a.setNext(b.getNext());
        notify(new UpdateEvent(from, to, null));
    }

    @Override
    public char charAt(int pos) {
        if (pos < 0 || pos >= len) return '\0';
        Piece p = getPieceForPosition(pos);
        InputStream in;
        try {
            in = new FileInputStream(p.getFile());
            Reader reader = new InputStreamReader(in, "UTF-8");
            int r, i=0;
            while ((r = reader.read()) != -1 && i<p.getFilePos()+pos) {
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
        while (p.getNext() != null && currentPos + p.getLen() < pos) {
            currentPos += p.getLen();
            p = p.getNext();
        }
        return p;
    }
}
