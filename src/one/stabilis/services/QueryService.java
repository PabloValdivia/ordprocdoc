package one.stabilis.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Language;

import one.stabilis.daos.QueryServiceDao;

public class QueryService implements QueryServiceDao {

	private static CLogger log = CLogger.getCLogger(Env.class);

	@Override
	public Map<String, String> getAllDocStatusPairsValueName(int p_MOrderStatusID, Properties p_Ctx, String p_Trx) {
		Map<String, String> refPairsMap = new HashMap<String, String>();
		PreparedStatement pstmtdsn = null;
		ResultSet rsdsn = null;
		String ctxtLang = Env.getAD_Language(p_Ctx);
		int AD_Client_ID = Env.getAD_Client_ID(p_Ctx);
		String sqlRefName = " FROM AD_Ref_List rf  "
				+ "LEFT JOIN AD_Ref_List_Trl rltrl on rltrl.AD_Ref_List_ID = rf.AD_Ref_List_ID  "
				+ "WHERE rf.AD_Reference_ID=? AND (rf.AD_Client_ID=? or rf.AD_Client_ID=0) ";
		if (!ctxtLang.equals(Language.getBaseAD_Language()))
			sqlRefName = "SELECT rf.value, rltrl.name " + sqlRefName + " and rltrl.AD_Language='" + ctxtLang + "' ";
		else
			sqlRefName = "SELECT rf.value, rf.name " + sqlRefName;
		try {
			pstmtdsn = DB.prepareStatement(sqlRefName, p_Trx);
			pstmtdsn.setInt(1, p_MOrderStatusID);
			pstmtdsn.setInt(2, AD_Client_ID);
			rsdsn = pstmtdsn.executeQuery();
			while (rsdsn.next()) {
				refPairsMap.put(rsdsn.getString(1), rsdsn.getString(2));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlRefName, e);
			return null;
		} finally {
			DB.close(rsdsn, pstmtdsn);
			pstmtdsn = null;
			rsdsn = null;
		}
		return refPairsMap;
	}

	@Override
	public Boolean checkMessagesValue(String p_MsgValue, Properties p_Ctx) {
		Boolean result = false;
		Statement stmtcms = null;
		ResultSet rscms = null;
		String sqlMsg = "SELECT COUNT(*) FROM ad_message am WHERE am.value LIKE ('" + p_MsgValue + "')"
				+ "AND am.ad_client_id IN (0," + Env.getAD_Client_ID(p_Ctx) + ") GROUP BY am.ad_client_id";
		try {
			stmtcms = DB.createStatement();
			rscms = stmtcms.executeQuery(sqlMsg);
			while (rscms.next()) {
				if (rscms.getInt(1) > 0) {
					result = true;
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlMsg, e);
			result = false;
		} finally {
			DB.close(rscms, stmtcms);
			stmtcms = null;
			rscms = null;
		}
		return result;
	}
}
