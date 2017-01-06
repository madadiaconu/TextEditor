package data_structures;

import gui.event_handling.UpdateEvent;
import gui.event_handling.UpdateEventListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Madalina Diaconu on 03.01.17.
 * Interface implemented by text data structures
 */
public abstract class Text {

    protected long len;     // number of characters in the text buffer

    public abstract void insert(int pos, String s);
    public abstract void delete(int from, int to);
    public abstract char charAt(int pos);

    public Text (String fn) {

    }

    public long length() {
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

    public void notify(UpdateEvent e) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            UpdateEventListener listener = (UpdateEventListener)iter.next();
            listener.update(e);
        }
    }
}
