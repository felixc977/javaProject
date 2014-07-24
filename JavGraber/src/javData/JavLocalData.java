package javData;

import java.util.Vector;

import org.json.simple.JSONObject;

public class JavLocalData {
	public String label;
	public boolean downloaded;
	public boolean existed;
	
	public JavLocalData()
	{
		this.downloaded = false;
		this.existed = false;
		this.label = new String("");
	}

	public JavLocalData(JavLocalData inData)
	{
		clone(inData);
	}

	public boolean matches(String inId) {
		return inId.matches(this.label);
	}
	
	public void clone(JavLocalData inData)
	{
		this.downloaded = inData.downloaded;
		this.existed = inData.existed;
		this.label = new String(inData.label);
	}
	
	public static int find(String inLabel, Vector<JavLocalData> inVector)
	{
		int idx = 0;
		for(;idx<inVector.size();idx++) {
			if (inVector.get(idx).matches(inLabel)) {
				break;
			}
		}
		if (idx == inVector.size()) {
			return -1;
		} else {
			return idx;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject Data2JSON(JavLocalData inData) {
		if (inData==null){
			System.out.println("[Error]Data2JSON: Null inData");
			return null;
		}

		JSONObject jObj = new JSONObject();
		jObj.put("label", inData.label);
		jObj.put("existed", inData.existed);
		jObj.put("downloaded", inData.downloaded);

		return jObj;
	}
	
	public static JavLocalData Json2Data(JSONObject jObj) {
		if (jObj==null){
			System.out.println("[Error]Json2Data: Null jObj");
			return null;
		}
		
		JavLocalData javData = new JavLocalData();
		javData.label = (String) jObj.get("label");
		javData.existed = (boolean) jObj.get("existed");
		javData.downloaded = (boolean) jObj.get("downloaded");

		return javData;
	}
}
