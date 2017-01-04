import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Madalina Diaconu on 03.01.17.
 * Interface implemented by text data structures
 */
public abstract class Text {

    interface UpdateEventListener {
        void update(UpdateEvent e);
    }

    class UpdateEvent {  // [from..to[ was replaced by text
        int from;
        int to;
        String text;
        UpdateEvent(int a, int b, String t) { from = a; to = b; text = t; }
    }

    protected int len;     // number of characters in the text buffer

    public abstract void insert(int pos, String s);
    public abstract void delete(int from, int to);
    public abstract char charAt(int pos);

    public Text (String fn) {

    }

    public int length() {
        return len;
    }

    /*-------------------------------------------------------------------
**  notification of listeners
**-----------------------------------------------------------------*/
    ArrayList listeners = new ArrayList();

    public void addUpdateEventListener(UpdateEventListener listener) {
        listeners.add(listener);
    }

    public void removeUpdateEventListener(UpdateEventListener listener) {
        listeners.remove(listener);
    }

    protected void notify(UpdateEvent e) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            UpdateEventListener listener = (UpdateEventListener)iter.next();
            listener.update(e);
        }
    }
}
