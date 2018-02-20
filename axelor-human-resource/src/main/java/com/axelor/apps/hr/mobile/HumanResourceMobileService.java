package com.axelor.apps.hr.mobile;

import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public interface HumanResourceMobileService {
	/**
	 * This method is used in mobile application.
	 * It was in ExpenseService
	 * @param request
	 * @param response
	 */
	@Transactional
	public void insertExpenseLine(ActionRequest request, ActionResponse response);
	
	/**
	 * This method is used in mobile application.
	 * It was in TimesheetService
	 * @param request
	 * @param response
	 */
	public void getActivities(ActionRequest request, ActionResponse response);
	
	/**
	 * This method is used in mobile application.
	 * It was in TimesheetService
	 * @param request
	 * @param response
	 */
	@Transactional
	public void insertTSLine(ActionRequest request, ActionResponse response);

	/**
	 * This method is used in mobile application.
	 * It was in LeaveService
	 * @param request
	 * @param response
	 */
	@Transactional
	public void insertLeave(ActionRequest request, ActionResponse response) throws AxelorException;
}