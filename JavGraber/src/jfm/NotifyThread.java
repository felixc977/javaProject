package jfm;

public class NotifyThread implements Runnable {
	int gIdx;
	String gsUrl;
	JfmMain targetCompo;
	Thread t;

	NotifyThread(int idx, JfmMain inPanel) {
		gIdx = idx;
		targetCompo = inPanel;
		t = new Thread(this, "F"+Integer.toString(idx));
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		targetCompo.parseAction(gIdx);
		System.out.printf("%d close\n", gIdx);
		return;
	}
}