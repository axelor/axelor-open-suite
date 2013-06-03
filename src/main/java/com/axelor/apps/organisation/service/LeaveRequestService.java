package com.axelor.apps.organisation.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.organisation.db.IEvent;
import com.axelor.apps.organisation.db.LeaveRequest;
import com.google.inject.persist.Transactional;

public class LeaveRequestService {

	private static final Logger LOG = LoggerFactory.getLogger(LeaveRequestService.class);
	
	@Transactional
	public Event createHolidayEvent(LeaveRequest leaveRequest)  {
		
		Event event = new Event();
		event.setTypeSelect(IEvent.HOLIDAY);
		event.setStartDateTime(leaveRequest.getStartDateT());
		event.setEndDateTime(leaveRequest.getEndDateT());
		event.setUserInfo(leaveRequest.getEmployeeUserInfo());
		if(leaveRequest.getLeaveRequestReason()!= null)  {
			event.setDescription(leaveRequest.getLeaveRequestReason().getName());
		}
		return event.save();
		
	}
	
	
	
}
