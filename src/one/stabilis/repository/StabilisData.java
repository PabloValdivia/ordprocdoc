package one.stabilis.repository;

import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MOrder;

import one.stabilis.daos.StatusDataDao;

public class StabilisData implements StatusDataDao {

	@Override
	// prepare list of allowed document statuses
	public List<String> getDocAllowedStatusesList() {
		List<String> statList = new ArrayList<String>();
		statList.add(MOrder.DOCSTATUS_Completed);
		statList.add(MOrder.DOCSTATUS_InProgress);
		statList.add(MOrder.DOCSTATUS_Closed);
		return statList;
	}

	@Override
	public Integer getMOrderStatusID() {
		return MOrder.DOCSTATUS_AD_Reference_ID;
	}

}
