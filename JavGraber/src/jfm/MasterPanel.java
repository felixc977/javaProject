package jfm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.json.simple.parser.ParseException;

import javparser.MaddawParser;

public class MasterPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JfmMain parentFrame;
	private JTable table;
	//private DefaultTableModel model;
	private JTableModel model;
	private JScrollPane scrollPane;
	private JTextField textfield;
	
	private int selectItem;
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
		try {
			MaddawParser.init();
		} catch (IOException |ParseException e1) {
			e1.printStackTrace();
		}

		textfield = new JTextField();
		this.add(textfield, BorderLayout.NORTH);
		textfield.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_enter");
		textfield.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("FUCK Enter "+textfield.getText());
//				String tmpDirName = textfield.getText();
//				File tmpDir = new File(tmpDirName);
//				if (tmpDir.isDirectory()) {
//					selectItem = tmpDir;
//					setPath(selectItem.getAbsolutePath());
//					DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
//					tableModel.fireTableDataChanged();
//					table.requestFocusInWindow();
//				}else{
//					textfield.setText(selectItem.getAbsolutePath());
//				}
			}
		});

		// Create table
		model = new JTableModel();
		table = new JTable(model);
		getDiffTime("PartA");
		setPath(dirPath);

		// Add the table to a scrolling pane
		table.setDefaultRenderer(Object.class,new ColorTable());

		/*set column size*/
		table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		TableColumn columnA = table.getColumnModel().getColumn(0);
        columnA.setMinWidth(30);
        columnA.setMaxWidth(30);
        columnA = table.getColumnModel().getColumn(1);
        columnA.setMinWidth(80);
        columnA.setMaxWidth(80);

		scrollPane = new JScrollPane(table);
		this.add(scrollPane, BorderLayout.WEST);

		SelectionListener listener = new SelectionListener(this);
		table.getSelectionModel().addListSelectionListener(listener);

		// key
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_enter");
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "key_space");
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "key_space");
		table.getActionMap().put("key_space", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doExecute();
				table.setCellSelectionEnabled(true);
				table.changeSelection(selectItem, 0, false, false);
				table.requestFocus();
			}
		});
		table.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				doFinish();
			}
		});

		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "key_left");
		table.getActionMap().put("key_left", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
//				if (dir.getParent()!=null) {
//					textfield.setText(selectItem.getAbsolutePath());
//					setPath(dir.getParent());
//					DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
//					tableModel.fireTableDataChanged();
//				}
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
	}
	
	private void doExecute() {
		if (selectItem>MaddawParser.length())
		{
			System.out.println("[Error]out of range");
			return;
		}
		textfield.setText(MaddawParser.get(selectItem).title);
		boolean bCheckd = model.getCheckAt(selectItem);
		model.setValueAt(!bCheckd, selectItem, 0);
		model.fireTableDataChanged();
	}
	
	private void doFinish() {
		String finalString="";
		for(int i=0; i<model.getRowCount(); i++) {
			if (model.getCheckAt(i)) {
				Vector<String> vDllink = MaddawParser.get(i).dllink;
				for (int j=0;j<vDllink.size();j++) {
					finalString = finalString+vDllink.get(j)+"\n";
				}				
			}
		}
		System.out.println(finalString);
		
		StringSelection stringSelection = new StringSelection (finalString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
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
		
		for (int i = model.getRowCount(); i > 0; i--) {
			model.removeRow(i - 1);
		}

		Vector<Object> vcTemp = new Vector<>();
		
		for (int i = 0; i <MaddawParser.length() ; i++) {
			vcTemp = new Vector<Object>();
			String jId = MaddawParser.get(i).id;
			String jTitle = MaddawParser.get(i).title;
			vcTemp.add(Boolean.FALSE);
			vcTemp.add(jId);
			vcTemp.add(jTitle);			
			model.addRow(vcTemp);
			//System.out.println(jTitle);
		}
		model.fireTableDataChanged();
	}
	
	public class ColorTable extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			boolean bIsDir = ((row%2)==1);
			
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
		selectItem = idx;
		parentFrame.onEvent(idx);
	}
}
