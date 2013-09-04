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

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.PricingList;
import com.axelor.apps.account.db.PricingListLine;
import com.axelor.apps.account.db.PricingListVersion;

public class PricingListLineService {
	
	public List<PricingListLine> getPricingListLine (List<PricingListVersion> versions, LocalDate from, LocalDate to){
		for (PricingListVersion version : versions) {
			if (from.isAfter(version.getFromDate()) || from == version.getFromDate() && (to.isBefore(version.getToDate())|| to == version.getToDate()) && !version.getPricingListLineList().isEmpty()){
				 return version.getPricingListLineList();
			}
		}
		return null;
	}
	
	public List<PricingListLine> getPricingListLine (PricingList pricingList, LocalDate date){
		for (PricingListVersion version : pricingList.getPricingListVersionList()) {
			if ((version.getFromDate().isBefore(date) || version.getFromDate() == date) && version.getToDate().isAfter(date) && !version.getPricingListLineList().isEmpty()){
				return version.getPricingListLineList();
			}
		}
		return null;
	}
	
	public List<PricingListLine> getPricingListLine (PricingList pricingList, LocalDate from, LocalDate to){
		for (PricingListVersion version: pricingList.getPricingListVersionList()) {
			if (from.isAfter(version.getFromDate()) || from == version.getFromDate() && (to.isBefore(version.getToDate())|| to == version.getToDate()) && !version.getPricingListLineList().isEmpty()){
				 return version.getPricingListLineList();
			}
		}
		return null;
	}
	
	public PricingListVersion getPricingListVersion (PricingList pricingList, LocalDate date){
		for (PricingListVersion version : pricingList.getPricingListVersionList()) {
			if (!version.getPricingListLineList().isEmpty() && (version.getFromDate().isBefore(date) || version.getFromDate() == date) && (version.getToDate() == null || version.getToDate().isAfter(date) || version.getToDate().isEqual(date))){
				return version;
			}
		}
		return null;
	}

	public List<PricingListVersion> getPricingListVersion (List<PricingListVersion> versions, LocalDate from, LocalDate to){
		List<PricingListVersion> res = new ArrayList<PricingListVersion>();
		for (PricingListVersion version: versions) {
			if(((version.getFromDate().isBefore(from) || version.getFromDate().isEqual(from)) && (version.getToDate().isBefore(to) || version.getToDate().isEqual(to))) || ((version.getFromDate().isBefore(from) || version.getFromDate().isEqual(from)) && version.getToDate().isAfter(to))){
				res.add(version);
			}
		}
		return res;
	}
	 
}