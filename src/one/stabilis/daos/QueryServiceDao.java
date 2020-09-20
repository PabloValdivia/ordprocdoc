package one.stabilis.daos;

import java.util.Map;
import java.util.Properties;

public interface QueryServiceDao {

	public Map<String, String> getAllDocStatusPairsValueName(int p_MOrderStatusID, Properties p_Ctx, String p_Trx);

	public Boolean checkMessagesValue(String p_MsgValue, Properties p_Ctx);

}
