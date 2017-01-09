package gui;

import data_structures.CharDescriptor;
import data_structures.PieceListText;
import gui.event_handling.UpdateEvent;
import gui.event_handling.UpdateEventListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollBar;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.List;
/**********************************************************************
*  gui.Viewer
**********************************************************************/

public class Viewer extends Canvas implements AdjustmentListener, UpdateEventListener {
	static final int    TOP = 5;    // top margin
	static final int    BOTTOM = 5; // bottom margin
	static final int    LEFT = 5;   // left margin
	static final int    EOF = '\0';
	static final String CRLF = "\r\n";

	PieceListText text;
	LineEx firstLine = null; // the lines in this viewer
	int firstVisibleTextPos = 0;     // first text position in this viewer
	int lastVisibleTextPos;          // last text position in this viewer
	JScrollBar scrollBar;
	JComboBox<Font> fontDropDown;
	Selection selection = null;
	Position caret;
	Position lastMousePos;      // last mouse position: used during mouse dragging
	Graphics g;

	String prevSearch;
	List<Integer> prevSearchPos;

	public Viewer(PieceListText t, JScrollBar sb, JComboBox<Font> cb) {
        g = getGraphics();
		scrollBar = sb;
		scrollBar.addAdjustmentListener(this);
		scrollBar.setMaximum((int)t.length());
		scrollBar.setUnitIncrement(50);
		scrollBar.setBlockIncrement(500);
		fontDropDown = cb;
		fontDropDown.setModel(new DefaultComboBoxModel<Font>(GraphicsEnvironment.
                getLocalGraphicsEnvironment().getAllFonts()));
		fontDropDown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Font font = (Font) value;
                setText(font.getFontName());
                return this;
            }
        });
		fontDropDown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if (selection != null) {
					Font f = (Font) itemEvent.getItem();
					text.updateFontForSelection(f.deriveFont(12.0f),
							selection.getBeg().getPosInText(),
							selection.getEnd().getPosInText());
					rebuildFrom(selection.getBeg());
				}
			}
		});
		text = t;
		text.addUpdateEventListener(this);
		this.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) { doKeyTyped(e); }
			public void keyPressed(KeyEvent e) { doKeyPressed(e); }
		});
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { doMousePressed(e); }
			public void mouseReleased(MouseEvent e) { doMouseReleased(e); }
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) { doMouseDragged(e); }
		});
		// disable TAB as a focus traversal key
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
	}

/*------------------------------------------------------------
*  scrolling
*-----------------------------------------------------------*/

	public void adjustmentValueChanged(AdjustmentEvent e) {
		int pos = e.getValue();
		if (pos > 0) { // find start of line
			char ch;
			do {
				ch = text.charAt(--pos).getChar();
			} while (pos > 0 && ch != '\n');
			if (pos > 0)
				pos++;
		}
		if (pos != firstVisibleTextPos) { // scroll
			Position caret0 = caret;
			Selection sel0 = selection;
			removeSelection();
			removeCaret();
			firstVisibleTextPos = pos;
			firstLine = fill(TOP, getHeight() - BOTTOM, firstVisibleTextPos);
			repaint();
			if (caret0 != null) setCaret(caret0.getPosInText());
			if (sel0 != null) setSelection(sel0.getBeg().getPosInText(), sel0.getEnd().getPosInText());
		}
	}

