/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
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
