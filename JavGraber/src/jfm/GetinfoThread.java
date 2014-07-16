package jfm;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import javparser.MaddawParser;


public class GetinfoThread implements Runnable {
	int gIdx;
	String gsUrl;
	MasterPanel parentCompo;
	Thread t;

	GetinfoThread(int idx, MasterPanel inPanel) {
		gIdx = idx;
		parentCompo = inPanel;
		t = new Thread(this, "F"+Integer.toString(idx));
	}
	
	public void start() {
		t.start();
	}

	public void run() {
		do {
			System.out.printf("%d gogo\n", gIdx);
			try {
				MaddawParser.init();
				MaddawParser.doAction(1);
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
