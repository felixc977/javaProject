package jfm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
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
	private JPanel subPanel;
	private JButton actionButton;
	private MasterPanel mySelf;
	
	private ProcessListener pListener = new MaddProcessListener();	
	private int selectItem;
	public long startT;
	
	MasterPanel(String inPath, JfmMain inFrame) {
		startT = System.currentTimeMillis();
		parentFrame = inFrame;
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
		
		mySelf = this;

		subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		this.add(subPanel, BorderLayout.NORTH);
		actionButton = new JButton("Update");
		subPanel.add(actionButton, BorderLayout.EAST);
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputText = textfield.getText();
				int searchDepth = 10;
				try {
					searchDepth = Integer.parseInt(inputText);
				}catch (NumberFormatException exception) {
					System.out.println("parse textfield failed");
				}				
				GetinfoThread t1=new GetinfoThread(searchDepth, mySelf, pListener);
				t1.start();
				setFocus(selectItem);
			}
		});
		textfield = new JTextField();
		subPanel.add(textfield, BorderLayout.CENTER);
		textfield.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_enter");
		textfield.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("FUCK Enter "+textfield.getText());
			}
		});

		// Create table
		model = new JTableModel();
		table = new JTable(model);
		getDiffTime("PartA");
		setPath();

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
				setFocus(selectItem);
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

	private void setFocus(int idx) {
		table.setCellSelectionEnabled(true);
		table.changeSelection(selectItem, 0, false, false);
		table.requestFocus();
	}

	private void doExecute() {
		if (selectItem>MaddawParser.length())
		{
			System.out.println("[Error]out of range");
			return;
		}
		//textfield.setText(MaddawParser.get(selectItem).id);
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

	public void setPath() {
		/*clear all*/
		model.clearAllRow();

		/*Insert new row*/
		Vector<Object> vcTemp = new Vector<>();
		for (int i = 0; i <MaddawParser.length() ; i++) {
			vcTemp = new Vector<Object>();
			String jDate = MaddawParser.get(i).date;
			String jTitle = MaddawParser.get(i).title;
			vcTemp.add(Boolean.FALSE);
			vcTemp.add(jDate);
			vcTemp.add(jTitle);			
			model.addRow(vcTemp);
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
	
	public class MaddProcessListener extends ProcessListener {
		@Override
		public void onEvent(int p1, int p2) {
			textfield.setText(String.format("Progress: %d tasks remained", p1));
		}
	}
}
