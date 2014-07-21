package javparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavIdInducer {
	private static String spattGeneral = "([a-zA-Z]{2,5})([\\-_]?)([0-9]{2,5})";
	private static String[][] spatt_NArray = {
		{"1Pon",		"(?i)(^1pon)|(^一本道)",			"[0-9]{6}[-_]+[0-9]{3}"},
		{"Heyzo",		"(?i)(^heyzo)|(^ヘイゾー)",		"[0-9]{4}"},
		{"Carib",		"(?i)(^carib)|(^カリビアン)",	"[0-9]{6}[-_]+[0-9]{3}"},
		{"TokyoHot",	"(?i)(^Tokyo Hot)",				"(?i)[a-z]{1}[0-9]{4}"},
		{"King8",		"(?i)(^Kin8)|(^金8天国)",		"[0-9]{4}"},
		{"Jgirl",		"(?i)(^JGIRL)",					"(?i)[a-z]{1}[0-9]{3}"},
		{"Mywife",		"(?i)(^Mywife)",				"[0-9]{5}"},
		{"Pacopaco",	"(?i)(^pacopaco)",				"[0-9]{6}[-_]+[0-9]{3}"},
		{"Xxx-Av",		"(?i)(^xxx-av)",				"[0-9]{5}"},
		{"Roselip",		"(?i)(^roselip)",				"[0-9]{4}"},
		{"天然むすめ",	"(?i)(^天然むすめ)",				"[0-9]{6}[-_]+[0-9]{2}"},
		{"メス豚",		"(?i)(^mesubuta)|(^メス豚)",		"[0-9]{6}[-_]+[0-9]{3}[-_]+[0-9]{2}"},
		{"1000Giri",	"(?i)(^1000giri)|(^1000人斬り)",	"[0-9]{6}"}
	};
	
	private static Pattern PattGeneral;

	static {
		PattGeneral = Pattern.compile(spattGeneral);
	}
	
	public static String transId(String inTitle) {
		String sJavId = "";
		boolean bHits = false;
		System.out.println("inTitle=>" + inTitle);

		do {
			/* Step1. N-Mask*/
			System.out.println("len="+spatt_NArray.length);
			for (int idx=0; idx<spatt_NArray.length; idx++)
			{
				Pattern prePatt = Pattern.compile(spatt_NArray[idx][1]);
				Matcher subMatcher = prePatt.matcher(inTitle);
				if (subMatcher.find()) {
					//sJavId = subMatcher.group(0);
					Pattern secPatt = Pattern.compile(spatt_NArray[idx][2]);
					Matcher tMatcher = secPatt.matcher(inTitle);
					if (tMatcher.find()) {
//						for (int i=0;i<matcher.groupCount();i++) {
//							System.out.println("group"+i+":"+ matcher.group(i));
//						}
						sJavId = spatt_NArray[idx][0]+"."+tMatcher.group(0);
						sJavId = sJavId.replace("_", "-");
						System.out.println("new=>" + sJavId);
						bHits = true;
						break;
					}
				}
			}

			/* Step2. Mask*/
			Matcher matcher = PattGeneral.matcher(inTitle);
			boolean matchFound = matcher.find();
			while (matchFound) {
				sJavId =  matcher.group(1).toUpperCase()+"-"+matcher.group(3);
				System.out.println("new=>" + sJavId);
				if (matcher.end() + 1 <= inTitle.length()) {
					matchFound = matcher.find(matcher.end());
					bHits = true;
				} else {
					break;
				}
			}
			
			/* Result*/
			if (bHits) {
				break;
			}
		} while (false);
		
		if (bHits) {
			//System.out.println(sJavId);
		}
		
		//System.out.println("Total File="+fileCount);
		return sJavId;
	}
}
