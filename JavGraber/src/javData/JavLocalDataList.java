package javData;

import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JavLocalDataList {
	private HashMap<String, JavLocalData> listMap;
	
	public JavLocalDataList()
	{
		listMap = new HashMap<String, JavLocalData>();
	}

	public JavLocalDataList(JSONArray jsonArray)
	{
		assign(jsonArray);
	}

	public void assign(JSONArray jsonArray) {
		/* init */
		listMap = new HashMap<String, JavLocalData>();

		/* assign */
		for (int i = 0; i < jsonArray.size(); i++) {
			JavLocalData jEntry = JavLocalData.Json2Data((JSONObject) jsonArray.get(i));
			put(jEntry);
		}
	}
	
	public JSONArray out() {
		JSONArray jsonArray = new JSONArray();
		return jsonArray;
	}
	
	public JavLocalData get(String qLabel) {
		JavLocalData jEntry = listMap.get(qLabel);
		System.out.println("[FUCK] test:"+jEntry);
		return jEntry;
	}
	
	public void put(JavLocalData inEntry) {
		listMap.put(inEntry.label, inEntry);
	}
	
	public void remove(String qLabel) {
		listMap.remove(qLabel);
	}
}
