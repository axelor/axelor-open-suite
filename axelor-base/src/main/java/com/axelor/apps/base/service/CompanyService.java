package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;

public interface CompanyService {

	/**
	 * Check whether the provided company has more than one active bank details.
	 * In that case, enable the manageMultiBanks boolean in the general object.
	 * 
	 * @param company
	 *            the company to check for multiple active bank details
	 */
	void checkMultiBanks(Company company);

}