/*------------------------------------------------------------
*  position handling
*-----------------------------------------------------------*/

	private Position getPos(int textPos) {
		if (textPos < firstVisibleTextPos) textPos = firstVisibleTextPos;
		if (textPos > lastVisibleTextPos) textPos = lastVisibleTextPos;
		Position pos = new Position();
		LineEx line = firstLine, last = null;
		pos.setPosFirstCharInText(firstVisibleTextPos);
		while (line != null && textPos >= pos.getPosFirstCharInText() + line.getLengthInCharacters()) {
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() + line.getLengthInCharacters());
			last = line;
			line = line.getNext();
		}
		Graphics g = getGraphics();
		if (line == null) {
			pos.setX(last.getX() + last.getWidth(g));
			pos.setY(last.getBase(g));
			pos.setLine(last);
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() - last.getLengthInCharacters());
			pos.setPosInLine(last.getLengthInCharacters());
		} else {
			pos.setX(line.getX());
			pos.setY(line.getBase(g));
			pos.setLine(line);
			pos.setPosInLine(textPos - pos.getPosFirstCharInText());
			FontMetrics fontMetrics = g.getFontMetrics();
			int i = pos.getPosFirstCharInText();
			while (i < textPos) {
				char ch = text.charAt(i).getChar(); i++;
				pos.setX(pos.getX() + charWidth(fontMetrics, ch));
			}
		}
		pos.setPosInText(pos.getPosFirstCharInText() + pos.getPosInLine());
		return pos;
	}

	private Position getPos(int x, int y) {
		Position pos = new Position();
		if (y >= getHeight() - BOTTOM) y = getHeight() - BOTTOM - 1;
		LineEx line = firstLine, last = null;
		pos.setPosFirstCharInText(firstVisibleTextPos);
		Graphics g = getGraphics();
		while (line != null && y >= line.getY() + line.getHeight(g)) {
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() + line.getLengthInCharacters());
			last = line;
			line = line.getNext();
		}
		if (line == null) { line = last; pos.setPosFirstCharInText(pos.getPosFirstCharInText() - last.getLengthInCharacters()); }
		pos.setY(line.getBase(g));
		pos.setLine(line);
		if (x >= line.getX() + line.getWidth(g)) {
			pos.setX(line.getX() + line.getWidth(g));
			pos.setPosInLine(line.getLengthInCharacters());
			if (pos.getPosFirstCharInText() + line.getLengthInCharacters() < text.length()) pos.setPosInLine(pos.getPosInLine() - 2);
		} else {
			pos.setX(line.getX());
			FontMetrics fontMetrics = g.getFontMetrics(getFontForPosition(pos.getPosInText()));
			int i = pos.getPosFirstCharInText();
			char ch = text.charAt(i).getChar();
			int w = charWidth(fontMetrics, ch);
			while (x >= pos.getX() + w) {
				pos.setX(pos.getX() + w);
				i++; ch = text.charAt(i).getChar();
				w = charWidth(fontMetrics, ch);
			}
			pos.setPosInLine(i - pos.getPosFirstCharInText());
		}
		pos.setPosInText(pos.getPosFirstCharInText() + pos.getPosInLine());
		return pos;
	}

/*------------------------------------------------------------
*  caret handling
*-----------------------------------------------------------*/

	private void invertCaret() {
		g = getGraphics();
		g.setXORMode(Color.WHITE);
		int x = caret.getX();
		int y = caret.getY();
		g.drawLine(x, y, x, y + 3); x++; y++;
		g.drawLine(x, y, x, y + 2); x++; y++;
		g.drawLine(x, y, x, y + 1); x++; y++;
		g.drawLine(x, y, x, y);
		g.setPaintMode();
	}

	private void setCaret(Position pos) {
		removeCaret(); removeSelection();
		caret = pos;
		invertCaret();
	}

	public void setCaret(int tpos) {
		if (tpos >= firstVisibleTextPos && tpos <= lastVisibleTextPos) {
			setCaret(getPos(tpos));
		} else caret = null;
	}

	public void setCaret(int x, int y) {
		setCaret(getPos(x, y));
	}

	public void removeCaret() {
		if (caret != null) invertCaret();
		caret = null;
	}

/*------------------------------------------------------------
*  selection handling
*-----------------------------------------------------------*/

	private void invertSelection(Position beg, Position end) {
		g = getGraphics();
		g.setXORMode(Color.WHITE);
		LineEx line = beg.getLine();
		int x = beg.getX();
		int y = line.getY();
		int w;
		int h = line.getHeight(g);
		while (line != end.getLine()) {
			w = line.getWidth(g) + LEFT - x;
			g.fillRect(x, y, w, h);
			line = line.getNext();
			x = line.getX(); y = line.getY();
		}
		w = end.getX() - x;
		g.fillRect(x, y, w, h);
		g.setPaintMode();
	}

	public void setSelection(int from, int to) {
		if (from < to) {
			removeCaret();
			Position beg = getPos(from);
			Position end = getPos(to);
			selection = new Selection(beg, end);
			invertSelection(beg, end);
		} else selection = null;
	}

	public void removeSelection() {
		if (selection != null) invertSelection(selection.getBeg(), selection.getEnd());
		selection = null;
	}

