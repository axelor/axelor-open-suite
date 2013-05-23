package com.axelor.apps.sale.db;

public interface ISale {

	/**
	 * Static select in Product Category
	 */
	// TypeSelect
	static final String DEFAULT = "default";
	static final String SUBSCRIPTION = "subscription";
	static final String CONSUMPTION = "consumption";
	static final String OVERLOAD = "overload";
	static final String ROUTING = "routing";
	static final String FEE = "fee";
	static final String SERVICE = "service";
	static final String PROVISION = "provision";
	
	/**
	 * Static select in SalesRuleAction
	 */
   static final int MEETING = 1;
   static final int SALES_OFFER_PERSO = 2;
   static final int EMAIL = 3;
   static final int MAIL = 4;
   static final int ALARM = 5;

   
   static final int ANNIVERSARY_DATE = 0;
   static final int NOTIFICATION = 1;
   static final int INVOICING_BLOCKING = 3;

}
