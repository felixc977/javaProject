package javreader;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirReader {
	private String path;
	private String spattGeneral = "([a-zA-Z]{2,5})([\\-_]?)([0-9]{2,5})";
	private String spattNM_1PON = "^(?i)(1pon)";
	private String spattNM_HEYZO = "(?i)(heyzo)";
	private String spattNM_CARIB = "(?i)(carib)";
	private Pattern PattGeneral;
	private Pattern PattNM_1PON;
	private Pattern PattNM_HEYZO;
	private Pattern PattNM_CARIB;

	DirReader(String inPath){
		this.path = inPath;
		init();
		getList();
	}
	
	private void init() {
		PattGeneral = Pattern.compile(spattGeneral);
		PattNM_1PON = Pattern.compile(spattNM_1PON);
		PattNM_HEYZO = Pattern.compile(spattNM_HEYZO);
		PattNM_CARIB = Pattern.compile(spattNM_CARIB);
	}
	
	public void getList() {
		Path root = Paths.get(path);
		try {
			Files.walkFileTree(root, 
					EnumSet.noneOf(FileVisitOption.class),
					1,
					new SimpleFileVisitor<Path>() {
		         @Override
		         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		             throws IOException
		         {
		        	 Path tPath = file.getFileName();
		        	 if (file.toFile().isDirectory()!=true) {
		        		 String newName = extractJavId(tPath);
		        		 System.out.println("old=>" + tPath);
		        		 boolean bSameName = newName.matches(tPath.toString());
			        	 if ((!bSameName)&&(newName!="")) {
			        		 //System.out.println("in");
			        		 //Files.move(file, file.resolveSibling(newName));
			        	 }
		        	 }		        	 
		             return FileVisitResult.CONTINUE;
		         }
		         @Override
		         public FileVisitResult postVisitDirectory(Path dir, IOException e)
		             throws IOException
		         {
		             if (e == null) {
		            	 //System.out.println(dir.getFileName());
		                 return FileVisitResult.CONTINUE;
		             } else {
		                 // directory iteration failed
		                 throw e;
		             }
		         }
		     });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String extractJavId(Path inPath) {
		String FileName = inPath.toString();
		String FileExt = getExt(FileName).toLowerCase();
		String sJavId = "";
		boolean bHits = false;

		do {
			/* NMask*/
			{
				Matcher subMatcher = PattNM_1PON.matcher(FileName);
				if (subMatcher.find()) {
					//sJavId = subMatcher.group(0);
					Pattern tPatt = Pattern.compile("[0-9]{6}_[0-9]{3}");
					Matcher tMatcher = tPatt.matcher(FileName);
					if (tMatcher.find()) {
						//System.out.println("1Pon id:"+tMatcher.group(0));
						sJavId = "1pon."+tMatcher.group(0)+"."+FileExt;
						System.out.println("new=>" + sJavId);
						bHits = true;
					}					
					break;
				}
			}
			if (bHits) {
				break;
			}
			
			/* Mask*/
			Matcher matcher = PattGeneral.matcher(FileName);
			boolean matchFound = matcher.find();
			while (matchFound) {
				sJavId =  matcher.group(1).toUpperCase()+"-"+matcher.group(3)+"."+FileExt;
				System.out.println("new=>" + sJavId);
				if (matcher.end() + 1 <= FileName.length()) {
					matchFound = matcher.find(matcher.end());
					bHits = true;
				} else {
					break;
				}
			}
		} while (false);
		
		if (bHits) {
			//System.out.println(sJavId);
		}
		
		//System.out.println("Total File="+fileCount);
		return sJavId;
	}
	
	public static String getExt(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
		    extension = fileName.substring(i+1);
		}
		return extension;
	}
}
