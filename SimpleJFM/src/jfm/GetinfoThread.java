package jfm;

import java.io.File;

public class GetinfoThread implements Runnable {
	File gFile;
	String gsUrl;
	JfmMain mainFrame;
	Thread t;

	GetinfoThread(File inFile, JfmMain inFrame) {
		gFile = inFile;
		mainFrame = inFrame;
		t = new Thread(this, inFile.getName());
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		do {
			if (gFile == null) {
				break;
			}
			System.out.printf("%s gogo\n", gFile.getName());
			mainFrame.parseAction(gFile);
		} while (false);
		System.out.printf("%s close\n", gFile.getName());
		return;
	}

}
