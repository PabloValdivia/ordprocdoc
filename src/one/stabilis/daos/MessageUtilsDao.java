package one.stabilis.daos;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public interface MessageUtilsDao {

	public String isMessagesList(List<String> MessagesList_Data, Properties p_Ctx);

	public void getLackMessagesLog(List<String> MessagesList_Data, Properties p_Ctx);

	public List<HashMap<String, String>> getLackCompleteMessagesMap(List<String> MessagesList_Data, Properties p_Ctx);


}
