package one.stabilis.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;

import org.adempiere.exceptions.AdempiereException;

import one.stabilis.daos.StatusDataDao;
import one.stabilis.daos.StatusUtilsDao;
import one.stabilis.repository.StabilisData;

public class StatusUtils implements StatusUtilsDao {

	private StatusDataDao sdata = new StabilisData();


	@Override
	public void getAdempiereException(String msg) {
		throw new AdempiereException(msg);
	}

	@Override
	public Boolean isDocStatusNotAcceptable(String docStatus) {
		Boolean result = false;
		int count = 0;
		for (String tmp : sdata.getDocAllowedStatusesList()) {
			if (!docStatus.equalsIgnoreCase(tmp))
				count++;
		}
		if (count == sdata.getDocAllowedStatusesList().size())
			result = true;
		return result;
	}

	@Override
	public List<String> getAllowedNamesFromStatusesMap(Map<String, String> refPairsMap,
			List<String> listOfAllowedValues) {
		List<String> listOfAllowedNames = new ArrayList<String>();
		if (refPairsMap != null && listOfAllowedValues != null) {
			for (Entry<String, String> pair : refPairsMap.entrySet()) {
				for (String value : listOfAllowedValues)
					if (pair.getKey().equals(value))
						listOfAllowedNames.add(pair.getValue());
			}
		}
		return listOfAllowedNames;
	}

	@Override
	public String getStringOfAllowedStatuses(List<String> docAllowedStatusesList) {
		// this function requires Java 1.8 at least
		StringJoiner joiner = new StringJoiner(", ");
		for (String term : docAllowedStatusesList) {
			joiner.add(term);
		}
		return joiner.toString();
	}

}
