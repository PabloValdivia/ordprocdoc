package one.stabilis.daos;

import java.util.HashMap;
import java.util.List;

public interface MessageDataDao {
	
	public List<String> getMessagesList();

	public List<HashMap<String, String>> getCompleteMessagesList();


}
