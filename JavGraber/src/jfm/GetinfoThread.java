package jfm;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import javparser.MaddawParser;


public class GetinfoThread implements Runnable {
	int gIdx;
	String gsUrl;
	MasterPanel parentCompo;
	ProcessListener pListener;
	Thread t;

	GetinfoThread(int idx, MasterPanel inPanel, ProcessListener inListener) {
		gIdx = idx;
		parentCompo = inPanel;
		pListener = inListener;
		t = new Thread(this, "F"+Integer.toString(idx));
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		do {
			System.out.printf("%d gogo\n", gIdx);
			try {
				MaddawParser.setListener(pListener);
				MaddawParser.init();
				MaddawParser.doAction(gIdx);
				MaddawParser.close();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}			
		} while (false);
		parentCompo.setPath();
		System.out.printf("%d close\n", gIdx);
		return;
	}
}
