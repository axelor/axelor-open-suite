package com.axelor.apps.crm.db.repo;

import com.axelor.apps.crm.db.Event;

public class EventManagementRepository extends EventRepository {

	
	@Override
	public Event copy(Event entity, boolean deep) {
		int eventType=entity.getTypeSelect();
		switch(eventType){
			case 1: //call
			case 2: //metting
				entity.setStatusSelect(1);
				break;
			case 3: //task s
				entity.setTaskStatusSelect(1);
				break;
			case 5: //tickets
				entity.setTicketStatusSelect(1);
				entity.setProgressSelect(0);
				break;
				
		}
		return super.copy(entity, deep);
	}
	

}
