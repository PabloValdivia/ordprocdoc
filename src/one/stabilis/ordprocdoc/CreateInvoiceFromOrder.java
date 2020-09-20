package one.stabilis.ordprocdoc;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import one.stabilis.base.CustomProcess;
import one.stabilis.daos.MessageDataDao;
import one.stabilis.daos.MessageUtilsDao;
import one.stabilis.daos.QueryServiceDao;
import one.stabilis.daos.StatusDataDao;
import one.stabilis.daos.StatusUtilsDao;
import one.stabilis.repository.MessageData;
import one.stabilis.repository.StabilisData;
import one.stabilis.services.QueryService;
import one.stabilis.utils.MessageFactory;
import one.stabilis.utils.MessageUtils;
import one.stabilis.utils.StatusUtils;

public class CreateInvoiceFromOrder extends CustomProcess {
	private int p_Invoice_DocType = 0;
	private int DocType = 0;

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
			if (param.getParameterName().equalsIgnoreCase("Invoice_DocType")) {
				p_Invoice_DocType = param.getParameterAsInt();
			}
		}
		mutil.getLackMessagesLog(mdata.getMessagesList(), p_Ctx);

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

			// new Message 'statusWrong' should be added in iD with translations
			sutil.getAdempiereException(Msg.translate(p_Ctx, mdata.getMessagesList().get(0)) + ": " + allowedDocNames);
			return null;
		}

		MInvoice minvoice = createInvoice(mOrder);

		getInvoicePopInfo(minvoice, mOrder);

		return null;
	}

	private MInvoice createInvoice(MOrder mOrder) {
		if (!mOrder.isSOTrx())
			DocType = p_Invoice_DocType;
		else
			DocType = mOrder.getC_DocType().getC_DocTypeInvoice_ID();

		MInvoice mInvoice = new MInvoice(mOrder, DocType, mOrder.getDateOrdered());
		mInvoice.saveEx();

		for (MOrderLine orderLine : mOrder.getLines()) {
			BigDecimal remainingQty = orderLine.getQtyOrdered().subtract(orderLine.getQtyDelivered());
			// check if remaining quantity is not equal or lower than ZERO
			if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
				MInvoiceLine invoiceLine = new MInvoiceLine(mInvoice);

				try {
					invoiceLine.setLine(orderLine.getLine());
					invoiceLine.setM_Product_ID(orderLine.getM_Product_ID());
					invoiceLine.setC_OrderLine_ID(orderLine.get_ID());
					invoiceLine.setC_UOM_ID(orderLine.getC_UOM_ID());
					invoiceLine.setQty(remainingQty);
					invoiceLine.setPriceEntered(orderLine.getPriceEntered());
					invoiceLine.setPriceActual(orderLine.getPriceActual());
					invoiceLine.setPriceList(orderLine.getPriceList());
					invoiceLine.setDescription(orderLine.getDescription());
					invoiceLine.setC_Project_ID(orderLine.getC_Project_ID());
					invoiceLine.setC_Campaign_ID(orderLine.getC_Campaign_ID());
				} catch (IllegalArgumentException e) {

					sutil.getAdempiereException(Msg.translate(p_Ctx, mdata.getMessagesList().get(1)) + ": "
							+ invoiceLine.get_ID() + ", " + orderLine.get_ID());
					invoiceLine.saveEx();
					mInvoice.saveEx();
				}
				invoiceLine.saveEx();
			} else {
				if (log.isLoggable(Level.WARNING))
					log.warning("No available qty in orderline " + orderLine.get_ID() + ", product id "
							+ orderLine.getM_Product_ID());
				continue;
			}
		}
		mInvoice.saveEx();
		return mInvoice;
	}

	private void getInvoicePopInfo(MInvoice minvoice, MOrder mOrder) {
		MDocType thisDocType = new MDocType(p_Ctx, DocType, null);
		addLog(Msg.translate(p_Ctx, mdata.getMessagesList().get(2)) + " "
				+ thisDocType.getPrintName(Env.getAD_Language(p_Ctx)) + "\n"); // Translated Doc Type Name
		addLog(minvoice.getC_Invoice_ID(), null, null, minvoice.getDocumentNo(), minvoice.get_Table_ID(),
				minvoice.getC_Invoice_ID());
	}
}
