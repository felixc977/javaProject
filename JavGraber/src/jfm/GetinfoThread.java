package jfm;


public class GetinfoThread implements Runnable {
	int gIdx;
	MasterPanel parentCompo;
	Thread t;

	GetinfoThread(int idx, MasterPanel inPanel) {
		gIdx = idx;
		parentCompo = inPanel;
		t = new Thread(this, "F"+Integer.toString(gIdx));
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		System.out.printf("%d gogo\n", gIdx);
		parentCompo.doGrabAction(gIdx);			
		//parentCompo.setPath();
		System.out.printf("%d close\n", gIdx);
	}
}
