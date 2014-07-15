package jfm;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SelectionListener implements ListSelectionListener {
	
	public MasterPanel mainObj = null; 
	
	SelectionListener(MasterPanel masterPanel) {
		mainObj = masterPanel;
	}
	
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

        boolean isAdjusting = e.getValueIsAdjusting();
        
        if (isAdjusting)
        	return;

        if (lsm.isSelectionEmpty()) {
        	;
        } else {
            // Find out which indexes are selected.
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                	if (mainObj!=null) {
                    	mainObj.onEvent(i);
                    }
                }
            }
        }
    }
}