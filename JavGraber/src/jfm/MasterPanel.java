package jfm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
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

import javparser.JavParser;
import javparser._91JavParser;
import javparser._JavsukiParser;
import javparser._MaddParser;

public class MasterPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JfmMain parentFrame;
	private JTable table;
	private JTableModel model;
	private JScrollPane scrollPane;
	private JTextField textfield;
	private JPanel subPanel;
	private JButton actionButton;
	private MasterPanel mySelf;
	private _MaddParser maddParser = new _MaddParser();	
	private _JavsukiParser javsukiParser = new _JavsukiParser();
	private _91JavParser _91javParser = new _91JavParser();
	private JavParser javParser = javsukiParser;	
	private ProcessListener pListener = new MaddProcessListener();	
	private int selectItem;
	public long startT;

	private int gSwitch = 0;
	
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
				GetinfoThread t1=new GetinfoThread(searchDepth, mySelf);
				t1.start();
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
		textfield.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				InputEvent.CTRL_DOWN_MASK), "key_switch");
		textfield.getActionMap().put("key_switch", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				switchProvider();
			}
		});
		textfield.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 
				java.awt.event.InputEvent.CTRL_DOWN_MASK), "key_ctrl_r");
		textfield.getActionMap().put("key_ctrl_r", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setFocusInList(selectItem);
			}
		});

		// Create table
		model = new JTableModel();
		table = new JTable(model);
		getDiffTime("PartA");

		/**/
		switchProvider();

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
				toggleItem();
				setFocusInList(selectItem);
			}
		});
		table.getActionMap().put("key_enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				getResult();
			}
		});
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 
				java.awt.event.InputEvent.CTRL_DOWN_MASK), "key_ctrl_e");
		table.getActionMap().put("key_ctrl_e", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setFocusInTextfield();
			}
		});
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				InputEvent.CTRL_DOWN_MASK), "key_switch");
		table.getActionMap().put("key_switch", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				switchProvider();
			}
		});
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 
				java.awt.event.InputEvent.CTRL_DOWN_MASK), "key_ctrl_r");
		table.getActionMap().put("key_ctrl_r", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setFocusInList(selectItem);
			}
		});
		
		//Mouse
		MouseAdapter mouseListener = new MouseAdapter (){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount()==2){
		        	toggleItem();
		        }
		    }
		};
		table.addMouseListener(mouseListener);
	}

	private void setFocusInList(int idx) {
		table.setCellSelectionEnabled(true);
		table.changeSelection(idx, 0, false, false);
		table.requestFocus();
	}
	
	private void setFocusInTextfield() {
		textfield.setText("");
		textfield.requestFocus();
	}

	private void toggleItem() {
		if (selectItem>javParser.length())
		{
			System.out.println("[Error]out of range");
			return;
		}
		//textfield.setText(MaddawParser.get(selectItem).id);
		boolean bCheckd = model.getCheckAt(selectItem);
		model.setValueAt(!bCheckd, selectItem, 0);
		model.fireTableDataChanged();
	}
	
	private void getResult() {
		String finalString="";
		for(int i=0; i<model.getRowCount(); i++) {
			if (model.getCheckAt(i)) {
				Vector<String> vDllink = javParser.get(i).dllink;
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

	public void doGrabAction(int inDepth) {
		try {
			javParser.setListener(pListener);
			javParser.doAction(inDepth);
			javParser.close();
		} catch (IOException e) {
			System.out.println("[Error]doGrabAction");
		}
		setPath();
		setFocusInList(selectItem);
	}
	
	public void setPath() {
		Vector<Object> vcTemp = new Vector<>();

		/*clear all*/
		model.clearAllRow();

		if (javParser.length()!=0) {
			/*Insert new row*/
			for (int i = 0; i <javParser.length() ; i++) {
				vcTemp = new Vector<Object>();
				String jDate = javParser.get(i).label;
				String jTitle = javParser.get(i).title;
				vcTemp.add(Boolean.FALSE);
				vcTemp.add(jDate);
				vcTemp.add(jTitle);			
				model.addRow(vcTemp);
			}
		} else {
			/*Insert empty row*/
			vcTemp.add(Boolean.FALSE);
			vcTemp.add("(Empty)");
			vcTemp.add("(Empty)");
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
	
	public void switchProvider() {
		gSwitch++;
		if ((gSwitch %3)==2) {
			javParser = maddParser;
		} else if ((gSwitch %3)==1){
			javParser = javsukiParser;
		} else {
			javParser = _91javParser;
		}
		textfield.setText("["+javParser.getName()+"]");
		setPath();
	}

	public void onEvent(int idx) {
		System.out.printf("onEvent, idx=%d\n", idx);
		selectItem = idx;
		parentFrame.onEvent(javParser.get(selectItem));
	}
	
	public class MaddProcessListener extends ProcessListener {
		@Override
		public void onEvent(int p1, int p2) {
			textfield.setText(String.format("Progress: %d tasks remained", p1));
		}
	}
}
