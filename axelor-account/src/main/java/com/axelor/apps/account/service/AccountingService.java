package com.axelor.apps.account.service;


public class AccountingService {

	private static boolean DEFAULT_UPDATE_CUSTOMER_ACCOUNT = true;
	
	private static final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>();
	
	public static void setUpdateCustomerAccount(boolean updateCustomerAccount) {
		
		threadLocal.set(updateCustomerAccount);
		
	}

	public static boolean getUpdateCustomerAccount()  {
		
		if(threadLocal.get() != null)  {  return threadLocal.get();  }
			
		return DEFAULT_UPDATE_CUSTOMER_ACCOUNT;
		
	}

}
