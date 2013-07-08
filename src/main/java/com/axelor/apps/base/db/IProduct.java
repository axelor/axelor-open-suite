package com.axelor.apps.base.db;

/**
 * Interface of Product package. Enum all static variable of packages.
 * 
 * @author Tan Rodrigue 
 * 
 */
public interface IProduct {

	// APPLICATION TYPE SELECT VALUE
	static final int PRODUCT_TYPE = 1;
	static final int PROFILE_TYPE = 2;
	static final int EXPENSE_TYPE = 3;

	// PRODUCT TYPE SELECT
	static final String SERVICE = "service";
	static final String STOCKABLE = "stockable";
}
