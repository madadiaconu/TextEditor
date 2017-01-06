package gui;

import data_structures.Text;
import gui.event_handling.UpdateEvent;
import gui.event_handling.UpdateEventListener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Collections;

/**********************************************************************
*  gui.Viewer
**********************************************************************/

public class Viewer extends Canvas implements AdjustmentListener, UpdateEventListener {
	static final int    TOP = 5;    // top margin
	static final int    BOTTOM = 5; // bottom margin
	static final int    LEFT = 5;   // left margin
	static final int    EOF = '\0';
	static final String CRLF = "\r\n";

	Text text;
	Line firstLine = null; // the lines in this viewer
	int firstVisibleTextPos = 0;     // first text position in this viewer
	int lastVisibleTextPos;          // last text position in this viewer
	JScrollBar scrollBar;
	JComboBox<Font> fontDropDown;
	Selection selection = null;
	Position caret;
	Position lastMousePos;      // last mouse position: used during mouse dragging
	Graphics g;

	public Viewer(Text t, JScrollBar sb, JComboBox<Font> cb) {
		scrollBar = sb;
		scrollBar.addAdjustmentListener(this);
		scrollBar.setMaximum(t.length());
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
		fontDropDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (selection != null) {

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
				ch = text.charAt(--pos);
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
		Line line = firstLine, last = null;
		pos.setPosFirstCharInText(firstVisibleTextPos);
		while (line != null && textPos >= pos.getPosFirstCharInText() + line.getLen()) {
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() + line.getLen());
			last = line;
			line = line.getNext();
		}
		if (line == null) {
			pos.setX(last.getX() + last.getW());
			pos.setY(last.getBase());
			pos.setLine(last);
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() - last.getLen());
			pos.setPosInLine(last.getLen());
		} else {
			pos.setX(line.getX());
			pos.setY(line.getBase());
			pos.setLine(line);
			pos.setPosInLine(textPos - pos.getPosFirstCharInText());
			FontMetrics m = g.getFontMetrics();
			int i = pos.getPosFirstCharInText();
			while (i < textPos) {
				char ch = text.charAt(i); i++;
				pos.setX(pos.getX() + charWidth(m, ch));
			}
		}
		pos.setPosInText(pos.getPosFirstCharInText() + pos.getPosInLine());
		return pos;
	}

	private Position getPos(int x, int y) {
		Position pos = new Position();
		if (y >= getHeight() - BOTTOM) y = getHeight() - BOTTOM - 1;
		Line line = firstLine, last = null;
		pos.setPosFirstCharInText(firstVisibleTextPos);
		while (line != null && y >= line.getY() + line.getH()) {
			pos.setPosFirstCharInText(pos.getPosFirstCharInText() + line.getLen());
			last = line;
			line = line.getNext();
		}
		if (line == null) { line = last; pos.setPosFirstCharInText(pos.getPosFirstCharInText() - last.getLen()); }
		pos.setY(line.getBase());
		pos.setLine(line);
		if (x >= line.getX() + line.getW()) {
			pos.setX(line.getX() + line.getW());
			pos.setPosInLine(line.getLen());
			if (pos.getPosFirstCharInText() + line.getLen() < text.length()) pos.setPosInLine(pos.getPosInLine() - 2);
		} else {
			pos.setX(line.getX());
			FontMetrics m = g.getFontMetrics();
			int i = pos.getPosFirstCharInText();
			char ch = text.charAt(i);
			int w = charWidth(m, ch);
			while (x >= pos.getX() + w) {
				pos.setX(pos.getX() + w);
				i++; ch = text.charAt(i);
				w = charWidth(m, ch);
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
		Line line = beg.getLine();
		int x = beg.getX();
		int y = line.getY();
		int w;
		int h = line.getH();
		while (line != end.getLine()) {
			w = line.getW() + LEFT - x;
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
			scrollBar.setValues(firstVisibleTextPos, 0, 0, text.length());
		}
	}

	private void doKeyPressed(KeyEvent e) {
		if (caret != null) {
			int key = e.getKeyCode();
			int pos = caret.getPosInText();
			char ch;
			if (key == KeyEvent.VK_RIGHT) {
				pos++; ch = text.charAt(pos);
				if (ch == '\n') pos++;
				setCaret(pos);
			} else if (key == KeyEvent.VK_LEFT) {
				pos--; ch = text.charAt(pos);
				if (ch == '\n') pos--;
				setCaret(pos);
			} else if (key == KeyEvent.VK_UP) {
				setCaret(caret.getX(), caret.getY() - caret.getLine().getH());
			} else if (key == KeyEvent.VK_DOWN) {
				setCaret(caret.getX(), caret.getY() + caret.getLine().getH());
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

	private void drawString(Graphics g, String s, int x, int y) {
		String s1 = s.replaceAll("\t", "    ");
		g.drawString(s1, x, y);
	}

/*------------------------------------------------------------
*  line handling
*-----------------------------------------------------------*/

	private Line fill(int top, int bottom, int pos) {
		g = getGraphics();
		FontMetrics m = g.getFontMetrics();
		Line first = null, line = null;
		int y = top;
		lastVisibleTextPos = pos;
		char ch = text.charAt(pos);
		while (y < bottom) {
			if (first == null) {
				first = line = new Line();
			} else {
				Line prev = line;
				line.setNext(new Line()); line = line.getNext(); line.setPrev(prev);
			}
			StringBuffer buf = new StringBuffer();
			while (ch != '\n' && ch != EOF) {
				buf.append(ch);
				pos++;
				ch = text.charAt(pos);
			}
			boolean eol = ch == '\n';
			if (eol) {
			    buf.append(ch);
			    pos++;
			    ch = text.charAt(pos);
			}
			line.setLen(buf.length());
			line.setText(buf.toString());
			line.setX(LEFT);
			line.setY(y);
			line.setW(stringWidth(m, line.getText()));
			line.setH(m.getHeight());
			line.setBase(y + m.getAscent());
			y += line.getH();
			lastVisibleTextPos += line.getLen();
			if (!eol) break;
		}
		return first;
	}

	private void rebuildFrom(Position pos) {
		Line line = pos.getLine();
		Line prev = line.getPrev();
		line = fill(line.getY(), getHeight() - BOTTOM, pos.getPosFirstCharInText());
		if (prev == null) firstLine = line; else { prev.setNext(line); line.setPrev(prev); }
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
			if (e.getFrom() != caret.getPosInText()) pos = getPos(e.getFrom());
			int newCarPos = pos.getPosInText() + e.getText().length();
			if (e.getText().indexOf(CRLF) >= 0) {
				rebuildFrom(pos);
				if (pos.getY() + pos.getLine().getH() > getHeight() - BOTTOM)
					scrollBar.setValue(firstVisibleTextPos + firstLine.getLen());
			} else {
				b = new StringBuffer(pos.getLine().getText());
				b.insert(pos.getPosInLine(), e.getText());
				pos.getLine().setText(b.toString());
				pos.getLine().setW(stringWidth(m, e.getText()));
				pos.getLine().setLen(pos.getLine().getLen() + e.getText().length());
				lastVisibleTextPos += e.getText().length();
				repaint(pos.getLine().getX(), pos.getLine().getY(), getWidth(), pos.getLine().getH()+1);
			}
			setCaret(newCarPos);

		} else if (e.getText() == null) { // delete
			if (caret == null || e.getTo() != caret.getPosInText()) pos = getPos(e.getTo());
			int d = e.getTo() - e.getFrom();
			if (pos.getPosInLine() - d < 0) { // delete across lines
				rebuildFrom(getPos(e.getFrom()));
			} else { // delete within a line
				b = new StringBuffer(pos.getLine().getText());
				b.delete(pos.getPosInLine() - d, pos.getPosInLine());
				pos.getLine().setText(b.toString());
				pos.getLine().setW(stringWidth(m, pos.getLine().getText()));
				pos.getLine().setLen(pos.getLine().getLen() - d);
				lastVisibleTextPos -= d;
				repaint(pos.getLine().getX(), pos.getLine().getY(), getWidth(), pos.getLine().getH()+1);
			}
			setCaret(e.getFrom());
		}
	}

	public void paint(Graphics g) {
		this.g = g;
		if (firstLine == null) {
			firstLine = fill(TOP, getHeight() - BOTTOM, 0);
			caret = getPos(0);
		}
		Line line = firstLine;
		while (line != null) {
			drawString(g, line.getText(), line.getX(), line.getBase());
			line = line.getNext();
		}
		if (caret != null) invertCaret();
		if (selection != null) invertSelection(selection.getBeg(), selection.getEnd());
	}
}