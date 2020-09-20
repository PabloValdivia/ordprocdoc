package one.stabilis.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MMessage;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class MessageFactory {

	private static CLogger log = CLogger.getCLogger(Env.class);

	public void createMessages(List<HashMap<String, String>> completeMessagesMap, Properties p_Ctx) {

		for (HashMap<String, String> mapTmp : completeMessagesMap) {

			MMessage newMsg = new MMessage(p_Ctx, null, null);

			newMsg.setValue(mapTmp.get(MMessage.COLUMNNAME_Value));
			newMsg.setMsgText(mapTmp.get(MMessage.COLUMNNAME_MsgText));
			newMsg.setMsgTip(mapTmp.get(MMessage.COLUMNNAME_MsgTip));
			newMsg.setMsgType(mapTmp.get(MMessage.COLUMNNAME_MsgType));

			newMsg.saveEx();

			if (log.isLoggable(Level.WARNING)) {
				log.warning("New message created: " + mapTmp.get(MMessage.COLUMNNAME_Value));
			}
		}
	}
}
