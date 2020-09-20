package one.stabilis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.logging.Level;

import org.compiere.model.MMessage;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import one.stabilis.daos.MessageDataDao;
import one.stabilis.daos.MessageUtilsDao;
import one.stabilis.daos.QueryServiceDao;
import one.stabilis.repository.MessageData;
import one.stabilis.services.QueryService;

public class MessageUtils implements MessageUtilsDao {

	private MessageDataDao mdata = new MessageData();
	private QueryServiceDao squery = new QueryService();

	private static CLogger log = CLogger.getCLogger(Env.class);

	@Override
	public String isMessagesList(List<String> MessagesList_Data, Properties p_Ctx) {
		StringJoiner joiner = new StringJoiner(", ");
		for (String term : MessagesList_Data) {
			if (!squery.checkMessagesValue(term, p_Ctx))
				joiner.add(term);
		}
		return joiner.toString();
	}

	@Override
	public void getLackMessagesLog(List<String> MessagesList_Data, Properties p_Ctx) {
		String lackMessages = isMessagesList(MessagesList_Data, p_Ctx);
		if (lackMessages != null)
			log.log(Level.WARNING, (Msg.translate(Env.getCtx(), "not.found ") + lackMessages));
	}

	@Override
	public List<HashMap<String, String>> getLackCompleteMessagesMap(List<String> MessagesList_Data, Properties p_Ctx) {
		List<HashMap<String, String>> mapListResult = new ArrayList<HashMap<String, String>>();
		if (MessagesList_Data != null) {
			for (String term : MessagesList_Data) {
				if (!squery.checkMessagesValue(term, p_Ctx))
					for (HashMap<String, String> mapTmp : mdata.getCompleteMessagesList()) {
						if (mapTmp.get(MMessage.COLUMNNAME_Value) == term) {
							mapListResult.add(mapTmp);
						}
					}
			}
			return mapListResult;
		} else
			return null;
	}

}
