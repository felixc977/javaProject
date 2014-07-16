package jfm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import javparser.JavEntry;
import javparser.MaddawParser;

public class SlavePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel iconLabel;
	private ImageIcon iImage;
	private String imgPath = null;

	private JTable TableInfo;
	private String[][] tableInfo = {{ "TITLE", "NULL" },
									{ "ID", "NULL" } ,
									{ "CAST", "NULL" } ,
									{ "DATA", "NULL" } ,
									{ "GENRE", "NULL" } };
	String[] tableCol = { "col1", "col2" };
	public enum TableEntry {
		title(0), id(1), cast(2), date(3), genre(4);
		
		private final int value;
	    private TableEntry(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	        return value;
	    }
	}

	SlavePanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.black));

		TableInfo = new JTable(tableInfo, tableCol);
		TableInfo.setBorder(BorderFactory.createLineBorder(Color.black));
		this.add(TableInfo, BorderLayout.SOUTH);

		init();
	}

	public void setInfo(int javId) {
		try {
			if (javId<=MaddawParser.length()) {
				JavEntry avEntry = null;
				avEntry = MaddawParser.get(javId);
				if (avEntry == null) {
					return;
				}				

				/* set table */
				String tmpStr = "";
				tableInfo[TableEntry.title.getValue()][1] = avEntry.title;
				tableInfo[TableEntry.id.getValue()][1] = avEntry.id;
				tableInfo[TableEntry.date.getValue()][1] = avEntry.date;
				
				tmpStr = "";
				for(String subStr: avEntry.genre) {
					tmpStr = tmpStr+subStr+" ";
				}
				tableInfo[TableEntry.genre.getValue()][1] = tmpStr;
				
				tmpStr = "";
				for(String subStr: avEntry.cast) {
					tmpStr = tmpStr+subStr+" ";
				}
				tableInfo[TableEntry.cast.getValue()][1] = tmpStr;
				
				/* set image */
				imgPath = avEntry.imgPath;
				changeImage();
			} else {
				System.out.println("setInfo: ParseResult.NotExist\n");
				setToNullState();
			}
		} catch (Exception e1) {
			System.out.println("[Error]setInfo: Exception\n");
		}
	}

	private void init() {
		changeImage();
		this.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				changeImage();
			}

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentShown(ComponentEvent e) {
			}
		});
	}
	
	public void setToWaitState() {
		System.out.printf("setToWaitState()\n");
		String tmpStr = "Loading";
		tableInfo[TableEntry.title.getValue()][1] = tmpStr;
		tableInfo[TableEntry.id.getValue()][1] = tmpStr;
		tableInfo[TableEntry.date.getValue()][1] = tmpStr;
		tableInfo[TableEntry.genre.getValue()][1] = tmpStr;
		tableInfo[TableEntry.cast.getValue()][1] = tmpStr;
		imgPath = "./img/loading.png";
		changeImage();
	}
	
	public void setToNullState() {
		System.out.printf("setToWaitState()\n");
		String tmpStr = "NULL";
		tableInfo[TableEntry.title.getValue()][1] = tmpStr;
		tableInfo[TableEntry.id.getValue()][1] = tmpStr;
		tableInfo[TableEntry.date.getValue()][1] = tmpStr;
		tableInfo[TableEntry.genre.getValue()][1] = tmpStr;
		tableInfo[TableEntry.cast.getValue()][1] = tmpStr;

		imgPath = "./img/directory_01.jpg";
		changeImage();
	}

	public void changeImage() {
		String path = imgPath;
		if (path != null) {
			System.out.println("change Image: " + path);
		} else {
			path = "./img/directory_01.jpg";
		}

		iImage = new ImageIcon(path);
		Image img = iImage.getImage();
		float scaleRatio = (float) this.getWidth() / (float) img.getWidth(null);
		int newWidth = this.getWidth();
		int newHeight = (int) (img.getHeight(null) * scaleRatio);
		if (newWidth == 0)
			newWidth = 300;
		if (newHeight == 0)
			newHeight = 300;

		Image simg = img.getScaledInstance(newWidth, newHeight,
				java.awt.Image.SCALE_SMOOTH);
		iImage = new ImageIcon(simg);

		if (iconLabel == null) {
			iconLabel = new JLabel("", iImage, JLabel.CENTER);
			this.add(iconLabel, BorderLayout.NORTH);
		} else {
			iconLabel.setIcon(iImage);
		}
		this.setVisible(true);
		this.repaint();
	}
}
