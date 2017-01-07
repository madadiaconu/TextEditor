import data_structures.PieceListText;
import gui.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Editor {

	private static int numberWindows = 0;

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

		JFrame frame = createGui(path, new PieceListText(path));
		frame.setVisible(true);
		numberWindows++;
		frame.getContentPane().repaint();
	}

	private static JFrame createGui(String title, PieceListText text) {
		JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 0);
		JComboBox<Font> comboBox = new JComboBox<>();
		Viewer viewer = new Viewer(text, scrollBar, comboBox);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add("North",comboBox);
		panel.add("Center", viewer);
		panel.add("East", scrollBar);

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem openAction = new JMenuItem("Open");
		JMenuItem saveAction = new JMenuItem("Save");
		fileMenu.add(openAction);
		fileMenu.add(saveAction);

		JFrame frame = new JFrame(title);
		frame.setJMenuBar(menuBar);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				numberWindows--;
				if (numberWindows == 0) {
					System.exit(0);
				} else {
					frame.setVisible(false);
				}
			}
		});
		frame.setSize(500, 600);
		frame.setResizable(false);
		frame.setContentPane(panel);

		openAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fileChooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					JFrame frame = createGui(file.getName(), new PieceListText(file));
					frame.setVisible(true);
					numberWindows++;
					frame.getContentPane().repaint();
				}
			}
		});

		saveAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

			}
		});

		return frame;
	}

}
