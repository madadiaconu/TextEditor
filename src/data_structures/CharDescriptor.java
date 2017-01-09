package data_structures;

import java.awt.Font;

public class CharDescriptor {

	private final char ch;

	private final Font font;

	public CharDescriptor(char ch, Font font) {
		this.ch = ch;
		this.font = font;
	}

	public char getChar() {
		return ch;
	}

	public Font getFont() {
		return font;
	}

	public boolean isEol() {
		return ch == '\n';
	}

	public boolean isEof() {
		return ch == '\0';
	}
}
