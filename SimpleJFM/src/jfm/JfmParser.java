package jfm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.SocketTimeoutException;

import javparser.AvEntry;
import javparser.Parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;

public class JfmParser {
	static String strDbDir = "./_db/";
	static String strImageDir = strDbDir+"imgtemp";
	static String strJsonFile = strDbDir+"DB.json";
	static String strQueryUrl = "http://www.javlibrary.com/tw/vl_searchbyid.php?keyword=";
	static String strGetIdUrl = "http://www.javlibrary.com/tw/?v=";
	static JSONArray jTotalData = new JSONArray();

	enum ParseResult {
	    NotExist,   // the name cannot be found on Web
	    Success,    // grab the data successfully
	    Duplicate   // there is already a duplication in DB
	} 
	
	public static ParseResult start(String inName) throws Exception {
		/* Load the existing JSON */
		try {
			FileReader fileReader = new FileReader(strJsonFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String jsonText = bufferedReader.readLine();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonText);
			jTotalData = (JSONArray) obj;
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			;
		}

		/* dirName DB_Dir existing*/
		String dirName =  strDbDir;
		File fileJav = new File(dirName);
		if (fileJav.exists()) {
			System.out.println("dir exist\n");
		} else {
			System.out.println("dir create\n");
			System.out.println(fileJav.mkdir());
		}

		/* Check if Image_Dir existing */
		dirName =  strImageDir;
		fileJav = new File(dirName);
		if (fileJav.exists()) {
			System.out.println("dir exist\n");
		} else {
			System.out.println("dir create\n");
			System.out.println(fileJav.mkdir());
		}

	    /* Grab the data */
		ParseResult eResult = doAction(inName);
		if (ParseResult.Success == eResult){
			StringWriter out = new StringWriter();
			jTotalData.writeJSONString(out);
			String jsonText2 = out.toString().replaceAll("\\\\", "");

			FileWriter fileWriter = new FileWriter(strJsonFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(jsonText2);
			bufferedWriter.close();
		}
		
		return eResult;
	}
	
	static AvEntry getInfoByName(String inName) {
		AvEntry stObj = null;
//		System.out.printf("getInfoByName inName=%s\n",inName);
		try {
			FileReader fileReader = new FileReader(strJsonFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String jsonText = bufferedReader.readLine();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonText);
			jTotalData = (JSONArray) obj;
			bufferedReader.close();
		} catch (IOException | ParseException e) {
			;
		}
		
		JSONObject jTmpObj = null;
		for (int i = 0; i < jTotalData.size(); i++) {
			jTmpObj = (JSONObject) jTotalData.get(i);
			if (inName.matches((String) jTmpObj.get("id"))) {
				System.out.printf("getInfoByName Hit!\n");
				stObj = Parser.transJsonData(jTmpObj);
				break;
			}
		}
	
		return stObj;
	}

	@SuppressWarnings("unchecked")
	static ParseResult doAction(String javId) {
		boolean bHit = false;
		ParseResult eResult = ParseResult.NotExist;

		do {
			for (int i = 0; i < jTotalData.size(); i++) {
				JSONObject jTmpObj = (JSONObject) jTotalData.get(i);
//				System.out.printf("%s %s \n", jTmpObj.get("id"), javId);
				if (javId.matches((String) jTmpObj.get("id"))) {
					bHit = true;
					break;
				}
			}

			if (bHit == true) {
				System.out.printf("%s skip\n", javId);
				eResult = ParseResult.Duplicate;
				break;
			}

			System.out.printf("%s processing\n", javId);
			Document doc = null;
			try {
				doc = Parser.getUrl(javId);
				if (doc == null) {
					System.out.printf("%s not exist\n", javId);
					break;
				}
			} catch (SocketTimeoutException e) {
				System.out.printf("%s timeout\n", javId);
				break;
			} catch (IOException e) {
				System.out.printf("%s IOException\n", javId);
				break;
			}

			AvEntry avEntry = null;
			try {
				avEntry = Parser.getAttr(doc);
			} catch (IOException e) {
				System.out.printf("%s getAttr, IOException\n", javId);
				break;
			}
			
			String dirName = strImageDir;
			avEntry.imgPath = dirName + "/" + avEntry.id + ".jpg";
			System.out.printf("imgPath  %s \n", avEntry.imgPath);
			try {
				Parser.saveImage(avEntry.imgSrc, avEntry.imgPath);
			} catch (IOException e) {
				System.out.printf("%s saveImage, IOException\n", javId);
				break;
			}

			JSONObject jObj = Parser.transData(avEntry);
			jTotalData.add(jObj);
			eResult = ParseResult.Success;
		} while (false);
		
		return eResult;
	}
}
