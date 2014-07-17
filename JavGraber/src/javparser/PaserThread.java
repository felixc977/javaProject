package javparser;

public class PaserThread implements Runnable {
	private String Name;
	public Thread t;

	PaserThread(String inName) {
		Name = inName;
		t = new Thread(this, Name);
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		while (true) {
			if (MaddawParser.parseSubPage()) {
				;
			}else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					;
				}
			}
		}
	}
}