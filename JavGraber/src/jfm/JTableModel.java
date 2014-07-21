package jfm;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

class JTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();
	String columnNames[] = { "_", "Date", "TITLE" };

	JTableModel(){
		;
	}
	
	public void clearAllRow() {
		for (int i = getRowCount(); i > 0; i--) {
			removeRow(i - 1);
		}
	}
	
	public void removeRow(int row) {
		rowData.remove(row);
	}
	
	public void addRow(Vector<Object> inData) {
		rowData.insertElementAt(inData, getRowCount());
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return rowData.size();
	}

	public Object getValueAt(int row, int column) {
		Vector<Object> tmpV = rowData.get(row);
		return tmpV.get(column);
	}
	
	public boolean getCheckAt(int row) {
		Vector<Object> tmpV = rowData.get(row);
		return (boolean)tmpV.get(0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int column) {
		return (getValueAt(0, column).getClass());
	}

	public void setValueAt(Object value, int row, int column) {
		Vector<Object> tmpV = rowData.get(row);
		tmpV.setElementAt(value, column);
		rowData.setElementAt(tmpV, row);
	}

	public boolean isCellEditable(int row, int column) {
		return true;//(column == 0);
	}
}
