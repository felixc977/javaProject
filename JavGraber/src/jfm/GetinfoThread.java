package jfm;


public class GetinfoThread implements Runnable {
	int gIdx;
	String gsUrl;
	JfmMain mainFrame;
	Thread t;

	GetinfoThread(int idx, JfmMain inFrame) {
		gIdx = idx;
		mainFrame = inFrame;
		t = new Thread(this, "F"+Integer.toString(idx));
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		do {
			System.out.printf("%d gogo\n", gIdx);
			mainFrame.parseAction(gIdx);
		} while (false);
		System.out.printf("%d close\n", gIdx);
		return;
	}

}
