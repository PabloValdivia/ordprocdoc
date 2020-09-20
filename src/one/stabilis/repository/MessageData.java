package one.stabilis.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.compiere.model.MMessage;

import one.stabilis.daos.MessageDataDao;

public class MessageData implements MessageDataDao{
	
	private String val = MMessage.COLUMNNAME_Value;
	private String txt = MMessage.COLUMNNAME_MsgText;
	private String tip = MMessage.COLUMNNAME_MsgTip;
	private String typ = MMessage.COLUMNNAME_MsgType;
	
	@Override
	public List<String> getMessagesList() {
		List<String> statList = new ArrayList<String>();
		List<HashMap<String, String>> msgCplList = getCompleteMessagesList();
		for (HashMap<String, String> mapTmp : msgCplList) {
			statList.add(mapTmp.get(val));
		}
		return statList;
	}

	@Override
	public List<HashMap<String, String>> getCompleteMessagesList() {
		List<HashMap<String, String>> msgCplList = new ArrayList<HashMap<String, String>>();
		msgCplList.add(new HashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3283722822529004303L;

			{
				put(val, "statusWrong");
				put(txt, "Wrong Status of document. ");
				put(tip, "Change to ");
				put(typ, MMessage.MSGTYPE_Error);
			}
		});
		msgCplList.add(new HashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5057468614742635897L;

			{
				put(val, "argumentWrong");
				put(txt, "Wrong type of argument submitted. ");
				put(tip, "");
				put(typ, MMessage.MSGTYPE_Error);
			}
		});
		msgCplList.add(new HashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8529530713188578025L;

			{
				put(val, "docGenerated");
				put(txt, "Document generated. ");
				put(tip, "");
				put(typ, MMessage.MSGTYPE_Information);
			}
		});
		return msgCplList;
	}

}
