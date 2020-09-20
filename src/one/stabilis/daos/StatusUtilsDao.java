package one.stabilis.daos;

import java.util.List;
import java.util.Map;

public interface StatusUtilsDao {

	public void getAdempiereException(String msg);
	
	public Boolean isDocStatusNotAcceptable(String docStatus);

	public List<String> getAllowedNamesFromStatusesMap(Map<String, String> refPairsMap,
			List<String> listOfAllowedValues);

	public String getStringOfAllowedStatuses(List<String> docAllowedStatusesList);


}
