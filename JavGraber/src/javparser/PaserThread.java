package javparser;

public class PaserThread implements Runnable {
	private String Name;
	private JavParser parser;
	private boolean wait;
	public Thread t;

	PaserThread(String inName, JavParser inParser) {
		Name = inName;
		parser = inParser;
		wait = true;
		t = new Thread(this, Name);
	}
	
	public void start() {
		t.start();
	}

	public boolean isWait() {
		return wait;
	}

	public void run() {
		while (true) {
			wait = false;
			if (parser.parseSubPage()) {
				;
			}else {
				wait = true;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					;
				}
			}
		}
	}
}