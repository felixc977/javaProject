package jfm;
import javData.JavEntry;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.*;


class JfmMain extends JFrame
 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String configFilePath = "./conf.ini";
	public String url = "";
	public String videoPlayer = "";

	// Instance attributes used in this example
	private	MasterPanel	masterPanel;
	private	SlavePanel slavePanel;
	public File focusFile = null;

	// Constructor of main frame
	public JfmMain()
	{
		// get configure
		try {
			java.util.List<String> lines = Files.readAllLines(Paths.get(configFilePath), StandardCharsets.UTF_8);
			for (String line:lines){
				if (line.indexOf("url",0)==0){
					int startIdx = line.indexOf("\"",0)+1;
					int endIdx = line.indexOf("\"",startIdx);
					String subStr = line.substring(startIdx, endIdx);
					url = subStr;
				} else if (line.indexOf("video",0)==0){
					int startIdx = line.indexOf("\"",0)+1;
					int endIdx = line.indexOf("\"",startIdx);
					String subStr = line.substring(startIdx, endIdx);
					videoPlayer = subStr;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Set the frame characteristics
		setTitle("Felix FileManager");
		setSize(1000, 600);
		setBackground(Color.gray);

		// MasterPanel
		masterPanel = new MasterPanel(url, this);
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add(masterPanel,  BorderLayout.WEST);

		// slavePanel
		slavePanel = new SlavePanel();
		getContentPane().add(slavePanel,  BorderLayout.CENTER);

	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void onEvent(JavEntry inEntry){
		slavePanel.setToWaitState();
		NotifyThread t1 = new NotifyThread(inEntry, this);
		t1.start();
	}
	
	public void parseAction(JavEntry inEntry){
		System.out.print("parseAction\n");
		slavePanel.setInfo(inEntry);
	}
	
	public String getVideoPlayer() {
		return videoPlayer;
	}
	
	// Main entry point for this example
	public static void main( String args[] )
	{
		// Create an instance of the test application
		JfmMain mainFrame = new JfmMain();
		mainFrame.setVisible( true );
	}
}