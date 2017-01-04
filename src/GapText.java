import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/*******************************************************************
*   Gap text
*******************************************************************/

public class GapText extends Text{
	char[] buf;  // text buffer
	int bufLen;  // max. capacity of the text buffer
	int gapPos;  // position of the gap

	public GapText(String fn) {
		super(fn);
		try {
			FileInputStream s = new FileInputStream(fn);
			InputStreamReader r = new InputStreamReader(s);
			len = s.available();
			bufLen = 2 * len;
			buf = new char[bufLen];
			r.read(buf, 0, len);
			r.close(); s.close();
			gapPos = len;
		} catch (IOException e) {
			len = 0;
			bufLen = 0;
			buf = new char[0];
		}
	}

	private void grow() { // enlarge the text buffer to twice its size
		int newBufLen = 2 * bufLen;
		char[] a = new char[newBufLen];
		System.arraycopy(buf, 0, a, 0, gapPos);
		int n = len - gapPos;
		System.arraycopy(buf, bufLen - n, a, newBufLen - n, n);
		buf = a;
		bufLen = newBufLen;
	}

	private void moveGap(int pos) {
		int gapEnd = gapPos + (bufLen - len);
		if (pos < gapPos) {
			int len = gapPos - pos;
			System.arraycopy(buf, pos, buf, gapEnd - len, len);
		} else { // pos > gapPos
			int len = pos - gapPos;
			System.arraycopy(buf, gapEnd, buf, gapPos, len);
		}
		gapPos = pos;
	}

	private int adjust(int pos) {
		if (pos < 0) return 0;
		else if (pos > len) return len;
		else return pos;
	}

	public char charAt(int pos) {
		if (pos < 0 || pos >= len) return '\0';
		if (pos < gapPos) return buf[pos];
		else return buf[pos + (bufLen-len)];
	}

	public void insert(int pos, String s) {
		pos = adjust(pos);
		while (len + s.length() >= bufLen) grow();
		if (pos != gapPos) moveGap(pos);
		for (int i = 0; i < s.length(); i++) {
			buf[pos + i] = s.charAt(i);
			gapPos++; len++;
		}
		notify(new UpdateEvent(pos, pos, s));
	}

	public void delete(int from, int to) {
		from = adjust(from);
		to = adjust(to);
		if (from < to) {
			if (to != gapPos) moveGap(to);
			gapPos = from; len -= (to-from);
		}
		notify(new UpdateEvent(from, to, null));
	}
}