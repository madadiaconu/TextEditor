package data_structures;

import gui.event_handling.UpdateEvent;

import javax.swing.JLabel;
import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Madalina Diaconu on 03.01.17.
 * Data structure used to manage text
 */
public class PieceListText extends Text {
    private static final Font DEFAULT_FONT = new JLabel().getFont();
    private static final CharDescriptor EOF_DESCRIPTOR = new CharDescriptor('\0', DEFAULT_FONT);

    private Piece firstPiece;
    private File scratch;
    private String clipboard;

    public PieceListText(String fn) throws FileNotFoundException {
        scratch = new File("scratch_file");
        scratch.delete();
        FileInputStream s = new FileInputStream(fn);
        firstPiece = new Piece();
        File f = new File(fn);
        len = f.length();
        firstPiece.setNext(new Piece(len, new File(fn), 0));
    }

    public PieceListText(File file) {
        scratch = new File("scratch_file");
        scratch.delete();
        firstPiece = new Piece();
        len = file.length();
        firstPiece.setNext(new Piece(len, file, 0));
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
    public CharDescriptor charAt(int pos) {
        if (pos < 0 || pos >= len) return EOF_DESCRIPTOR;
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
                return EOF_DESCRIPTOR;
            }
            Font font = p.getFont();
            if (font == null) {
            	font = DEFAULT_FONT;
            }
            return new CharDescriptor((char)r, font);
        } catch (IOException e) {
            e.printStackTrace();
            return EOF_DESCRIPTOR;
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

    public Font getFontForPosition (int pos) {
        return getPieceForPosition(pos).getFont();
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

    public void saveToFile() {
        String fileContent = getAllText();
        File fileToBeSaved = new File("file"+System.currentTimeMillis());
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileToBeSaved, true), "UTF-8"))) {
            writer.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAllText() {
        Piece currentPiece = firstPiece;
        StringBuilder content = new StringBuilder();
        while (currentPiece != null) {
            content.append(getTextInPiece(currentPiece));
            currentPiece = currentPiece.getNext();
        }
        return content.toString();
    }

    private String getTextInPiece (Piece piece) {
        if (piece.getFile() != null) {
            try (InputStream in = new FileInputStream(piece.getFile())) {
                Reader reader = new InputStreamReader(in, "UTF-8");
                int r;
                reader.skip(piece.getFilePos());
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < piece.getLen(); i++) {
                    stringBuilder.append((char)reader.read());
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    public String getText (int from, int to) {
        Piece currentPiece = split(from).getNext(); //get first piece of selection
        Piece lastPieceInSelection = split(to);
        StringBuilder text = new StringBuilder();
        text.append(getTextInPiece(currentPiece));
        if (currentPiece != lastPieceInSelection) {
            while (currentPiece.getNext() != lastPieceInSelection) {
                currentPiece = currentPiece.getNext();
                text.append(getTextInPiece(currentPiece));
            }
        }
        return text.toString();
    }

    public void copy(int from, int to) {
        clipboard = getText(from, to);
    }

    public void cut(int from, int to) {
        clipboard = getText(from, to);
        delete(from, to);
    }

    public void paste(int to) {
        insert(to, clipboard);
    }

    public String getClipboard() {
        return clipboard;
    }

    public List<Integer> find (String term) {
        List<Integer> positions = new ArrayList<>();
        String content = getAllText();
        int pos = content.indexOf(term);
        int totalPos = pos; //position with respect to the original text
        while (pos != -1) {
            positions.add(totalPos);
            pos += term.length(); //skip the term we just found
            totalPos += term.length();
            content = content.substring(pos);
            pos = content.indexOf(term); //keep adding to pos because we want the position in the original text
            totalPos += pos;
        }
        return positions;
    }
}
