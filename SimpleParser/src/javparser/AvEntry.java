package javparser;

import java.util.ArrayList;
import java.util.List;

public class AvEntry
{
	public String title;
	public String imgSrc;
	public String id;
	public String date;
	public String director;
	public String label;
	public String maker;
	public int length;
	public List<String> cast;
	public List<String> genre;
	
	public AvEntry()
	{
		this.cast = new ArrayList<String>();
		this.genre = new ArrayList<String>();
	}
}