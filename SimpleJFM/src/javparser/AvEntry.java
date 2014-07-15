package javparser;

import java.util.ArrayList;
import java.util.List;

public class AvEntry
{
	public String title;
	public String imgSrc;
	public String imgPath;
	public String id;
	public String date;
	public String director;
	public String label;
	public String maker;
	public Integer length;
	public List<String> cast;
	public List<String> genre;
	
	AvEntry()
	{
		this.cast = new ArrayList<String>();
		this.genre = new ArrayList<String>();
	}
	
	public void Clone(AvEntry inAvEntry)
	{
		title = inAvEntry.title;
		imgSrc = inAvEntry.imgSrc;
		imgPath = inAvEntry.imgPath;
		id = inAvEntry.id;
		date = inAvEntry.date;
		director = inAvEntry.director;
		label = inAvEntry.label;
		maker = inAvEntry.maker;
		length = inAvEntry.length;
		this.cast = new ArrayList<String>(inAvEntry.cast);
		this.genre = new ArrayList<String>(inAvEntry.genre);
	}
}