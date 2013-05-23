package com.axelor.apps.base.db;

/**
 * Interface of Pricing package. Enum all static variable of packages.
 * 
 * @author guerrier
 * 
 */
public interface IPricing {

	/**
	 * Static select in Pricing
	 */

	// ADMINISTERED ELECTED
	static final String ADMIN = "admin";
	static final String ELU = "elu";
	static final String ROUTING = "routing";
	static final String SERVICE = "service";

	/**
	 * Static select in Constituent
	 */

	// TYPE SELECT VALUE
	static final int VAR_TYPE = 1;
	static final int FIX_TYPE = 2;

	// COMMITMENT TYPE SELECT VALUE
	static final int NONE = 0;
	static final int COMMITMENT_TYPE = 1;
	static final int HTA_TYPE = 2;
	static final int DISC_BAND_TYPE = 3;
	
	// POWER INPUT MODE 
	static final int NO_POWER = 1;
	static final int MONO_POWER = 2;
	static final int MULTI_POWER = 3;
	

}
