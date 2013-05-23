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