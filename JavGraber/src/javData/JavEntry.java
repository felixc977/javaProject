package javData;

import java.util.Vector;

public class JavEntry
{
	public String title;
	public String link;
	public String imgSrc;
	public String imgPath;
	public String id;
	public String date;
	public String director;
	public String label;
	public String maker;
	public Integer length;
	public Vector<String> cast;
	public Vector<String> genre;
	public Vector<String> dllink;
	
	public JavEntry()
	{
		this.cast = new Vector<String>();
		this.genre = new Vector<String>();
		this.dllink = new Vector<String>();
	}

	public JavEntry(JavEntry inEntry)
	{
		Clone(inEntry);
	}
	
	public void Clone(JavEntry inAvEntry)
	{
		title = inAvEntry.title;
		link = inAvEntry.link;
		imgSrc = inAvEntry.imgSrc;
		imgPath = inAvEntry.imgPath;
		id = inAvEntry.id;
		date = inAvEntry.date;
		director = inAvEntry.director;
		label = inAvEntry.label;
		maker = inAvEntry.maker;
		length = inAvEntry.length;
		this.cast = new Vector<String>(inAvEntry.cast);
		this.genre = new Vector<String>(inAvEntry.genre);
		this.dllink = new Vector<String>(inAvEntry.dllink);
	}
}