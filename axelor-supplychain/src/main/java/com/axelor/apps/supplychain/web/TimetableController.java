/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TimetableController {
	
	@Inject
	protected TimetableService timetableService;
	
	@Inject
	protected TimetableRepository timeTableRepo;
	
	public void generateInvoice(ActionRequest request, ActionResponse response) throws AxelorException{
		Timetable timetable = request.getContext().asType(Timetable.class);
		timetable = timeTableRepo.find(timetable.getId());
		Invoice invoice = timetableService.generateInvoice(timetable);
		response.setView(ActionView
				.define(I18n.get("Invoice generated"))
				.model("com.axelor.apps.account.db.Invoice")
				.add("form", "invoie-form")
				.add("grid", "invoice-grid")
				.param("forceEdit", "true")
				.context("_showRecord", invoice.getId().toString())
				.map());
		response.setCanClose(true);
	}
}