/*------------------------------------------------------------
*  keyboard handling
*-----------------------------------------------------------*/

	private void doKeyTyped(KeyEvent e) {
		boolean selection = this.selection != null;
		if (selection) {
			text.delete(this.selection.getBeg().getPosInText(), this.selection.getEnd().getPosInText());
			// selection is removed; caret is set at selection.beg.tpos
		}
		if (caret != null) {
			char ch = e.getKeyChar();
			if (ch == KeyEvent.VK_BACK_SPACE) {
				if (caret.getPosInText() > 0 && !selection) {
					int d = caret.getPosInLine() == 0 ? 2 : 1;
					text.delete(caret.getPosInText() - d, caret.getPosInText());
				}
			} else if (ch == KeyEvent.VK_ESCAPE) {
			} else if (ch == KeyEvent.VK_ENTER) {
				text.insert(caret.getPosInText(), CRLF);
			} else {
				text.insert(caret.getPosInText(), String.valueOf(ch));
			}
			scrollBar.setValues(firstVisibleTextPos, 0, 0, (int)text.length());
		}
	}

	private void doKeyPressed(KeyEvent e) {
		Graphics g = getGraphics();
		if (caret != null) {
			int key = e.getKeyCode();
			int pos = caret.getPosInText();
			char ch;
			if (key == KeyEvent.VK_RIGHT) {
				pos++; ch = text.charAt(pos).getChar();
				if (ch == '\n') pos++;
				setCaret(pos);
			} else if (key == KeyEvent.VK_LEFT) {
				pos--; ch = text.charAt(pos).getChar();
				if (ch == '\n') pos--;
				setCaret(pos);
			} else if (key == KeyEvent.VK_UP) {
				setCaret(caret.getX(), caret.getY() - caret.getLine().getHeight(g));
			} else if (key == KeyEvent.VK_DOWN) {
				setCaret(caret.getX(), caret.getY() + caret.getLine().getHeight(g));
			} else if (key == KeyEvent.VK_F1) {
			}
		}
	}

/*------------------------------------------------------------
*  mouse handling
*-----------------------------------------------------------*/

	private void doMousePressed(MouseEvent e) {
		removeCaret(); removeSelection();
		Position pos = getPos(e.getX(), e.getY());
		selection = new Selection(pos, pos);
		lastMousePos = pos;
	}

	private void doMouseDragged(MouseEvent e) {
		if (selection == null) return;
		Position pos = getPos(e.getX(), e.getY());
		if (pos.getPosInText() < selection.getBeg().getPosInText()) {
			if (lastMousePos.getPosInText() >= selection.getEnd().getPosInText()) {
				invertSelection(selection.getBeg(), lastMousePos);
				selection.setEnd(selection.getBeg());
			}
			invertSelection(pos, selection.getBeg());
			selection.setBeg(pos);
		} else if (pos.getPosInText() > selection.getEnd().getPosInText()) {
			if (lastMousePos.getPosInText() <= selection.getBeg().getPosInText()) {
				invertSelection(lastMousePos, selection.getEnd());
				selection.setBeg(selection.getEnd());
			}
			invertSelection(selection.getEnd(), pos);
			selection.setEnd(pos);
		} else if (pos.getPosInText() < lastMousePos.getPosInText()) { // beg <= pos <= end; clear pos..getEnd()
			invertSelection(pos, selection.getEnd());
			selection.setEnd(pos);
		} else if (lastMousePos.getPosInText() < pos.getPosInText()) { // beg <= pos <= end; clear beg..pos
			invertSelection(selection.getBeg(), pos);
			selection.setBeg(pos);
		}
		lastMousePos = pos;
	}

	private void doMouseReleased(MouseEvent e) {
		if (selection.getBeg().getPosInText() == selection.getEnd().getPosInText()) setCaret(selection.getBeg());
		lastMousePos = null;
	}

/*------------------------------------------------------------
*  TAB handling
*-----------------------------------------------------------*/

	private int charWidth(FontMetrics m, char ch) {
		if (ch == '\t') return 4 * m.charWidth(' '); else return m.charWidth(ch);
	}

	private int stringWidth(FontMetrics m, String s) {
		String s1 = s.replaceAll("\t", "    ");
		return m.stringWidth(s1);
	}

    /**
     * @param firstPosition relative to the text
     * @param lastPosition relative to the text
     * @param string the string
     * @return the string width
     */
	private int stringWidth(int firstPosition, int lastPosition, String string) {
        int width = 0;
        g = getGraphics();
        if (text != null) {
            for (int i = firstPosition, j=0; i < lastPosition; i++, j++) {
                FontMetrics fontMetrics = g.getFontMetrics(getFontForPosition(i));
                width += fontMetrics.charWidth(string.charAt(j));
            }
            return width;
        }
        return g.getFontMetrics().stringWidth(string);
    }

	private void drawString(Graphics g, String s, int x, int y) {
		String s1 = s.replaceAll("\t", "    ");
		g.drawString(s1, x, y);
	}

	private Font getFontForPosition (int pos) {
	    Font font = text.getFontForPosition(pos);
	    if (font != null) {
	        return font;
        }
        g = getGraphics();
        return g.getFontMetrics().getFont();
    }

