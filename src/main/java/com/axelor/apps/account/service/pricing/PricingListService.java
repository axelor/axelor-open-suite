package com.axelor.apps.account.service.pricing;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.PricingList;
import com.axelor.apps.account.db.PricingListVersion;
import com.axelor.apps.tool.date.Period;

public class PricingListService {
	
	/**
	 * Retourne une liste de versions de barèmes actives compris entre deux dates.
	 * 
	 * @param pricingList
	 * @param from
	 * @param to
	 * @param adjustAgain
	 * @return
	 */
	public List<PricingListVersion> getUpdates(PricingList pricingList, Period period){
		
		List<PricingListVersion> pricingListVersions = new ArrayList<PricingListVersion>();
		
		if (pricingList.getPricingListVersionList().size() > 1){
			
			String sql = "pricingList = ?1 AND activeOk = true AND ((toDate = null AND fromDate >= ?2 AND fromDate < ?3) OR (toDate != null AND toDate > ?2 AND toDate <= ?3))";
			pricingListVersions.addAll(PricingListVersion.all().filter(sql,	pricingList, period.getFrom(), period.getTo()).fetch());
			
		}
		
		return pricingListVersions;
	}
	
}
