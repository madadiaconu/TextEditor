package gui;

import data_structures.CharDescriptor;
import data_structures.Text;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public final class LineEx {
	private final Point startLocation;

	private final List<CharDescriptor> charDescriptors;

	private LineEx previous;
	private LineEx next;

	private int width = -1;
	private int height = -1;
	private int base = -1;
	private String text;

	private LineEx(Point startLocation, List<CharDescriptor> charDescriptors) {
		this.startLocation = startLocation;
		this.charDescriptors = charDescriptors;
	}

	public List<CharDescriptor> getCharDescriptors() {
		return charDescriptors;
	}


	public LineEx getPrevious() {
		return previous;
	}

	public void setPrevious(LineEx previous) {
		this.previous = previous;
	}

	public LineEx getNext() {
		return next;
	}

	public void setNext(LineEx next) {
		this.next = next;
	}

	public int getWidth(Graphics g) {
		if (width == -1) {
			width = charDescriptors.stream().mapToInt(desc -> {
				FontMetrics m = g.getFontMetrics(desc.getFont());
				return m.charWidth(desc.getChar());
			}).sum();
		}
		return width;
	}

	public int getHeight(Graphics g) {
		if (height == -1) {
			height = charDescriptors.stream().mapToInt(desc -> {
				FontMetrics m = g.getFontMetrics(desc.getFont());
				return m.getHeight();
			}).max().orElse(0);
		}
		return height;
	}

	public String getText() {
		if (text == null) {
			StringBuilder sb = new StringBuilder();
			for (CharDescriptor desc : charDescriptors) {
				sb.append(desc.getChar());
			}
			text = sb.toString();
		}
		return text;
	}

	public int getX() {
		return startLocation.x;
	}

	public int getY() {
		return startLocation.y;
	}

	public int getLengthInCharacters() {
		return charDescriptors.size();
	}

	public int getBase(Graphics g) {
		if (base == -1) {
			base = getY() +
					charDescriptors.stream().mapToInt(desc -> {
						FontMetrics m = g.getFontMetrics(desc.getFont());
						return m.getAscent();
					}).max().orElse(0);
		}
		return base;
	}

	public boolean isEof() {
		return charDescriptors.size() == 0;
	}

	public static LineEx parseLine(Text text, int startPos, Point startLocation) {
		List<CharDescriptor> charDescriptors = new ArrayList<>();
		int curPos = startPos;
		CharDescriptor desc;
		do {
			desc = text.charAt(curPos);
			if (desc.isEof()) {
				break;
			}
			charDescriptors.add(desc);
			curPos++;
		} while (!desc.isEol());
		return new LineEx(startLocation, charDescriptors);
	}

}
