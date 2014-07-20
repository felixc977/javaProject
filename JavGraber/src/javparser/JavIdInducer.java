package javparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavIdInducer {
	private static String spattGeneral = "([a-zA-Z]{2,5})([\\-_]?)([0-9]{2,5})";
	private static String spattNM_1PON = "(?i)(1pon)|(一本道)";
	//private static String spattNM_HEYZO = "(?i)(heyzo)";
	//private static String spattNM_CARIB = "(?i)(carib)";
	private static Pattern PattGeneral;
	private static Pattern PattNM_1PON;
	//private static Pattern PattNM_HEYZO;
	//private static Pattern PattNM_CARIB;

	static {
		PattGeneral = Pattern.compile(spattGeneral);
		PattNM_1PON = Pattern.compile(spattNM_1PON);
		//PattNM_HEYZO = Pattern.compile(spattNM_HEYZO);
		//PattNM_CARIB = Pattern.compile(spattNM_CARIB);
	}
	
	public static String transId(String inTitle) {
		String sJavId = "";
		boolean bHits = false;
		System.out.println("inTitle=>" + inTitle);

		do {
			/* NMask*/
			{
				Matcher subMatcher = PattNM_1PON.matcher(inTitle);
				if (subMatcher.find()) {
					//sJavId = subMatcher.group(0);
					Pattern tPatt = Pattern.compile("[0-9]{6}_[0-9]{3}");
					Matcher tMatcher = tPatt.matcher(inTitle);
					if (tMatcher.find()) {
//						for (int i=0;i<matcher.groupCount();i++) {
//							System.out.println("group"+i+":"+ matcher.group(i));
//						}

						//System.out.println("1Pon id:"+tMatcher.group(0));
						sJavId = "1PON."+tMatcher.group(0);
						System.out.println("new=>" + sJavId);
						bHits = true;
					}					
					break;
				}
			}

			/* Mask*/
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
