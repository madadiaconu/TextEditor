import data_structures.PieceListText;
import gui.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Editor {

	public static void main(String[] arg) throws FileNotFoundException {
		if (arg.length < 1) {
			System.out.println("-- file name missing");
			return;
		}
		String path = arg[0];
		try {
			FileInputStream s = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			System.out.println("-- file " + path + " not found");
			return;
		}

		JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 0);
		JComboBox<Font> comboBox = new JComboBox<>();
		Viewer viewer = new Viewer(new PieceListText(path), scrollBar, comboBox);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add("North",comboBox);
		panel.add("Center", viewer);
		panel.add("East", scrollBar);

		JFrame frame = new JFrame(path);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setSize(500, 600);
		frame.setResizable(false);
		frame.setContentPane(panel);
		frame.setVisible(true);
		frame.getContentPane().repaint();
	}

}
