package javparser;

public class PaserThread implements Runnable {
	private String Name;
	private JavParser parser;
	public Thread t;

	PaserThread(String inName, JavParser inParser) {
		Name = inName;
		parser = inParser;
		t = new Thread(this, Name);
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		while (true) {
			if (parser.parseSubPage()) {
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