package com.axelor.apps.crm.db;

public interface IOpportunity {
	
	/**
	 * Static opportunity sales stage select
	 */
	static final int STAGE_NEW = 1;
	static final int STAGE_QUALIFICATION= 2;
	static final int STAGE_PROPOSITION = 3;
	static final int STAGE_NEGOTIATION = 4;
	static final int STAGE_CLOSED_WON = 5;
	static final int STAGE_CLOSED_LOST = 6;

}
