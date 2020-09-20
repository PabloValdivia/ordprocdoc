package one.stabilis.ordprocdoc;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MWarehouse;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.model.MDocType;

import one.stabilis.base.CustomProcess;
import one.stabilis.daos.MessageDataDao;
import one.stabilis.daos.MessageUtilsDao;
import one.stabilis.daos.QueryServiceDao;
import one.stabilis.daos.StatusDataDao;
import one.stabilis.daos.StatusUtilsDao;
import one.stabilis.services.QueryService;
import one.stabilis.utils.StatusUtils;
import one.stabilis.repository.MessageData;
import one.stabilis.repository.StabilisData;
import one.stabilis.utils.MessageFactory;
import one.stabilis.utils.MessageUtils;

public class CreateInoutFromOrder extends CustomProcess {
	boolean p_isInOut;
	int p_Inout_DocType = 0;
	int DocType = 0;

	private StatusUtilsDao sutil = new StatusUtils();
	private MessageUtilsDao mutil = new MessageUtils();
	private QueryServiceDao squery = new QueryService();
	private StatusDataDao sdata = new StabilisData();
	private MessageDataDao mdata = new MessageData();
	private MessageFactory smsgfa = new MessageFactory();
	private Properties p_Ctx = Env.getCtx();

	@Override
	protected void prepare() {
		for (ProcessInfoParameter param : getParameter()) {
			if (param.getParameterName().equalsIgnoreCase("Inout_DocType")) {
				p_Inout_DocType = param.getParameterAsInt();
			}
		}

		mutil.getLackMessagesLog(mdata.getMessagesList(), Env.getCtx());

		// add new messages for this plugin
		smsgfa.createMessages(mutil.getLackCompleteMessagesMap(mdata.getMessagesList(), p_Ctx), p_Ctx);
	}

	@Override
	protected String doIt() throws Exception {
		MOrder mOrder = new MOrder(p_Ctx, getRecord_ID(), get_TrxName());

		if (sutil.isDocStatusNotAcceptable(mOrder.getDocStatus())) {

			String allowedDocNames = sutil.getStringOfAllowedStatuses(sutil.getAllowedNamesFromStatusesMap(
					squery.getAllDocStatusPairsValueName(sdata.getMOrderStatusID(), getCtx(), get_TrxName()),
					sdata.getDocAllowedStatusesList()));

			sutil.getAdempiereException(Msg.translate(p_Ctx, mdata.getMessagesList().get(0)) + ": " + allowedDocNames);
			return null;
		}

		MInOut inout = createInOut(mOrder);

		getInOutPopInfo(inout, mOrder);

		return null;
	}

	private MInOut createInOut(MOrder mOrder) {
		if (!mOrder.isSOTrx())
			DocType = p_Inout_DocType;
		else
			DocType = mOrder.getC_DocType().getC_DocTypeShipment_ID();

		MInOut mInOut = new MInOut(mOrder, DocType, mOrder.getDateOrdered());
		mInOut.saveEx();

		for (MOrderLine orderLine : mOrder.getLines()) {

			BigDecimal remainingQty = orderLine.getQtyOrdered().subtract(orderLine.getQtyDelivered());
			// check if remaining quantity is not equal or lower than ZERO
			if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
				MInOutLine inOutLine = new MInOutLine(mInOut);

				try {
					inOutLine.setM_Product_ID(orderLine.getM_Product_ID());
					inOutLine.setQty(remainingQty);
					inOutLine.setM_Locator_ID(
							MWarehouse.get(p_Ctx, mOrder.getM_Warehouse_ID()).getDefaultLocator().get_ID());
					inOutLine.setLine(orderLine.getLine());
					inOutLine.setM_InOut_ID(mInOut.get_ID());
					inOutLine.setC_OrderLine_ID(orderLine.get_ID());
					inOutLine.setC_UOM_ID(orderLine.getC_UOM_ID());
					inOutLine.setDescription(orderLine.getDescription());
					inOutLine.setC_Project_ID(orderLine.getC_Project_ID());
					inOutLine.setC_Campaign_ID(orderLine.getC_Campaign_ID());
					inOutLine.saveEx();
				} catch (IllegalArgumentException e) {

					sutil.getAdempiereException(Msg.translate(p_Ctx, mdata.getMessagesList().get(1)) + ": "
							+ inOutLine.get_ID() + ", " + orderLine.get_ID());
					inOutLine.saveEx();
					mOrder.saveEx();
				}
				inOutLine.saveEx();
			} else {
				if (log.isLoggable(Level.WARNING))
					log.warning("No available qty in orderline " + orderLine.get_ID() + ", product id "
							+ orderLine.getM_Product_ID());
				continue;
			}
		}
		mOrder.saveEx();
		return mInOut;
	}

	private void getInOutPopInfo(MInOut inout, MOrder mOrder) {
		MDocType thisDocType = new MDocType(Env.getCtx(), DocType, null);
		addLog(Msg.translate(p_Ctx, mdata.getMessagesList().get(2)) + " "
				+ thisDocType.getPrintName(Env.getAD_Language(p_Ctx)) + "\n"); // Translated Doc Type Name
		addLog(inout.getM_InOut_ID(), null, null, inout.getDocumentNo(), inout.get_Table_ID(), inout.getM_InOut_ID());
	}
}
