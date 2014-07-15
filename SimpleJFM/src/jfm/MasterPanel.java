package jfm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MasterPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JfmMain parentFrame;
	private JTable table;
	private DefaultTableModel model;
	private JScrollPane scrollPane;
	private JTextField textfield;
	
	private File selectItem;
	private File dir;
	private File[] listOfFiles;
	private String dirPath;
	public long startT;

	MasterPanel(String inPath, JfmMain inFrame) {
		startT = System.currentTimeMillis();
		parentFrame = inFrame;
		dirPath = inPath;
		this.setLayout(new BorderLayout());
		init();
	}
	
	public void getDiffTime(String Name){
		//return;
		long currTime = System.currentTimeMillis();;
		System.out.println(Name+" Diff Time ="+(currTime - startT)+"ms");
		startT = currTime;
	}

	public void init() {
		textfield = new JTextField();
		this.add(textfield, BorderLayout.NORTH);
		textfield.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_enter");
		textfield.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("FUCK Enter "+textfield.getText());
				String tmpDirName = textfield.getText();
				File tmpDir = new File(tmpDirName);
				if (tmpDir.isDirectory()) {
					selectItem = tmpDir;
					setPath(selectItem.getAbsolutePath());
					DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
					tableModel.fireTableDataChanged();
					table.requestFocusInWindow();
				}else{
					textfield.setText(selectItem.getAbsolutePath());
				}
			}
		});

		// Create columns names
		String columnNames[] = { "Name", "Size" };
		String dataValues[][] = {};

		// Create table
		model = new DefaultTableModel(dataValues, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(model);
		getDiffTime("PartA");
		setPath(dirPath);

		// Add the table to a scrolling pane
		table.setDefaultRenderer(Object.class,new ColorTable());
		scrollPane = new JScrollPane(table);
		this.add(scrollPane, BorderLayout.WEST);

		SelectionListener listener = new SelectionListener(this);
		table.getSelectionModel().addListSelectionListener(listener);

		// key
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_enter");
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "key_enter");
		table.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				doExecute();
			}
		});

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "key_left");
		table.getActionMap().put("key_left", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (dir.getParent()!=null) {
					textfield.setText(selectItem.getAbsolutePath());
					setPath(dir.getParent());
					DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
					tableModel.fireTableDataChanged();
				}
			}
		});
		
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 
				java.awt.event.InputEvent.CTRL_DOWN_MASK), "key_ctrl_e");
		table.getActionMap().put("key_ctrl_e", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("FUCK Combine");
				textfield.requestFocusInWindow();
			}
		});
		
		//Mouse
		MouseAdapter mouseListener = new MouseAdapter (){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount()==2){
		        	doExecute();
		        }
		    }
		};
		table.addMouseListener(mouseListener);
		
		//table.getCellRenderer(0, 0);
	}
	
	private void doExecute() {
		if (selectItem==null)
		{
			System.out.println("reach the root\n");
			return;
		}
		textfield.setText(selectItem.getAbsolutePath());
		if (Utils.checkIsVideo(selectItem.getName())) {
			String videoPlayerPath = parentFrame.getVideoPlayer();
			String[] cmd = {videoPlayerPath, selectItem.getAbsolutePath() };
			RunThread t1 = new RunThread("run "+selectItem.getName(), cmd);
			t1.start();
		}else if (selectItem.isDirectory()) {
			setPath(selectItem.getAbsolutePath());
			DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
			tableModel.fireTableDataChanged();
		}
	}
	
	public class RunThread implements Runnable {
		String sName;
		String[] sCmd;
		Thread t;

		RunThread(String inName, String[] inCmd) {
			sName = inName;
			sCmd = inCmd;
			t = new Thread(this, sName);
		}
		
		public void start() {
			t.start();
		}

		public void run() {
			Process p;
			try {
				System.out.print("start Play\n");
				p = Runtime.getRuntime().exec(sCmd);
				p.waitFor();
				System.out.print("end Play\n");
			} catch (IOException | InterruptedException e1) {
				System.out.print("Runtime failed\n");
			}
		}
	}

	public void setPath(String inPath) {
		dirPath = inPath;

		/*Test start*/
//		Path root = Paths.get(inPath);
//		try {
//			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
//		         @Override
//		         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
//		             throws IOException
//		         {
//		             //Files.delete(file);
//		        	 System.out.println(file.getFileName());
//		             return FileVisitResult.CONTINUE;
//		         }
//		         @Override
//		         public FileVisitResult postVisitDirectory(Path dir, IOException e)
//		             throws IOException
//		         {
//		             if (e == null) {
//		                 //Files.delete(dir);
//		            	 System.out.println(dir.getFileName());
//		                 return FileVisitResult.CONTINUE;
//		             } else {
//		                 // directory iteration failed
//		                 throw e;
//		             }
//		         }
//		     });
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		/*Test end*/
		
		// Read FileSystem
		getDiffTime("Part0");
		dir = new File(dirPath);
		getDiffTime("Part0.5");
		listOfFiles = dir.listFiles();

		getDiffTime("Part1");
		for (int i = model.getRowCount(); i > 0; i--) {
			model.removeRow(i - 1);
		}

		Vector<String> vcTemp = new Vector<>();
		
		String fileName;
		vcTemp.add("..");
		vcTemp.add("");
		model.addRow(vcTemp);
		getDiffTime("Part2");
		for (int i = 0; i < listOfFiles.length; i++) {
			getDiffTime("Part2.1");
			vcTemp = new Vector<>();
			getDiffTime("Part2.2");
			fileName = listOfFiles[i].getName();
			getDiffTime("Part2.3");

			if (listOfFiles[i].isDirectory()) {
				vcTemp.add(fileName);
				vcTemp.add("");
				//model.insertRow(0,vcTemp);
			} else {
				// long fileSize = (listOfFiles[i].length() / 1024) / 1024;
				// double fileSizeF = (double) fileSize / (double) 1024;
				// String fileSizeS = String.format("%.2f", fileSizeF);
				getDiffTime("Part2.31");
				String fileSizeS = "";
				vcTemp.add(fileName);
				getDiffTime("Part2.32");
				vcTemp.add(String.valueOf(fileSizeS) + " GB");
				getDiffTime("Part2.33");
				//model.addRow(vcTemp);
				/*
				 * if (!Utils.checkIsVideo(fileName)) {
				 * System.out.print("Skip "+fileName+"\n"); continue; }
				 */
			}
			model.addRow(vcTemp);
			getDiffTime("Part2.5");
		}
		getDiffTime("Part3");

		model.fireTableDataChanged();
		getDiffTime("Part4");
	}
	
	public class ColorTable extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			boolean bIsDir = true;
			if (row>0){
				File tFile = listOfFiles[row-1];
				bIsDir = tFile.isDirectory();
			}
			
			if (bIsDir) {
				setOpaque(true);
				setBackground(Color.LIGHT_GRAY);
			} else {
				setOpaque(true);
				setBackground(Color.PINK);
			}
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, col);
		}
	}

	public void onEvent(int idx) {
		System.out.printf("onEvent, idx=%d\n", idx);
		//TableModel tmodel = table.getModel();
		//selectItem = (String) tmodel.getValueAt(idx, 0);
		if (idx>0) {
			selectItem = listOfFiles[idx-1];
			textfield.setText(selectItem.getAbsolutePath());
			System.out.printf("onEvent, selectItem=%s\n", selectItem.getName());
		}else{
			selectItem = dir.getParentFile();
			System.out.printf("onEvent, selectItem = parent\n");
		}

		parentFrame.onEvent(selectItem);
	}
}
