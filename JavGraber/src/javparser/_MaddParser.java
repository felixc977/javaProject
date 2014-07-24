package javparser;

import javData.JavEntry;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class _MaddParser extends JavParser {
	public String strGetIdUrl = "http://maddawgjav.net/page/";
	public String parserName = "maddawgjav";

	private final static Lock _mutex = new ReentrantLock(true);

	public _MaddParser() {
		try {
			init(parserName);
		} catch (IOException|ParseException e) {
			System.out.println("Init Error");
		}
	}

	@Override
	public void parseAction(int actDepth) {
		for (int i=1;i<=actDepth;i++) {
			String inUrl = String.format("%s%d", strGetIdUrl, i);
			Document doc;
			try {
				doc = Jsoup.connect(inUrl).userAgent("Mozilla").get();
			} catch (IOException e) {
				System.out.println("[Error]parseAction: IOException");
				return;
			}
			
			Element content = doc.getElementById("content");
			if (content != null) {
				Elements divContents = doc.getElementsByTag("div");
				for (Element divContent : divContents) {
					if (divContent.attr("id").contains("post-")) {
						JavEntry tmpV = new JavEntry();
						Elements links = divContent.getElementsByTag("a");
						Element link = links.get(0);
						
						tmpV.id=(divContent.attr("id").toString());
						//System.out.println("id: "+tmpV.id);
						
						if (checkIfExist(tmpV.id)) {
							//System.out.println(" alreay exist!");
							continue;
						}
						
						tmpV.title=(link.html());
						tmpV.link =(link.attr("href").toString());
						
						QueueA.add(tmpV);
						wakeupThreadB();
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean parseSubPage() {
		_mutex.lock();
		JavEntry tmpV = getNextJob();
		_mutex.unlock();

		if (tmpV==null) {
			return false;
		}

		Document subDoc=null;
		try {
			subDoc = Jsoup.connect(tmpV.link).userAgent("Mozilla").get();
			tmpV.label = getAttrRealId(tmpV.title);
			tmpV.imgSrc = getAttrImage(subDoc);
			tmpV.dllink = getAttrDLink(subDoc, tmpV.id);
			tmpV.cast = getAttrCast(subDoc);
			tmpV.date = getAttrDate(subDoc);
			tmpV.imgPath = DbRoot+tmpV.id+".jpg";
			saveImage(tmpV.imgSrc, tmpV.imgPath);
		} catch (IOException e) {
			System.out.println("[Error] "+e.getMessage());
			tmpV.imgPath = "";
		}

		// Final
		_mutex.lock();
		jDataList.add(tmpV);
		JSONObject jObj = transData(jDataList.lastElement());
		jTotalData.add(jObj);
		_mutex.unlock();
		return true;
	}

	@Override
	public String getAttrImage(Document doc) throws IOException {
		String imgSrc = null;
		Elements contents = doc.getElementsByClass("alignnone");
		imgSrc = contents.get(0).attr("src").toString();
		//System.out.println("imgsrc="+imgSrc);
		return imgSrc;
	}

	@Override
	public Vector<String> getAttrDLink(Document doc, String EntryId)
			throws IOException {
		Vector<String> dlink = new Vector<String>();
		Element contentMain = doc.getElementById(EntryId);
		Elements contents = contentMain.getElementsByTag("a");
		for (Element content : contents) {
			if (content.attr("href").contains("rapidgator")) {
				//System.out.println("dllink="+content.attr("href").toString());
				dlink.add(content.attr("href").toString());
			}				
		}

		return dlink;
	}

	@Override
	public Vector<String> getAttrCast(Document doc) throws IOException {
		Vector<String> vCast = new Vector<String>();
		Elements contents = doc.getElementsByTag("meta");
		for (Element content : contents) {
			if (content.attr("name").contains("keywords")) {
				String innerContent = content.attr("content").toString();
				String[] innerContentList = innerContent.split("[,\\s]+");
				for (int i=0;i<innerContentList.length;i++) {
					boolean bHit = false;
					//System.out.println(i+":"+innerContentList[i]);
					for (int j=0;j<vCast.size();j++) {
						if (vCast.get(j).contains(innerContentList[i])) {
							bHit = true;
							break;
						}
					}
					if (bHit==false) {
						vCast.add(innerContentList[i]);
					}
				}				
				break;
			}			
		}
		return vCast;
	}

	@Override
	public String getAttrDate(Document doc) throws IOException {
		Element content = doc.getElementsByClass("post-info-date").first();
		String dateInfo = content.html();
		int month = 1;
		int day = 1;
		int year = 2000;
		
		String spatt = "([a-zA-Z]+)\\s([0-9]+),\\s([0-9]+)";
		Pattern patt = Pattern.compile(spatt);
		Matcher matcher = patt.matcher(dateInfo);
		if (matcher.find()) {
			year = Integer.parseInt(matcher.group(3));
			day = Integer.parseInt(matcher.group(2));
			switch (matcher.group(1)) {
			case "January":
				month = 1;
				break;
			case "February":
				month = 2;
				break;
			case "March":
				month = 3;
				break;
			case "April":
				month = 4;
				break;
			case "May":
				month = 5;
				break;
			case "June":
				month = 6;
				break;
			case "July":
				month = 7;
				break;
			case "August":
				month = 8;
				break;
			case "September":
				month = 9;
				break;
			case "October":
				month = 10;
				break;
			case "November":
				month = 11;
				break;
			case "December":
				month = 12;
				break;
			default:
				System.out.println("getDat: Default");
			}
		}
		String sRet = String.format("%04d%02d%02d", year, month, day);
		return sRet;
	}
}