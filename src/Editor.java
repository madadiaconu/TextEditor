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
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(fileMenu);
		menuBar.add(editMenu);

		JMenuItem openAction = new JMenuItem("Open");
		JMenuItem saveAction = new JMenuItem("Save");
        fileMenu.add(openAction);
        fileMenu.add(saveAction);

        JMenuItem cutAction = new JMenuItem("Cut");
        JMenuItem copyAction = new JMenuItem("Copy");
        JMenuItem pasteAction = new JMenuItem("Paste");
        JMenuItem findAction = new JMenuItem("Find");
        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(findAction);

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
				text.saveToFile();
			}
		});

		cutAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                viewer.cut();
            }
        });

		copyAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                viewer.copy();
            }
        });

		pasteAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                viewer.paste();
            }
        });

		findAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JTextField textField = new JTextField();
                JButton searchButton = new JButton();
                searchButton.setText("Search");
                searchButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        viewer.find(textField.getText());
                    }
                });

                JPanel panel = new JPanel(new BorderLayout());
                panel.add("North",textField);
                panel.add("Center", searchButton);

                JFrame frame = new JFrame("Find");
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        frame.setVisible(false);
                    }
                });
                frame.setSize(150, 50);
                frame.setResizable(false);
                frame.setContentPane(panel);
                frame.setVisible(true);
                frame.getContentPane().repaint();
            }
        });

		return frame;
	}

}
