package data_structures;

import gui.event_handling.UpdateEvent;

import java.awt.*;
import java.io.*;


/**
 * Created by Madalina Diaconu on 03.01.17.
 * Data structure used to manage text
 */
public class PieceListText extends Text {

    private Piece firstPiece;
    private File scratch;

    public PieceListText(String fn) throws FileNotFoundException {
        super(fn);
        scratch = new File("scratch_file");
        scratch.delete();
        FileInputStream s = new FileInputStream(fn);
        firstPiece = new Piece();
        File f = new File(fn);
        len = f.length();
        firstPiece.setNext(new Piece(len, new File(fn), 0));
    }

    public Piece getFirstPiece() {
        return firstPiece.getNext(); //skip the dummy piece at the beginning
    }

    public void insert (int pos, String s) {
        Piece p = split(pos); // split piece at pos
        if (!isLastPieceOnScratchFile(p)) {
            Piece q = new Piece(0, scratch, scratch.length());
            q.setNext(p.getNext()); p.setNext(q);
            p = q;
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(scratch, true), "UTF-8"))) {
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.setLen(p.getLen()+s.length()); len += s.length();
        notify(new UpdateEvent(pos, pos, s));
    }


    /**
     * @param pos insertion position
     * @return the piece before the insert
     */
    private Piece split(int pos) {
        Piece pieceContainingPos = getPieceForPosition(pos);
        long len = getLenUntilPiece(pieceContainingPos);
        if (pos != len) {
            long secondPieceLen = len - pos;
            long firstPieceLen = pieceContainingPos.getLen() - secondPieceLen;
            pieceContainingPos.setLen(firstPieceLen);
            Piece pieceAfterInsertion = new Piece(secondPieceLen, pieceContainingPos.getFile(), pieceContainingPos.getFilePos() + firstPieceLen);
            pieceAfterInsertion.setNext(pieceContainingPos.getNext());
            pieceContainingPos.setNext(pieceAfterInsertion);
        }
        return pieceContainingPos;
    }

    private boolean isLastPieceOnScratchFile(Piece p) {
        return p.getFile() != null && p.getFile().equals(scratch) && p.getFilePos()+p.getLen() == scratch.length();
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
        while (p.getLen() == 0) { //ignore dummy pieces
            p = p.getNext();
        }
        try (InputStream in = new FileInputStream(p.getFile())){
            Reader reader = new InputStreamReader(in, "UTF-8");
            int r;
            reader.skip(p.getFilePos()+(pos - (getLenUntilPiece(p) - p.getLen())));
            r =  reader.read();
            if (r == -1) {
                return '\0';
            }
            return (char) r;
        } catch (IOException e) {
            e.printStackTrace();
            return '\0';
        }
    }

    /**
     * @param pos the position contained by the returned piece
     * @return piece containing pos
     */
    private Piece getPieceForPosition (int pos) {
        Piece cur = firstPiece;
        long totalLenUpToCur = cur.getLen();
        while (pos >= totalLenUpToCur && cur.getNext() != null) {
            cur = cur.getNext();
            totalLenUpToCur = totalLenUpToCur + cur.getLen();
        }
        return cur;
    }

    /**
     * @param searchedForPiece
     * @return length up to the piece, including it
     */
    private long getLenUntilPiece (Piece searchedForPiece) {
        Piece currentPiece = firstPiece;
        long len = currentPiece.getLen();
        while (currentPiece != searchedForPiece) {
            currentPiece = currentPiece.getNext();
            len = len + currentPiece.getLen();
        }
        return len;
    }

    public void updateFontForSelection(Font font, int from, int to) {
        Piece currentPiece = split(from).getNext(); //get first piece of selection
        Piece lastPieceInSelection = split(to);
        currentPiece.setFont(font);
        if (currentPiece != lastPieceInSelection) {
            lastPieceInSelection.setFont(font);
            while (currentPiece.getNext() != lastPieceInSelection) {
                currentPiece.setFont(font);
                currentPiece = currentPiece.getNext();
            }
        }
    }

}
