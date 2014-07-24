package jfm;

import javData.JavEntry;

public class NotifyThread implements Runnable {
	JavEntry gEntry;
	String gsUrl;
	JfmMain targetCompo;
	Thread t;

	NotifyThread(JavEntry inEntry, JfmMain inPanel) {
		gEntry = inEntry;
		targetCompo = inPanel;
		t = new Thread(this, "NotifyThread");
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		targetCompo.parseAction(gEntry);
		//System.out.printf("NotifyThread close\n");
		return;
	}
}