/*------------------------------------------------------------
*  line handling
*-----------------------------------------------------------*/

	private LineEx fill(int top, int bottom, int pos) {
		int y = top;
		lastVisibleTextPos = pos;
		LineEx first = null;
		LineEx previousLine = null;
		g = getGraphics();
		while (y < bottom) {
			LineEx line = LineEx.parseLine(text, pos, new Point(LEFT, y));
			if (line.isEof()) {
				break;
			}
			if (first == null) {
				first = line;
			} else {
				previousLine.setNext(line);
				line.setPrevious(previousLine);
			}
			y += line.getHeight(g);
			pos += line.getLengthInCharacters();
			lastVisibleTextPos += line.getLengthInCharacters();
			previousLine = line;
		}
		return first;
	}

	private void rebuildFrom(Position pos) {
		LineEx line = pos.getLine();
		LineEx prev = line.getPrevious();
		line = fill(line.getY(), getHeight() - BOTTOM, pos.getPosFirstCharInText());
		if (prev == null)
			firstLine = line;
		else {
			prev.setNext(line);
			line.setPrevious(prev);
		}
		repaint(LEFT, line.getY(), getWidth(), getHeight());
	}

/*------------------------------------------------------------
*  text drawing
*-----------------------------------------------------------*/

	public void update(UpdateEvent e) {
		StringBuffer b;
		g = getGraphics();
		FontMetrics m = g.getFontMetrics();
		Position pos = caret;

		if (e.getFrom() == e.getTo()) { // insert
			handleInsert(e, pos, m);
		} else if (e.getText() == null) { // delete
			rebuildFrom(getPos(e.getFrom()));
			setCaret(e.getFrom());
		}
	}

	private void handleInsert(UpdateEvent e, Position pos, FontMetrics m) {
		if (e.getFrom() != caret.getPosInText())
			pos = getPos(e.getFrom());
		int newCarPos = pos.getPosInText() + e.getText().length();
		rebuildFrom(pos);
		Graphics g = getGraphics();
		if (pos.getY() + pos.getLine().getHeight(g) > getHeight() - BOTTOM)
			scrollBar.setValue(firstVisibleTextPos + firstLine.getLengthInCharacters());
		setCaret(newCarPos);
	}

	public void paint(Graphics g) {
		this.g = g;
		if (firstLine == null) {
			firstLine = fill(TOP, getHeight() - BOTTOM, 0);
			caret = getPos(0);
		}
		LineEx line = firstLine;
		while (line != null) {
			drawLine(line);
			line = line.getNext();
		}
		if (caret != null) invertCaret();
		if (selection != null) invertSelection(selection.getBeg(), selection.getEnd());
	}

	private void drawLine(LineEx line) {
		Graphics g = getGraphics();
		int x = line.getX();
		int y = line.getBase(g);
		for (CharDescriptor desc : line.getCharDescriptors()) {
			String s = String.valueOf(desc.getChar()).replaceAll("\t", "    ");
			Font f = desc.getFont();
			g.setFont(f);
			g.drawString(s, x, y);
			x += g.getFontMetrics().charWidth(desc.getChar());
		}
	}

	public void cut() {
		if (selection != null) {
			text.cut(selection.getBeg().getPosInText(), selection.getEnd().getPosInText());
		}
	}

	public void copy() {
		if (selection != null) {
			text.copy(selection.getBeg().getPosInText(), selection.getEnd().getPosInText());
		}
	}

	public void paste() {
		text.paste(caret.getPosInText());
	}

	public void find(String term) {
	    resetSelection();
	    prevSearch = term;
	    List<Integer> positions = text.find(term);
        prevSearchPos = positions;
        for (Integer pos: positions) {
	        setSelection(pos,pos+term.length());
        }
    }

    private void resetSelection() {
	    if (prevSearchPos != null) {
            for (Integer pos : prevSearchPos) {
                setSelection(pos, pos + prevSearch.length());
            }
        }
    }
}