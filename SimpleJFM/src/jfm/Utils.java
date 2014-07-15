package jfm;

import java.io.File;

public class Utils {
	public static String getExt(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
		    extension = fileName.substring(i+1);
		}
		return extension;
	}
	
	public static String getFileName(String fileName) {
		String finalFileName = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
		    finalFileName = fileName.substring(0,i);
		}
		return finalFileName;
	}
	
	public static boolean checkIsVideo(String fileName) {
		boolean bResult = false;
		String fileExt = getExt(fileName);
		if (fileExt.equals("wmv")) {
			bResult = true;
		} else if (fileExt.equals("mp4")) {
			bResult = true;
		} else if (fileExt.equals("mkv")) {
			bResult = true;
		} else if (fileExt.equals("avi")) {
			bResult = true;
		} else if (fileExt.equals("asf")) {
			bResult = true;
		}
		return bResult;
	}
	public static boolean checkIsVideo(File inFile) {
		String FileName = inFile.getName();		
		return checkIsVideo(FileName);
	}
}
