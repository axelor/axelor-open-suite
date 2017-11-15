package com.axelor.apps.helpdesk.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.helpdesk.db.repo.TicketManagementRepository;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;

public class HelpdeskModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(TicketRepository.class).to(TicketManagementRepository.class);
	}

}
