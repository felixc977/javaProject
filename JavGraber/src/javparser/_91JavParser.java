package javparser;

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

public class _91JavParser extends JavParser {
	public String strGetIdUrl = "http://91javporn.com/page/";
	public String parserName = "91Javporn";

	private final static Lock _mutex = new ReentrantLock(true);

	public _91JavParser() {
		try {
			init(parserName);
		} catch (IOException|ParseException e) {
			System.out.println("Init Error");
		}
	}

	@Override
	public void parseAction(int actDepth) {
		System.out.println("parseAction");
		for (int i=1;i<=actDepth;i++) {
			String inUrl = String.format("%s%d", strGetIdUrl, i);
			Document doc;
			try {
				doc = Jsoup.connect(inUrl).userAgent("Mozilla").get();
			} catch (IOException e) {
				System.out.println("[Error]parseAction: IOException");
				return;
			}
			
			Elements divContents = doc.getElementsByTag("div");
			for (Element divContent : divContents) {
				String tmpStr = divContent.attr("class");
				Pattern tmpPatt = Pattern.compile("(post-[0-9]+)");
				Matcher matcher = tmpPatt.matcher(tmpStr);
				
				if (matcher.find()) {
					JavEntry tmpV = new JavEntry();
					tmpV.id=matcher.group(0);
					//System.out.println("id: "+tmpV.id);
					
					if (checkIfExist(tmpV.id)) {
						//System.out.println(" alreay exist!");
						continue;
					}
					
					Elements subAs = divContent.getElementsByTag("a");
					Element subA = subAs.first();
					tmpV.title=(subA.html());
					System.out.println("title: "+tmpV.title);
					tmpV.link =(subA.attr("href").toString());
					System.out.println("link: "+tmpV.link);

					QueueA.add(tmpV);
					wakeupThreadB();
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
		int RetryTimes = 5;
		do {
			try {
				subDoc = Jsoup.connect(tmpV.link).userAgent("Mozilla").get();
				if (subDoc!=null) {
					break;
				}
			}catch (IOException e) {
				RetryTimes--;
				System.out.println("[Warn] parseSubPage:"+e.getMessage());
				System.out.println("[Warn] reconnect:");
			}
		} while(RetryTimes>0);
		try {
			tmpV.label = getAttrRealId(tmpV.title);
			tmpV.imgSrc = getAttrImage(subDoc);
			tmpV.dllink = getAttrDLink(subDoc, tmpV.id);
			tmpV.cast = getAttrCast(subDoc);
			tmpV.date = getAttrDate(subDoc);
			tmpV.imgPath = DbRoot+tmpV.id+".jpg";
			saveImage(tmpV.imgSrc, tmpV.imgPath);
		} catch (IOException e) {
			System.out.println("[Error] parseSubPage2:"+e.getMessage());
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
		Elements contents = doc.getElementsByClass("entry");
		Element divContent = contents.first();
		Elements imgContents = divContent.getElementsByTag("img");
		Element imgContent = imgContents.first();
		imgSrc = imgContent.attr("src").toString();

		System.out.println("imgsrc="+imgSrc);
		return imgSrc;
	}
	
	@Override
	public Vector<String> getAttrDLink(Document doc, String EntryId)
			throws IOException {
		Vector<String> dlink = new Vector<String>();
		Elements contents = doc.getElementsByTag("a");
		for (Element content : contents) {
			if (content.attr("href").contains("http://rapidgator.net/file/")) {
//				System.out.println("dllink="+content.attr("href").toString());
				dlink.add(content.attr("href").toString());
			}				
		}

		return dlink;
	}

	@Override
	public Vector<String> getAttrCast(Document doc) throws IOException {
		Vector<String> vCast = new Vector<String>();
		Elements contents = doc.getElementsByClass("entry");
		Element divContent = contents.first();
		{
			String tmpStr = divContent.html().toString();
			Pattern tmpPatt = Pattern.compile("出演[\\s]+([^<]+)");
			Matcher matcher = tmpPatt.matcher(tmpStr);
			if (matcher.find()) {
				String innerContent = matcher.group(1);
				String[] innerContentList = innerContent.split("[（）、\\s]+");
				for (int i=0;i<innerContentList.length;i++) {
					boolean bHit = false;
//					System.out.println(i+":"+innerContentList[i]);
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
			}
		}
		return vCast;
	}

	@Override
	public String getAttrDate(Document doc) throws IOException {
		String dateInfo = null;
		Elements contents = doc.getElementsByTag("span");
		for (Element content:contents){
			if (content.attr("class").contains("post-info-date")){
				dateInfo = content.html();
			}
		}

		if (dateInfo==null){
			return null;
		}

		int month = 1;
		int day = 1;
		int year = 2000;
		
		String spatt = "([a-zA-Z]+)[\\s]?([0-9]+),[\\s]?([0-9]+)";
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
//		System.out.println(sRet);
		return sRet;
	}
}