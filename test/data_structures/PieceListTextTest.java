package data_structures;

import org.junit.Test;

import java.awt.*;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mada on 06.01.17.
 */
public class PieceListTextTest {

    @Test
    public void testSimpleCharAt() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Line 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }

    @Test
    public void testInsertAtStart() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.insert(0, "Hello");
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("HelloLine 1\n");
    }

    @Test
    public void testInsertAtEnd() throws FileNotFoundException {
        String path = getFilePath("/test2");
        PieceListText text = new PieceListText(path);
        text.insert(4, "Hello");
        List<String> result = getLines(text);
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("01\n");
        assertThat(result.get(1)).isEqualTo("3Hello");
    }

    @Test
    public void testInsertAtTheMiddle() throws FileNotFoundException {
        String path = getFilePath("/test2");
        PieceListText text = new PieceListText(path);
        text.insert(2, "Hello");
        List<String> result = getLines(text);
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("01Hello\n");
        assertThat(result.get(1)).isEqualTo("3");
    }


    @Test
    public void testInsertTwice() throws FileNotFoundException {
        String path = getFilePath("/test2");
        PieceListText text = new PieceListText(path);
        text.insert(2, "Hello");
        text.insert(4, "zzz");
        List<String> result = getLines(text);
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("01Hezzzllo\n");
        assertThat(result.get(1)).isEqualTo("3");
    }

    @Test
    public void deleteStart() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.delete(0,2);
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("ne 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }


    @Test
    public void deleteMiddle() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.delete(2,4);
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Li 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }


    @Test
    public void deleteInsert() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.insert(1,"Hello");
        text.delete(3,6);
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("LHeine 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }


    @Test
    public void deleteAcrossPieces() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.insert(1,"Hello");
        text.delete(3,8);
        List<String> result = getLines(text);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("LHee 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }

    @Test
    public void updateFontForSelection() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        Font font = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getAllFonts()[0];
        text.updateFontForSelection(font, 1, 4);
        Piece firstPiece = text.getFirstPiece();
        Piece secondPiece = firstPiece.getNext();
        Piece thirdPiece = secondPiece.getNext();
        assertThat(firstPiece.getFont()).isEqualTo(null);
        assertThat(secondPiece.getFont()).isEqualTo(font);
        assertThat(thirdPiece.getFont()).isEqualTo(null);
    }

    @Test
    public void updateFontForSelectionAcrossPieces() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.insert(3,"Hello"); //at this point, there are already 3 pieces
        Font font = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getAllFonts()[0];
        text.updateFontForSelection(font, 1, 5); //at this point, we have 5 pieces
        Piece firstPiece = text.getFirstPiece();
        Piece secondPiece = firstPiece.getNext();
        Piece thirdPiece = secondPiece.getNext();
        assertThat(firstPiece.getFont()).isEqualTo(null);
        assertThat(secondPiece.getFont()).isEqualTo(font);
        assertThat(thirdPiece.getFont()).isEqualTo(font);
        assertThat(thirdPiece.getNext().getFont()).isEqualTo(null);
        assertThat(thirdPiece.getNext().getNext().getFont()).isEqualTo(null);
    }

    @Test
    public void cut() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.cut(1,3);
        List<String> result = getLines(text);
        assertThat(text.getClipboard()).isEqualTo("in");
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Le 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }


    @Test
    public void copy() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.copy(1,3);
        List<String> result = getLines(text);
        assertThat(text.getClipboard()).isEqualTo("in");
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Line 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Line 3\n");
    }


    @Test
    public void paste() throws FileNotFoundException {
        String path = getFilePath("/test1");
        PieceListText text = new PieceListText(path);
        text.cut(1,3);
        text.paste(7);
        List<String> result = getLines(text);
        assertThat(text.getClipboard()).isEqualTo("in");
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("Le 1\n");
        assertThat(result.get(1)).isEqualTo("\n");
        assertThat(result.get(2)).isEqualTo("Linine 3\n");
    }

    private List<String> getLines(PieceListText list) {
        List<String> result = new ArrayList<>();
        int pos = 0;
        while (true) {
            String line = getLine(list, pos);
            if (line.length() == 0) {
                return result;
            }
            result.add(line);
            pos += line.length();
        }
    }

    private String getLine(PieceListText list, int start) {
        StringBuilder sb = new StringBuilder();
        int pos = start;
        do {
            sb.append(list.charAt(pos));
            pos++;
        } while (!endsInLine(sb));
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\0') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private boolean endsInLine(StringBuilder sb) {
        return sb.length() != 0 && (sb.charAt(sb.length() - 1) == '\n' || sb.charAt(sb.length() - 1) == '\0');
    }

    private String getFilePath(String fileName) {
        URL url = this.getClass().getResource(fileName);
        return url.getFile().replace("%20", " ");
    }

}