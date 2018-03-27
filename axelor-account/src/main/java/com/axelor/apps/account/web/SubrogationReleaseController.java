/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import java.io.IOException;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.SubrogationReleaseService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SubrogationReleaseController {

	private SubrogationReleaseService subrogationReleaseService;

	@Inject
	public SubrogationReleaseController(SubrogationReleaseService subrogationReleaseService) {
		this.subrogationReleaseService = subrogationReleaseService;
	}

	public void retrieveInvoices(ActionRequest request, ActionResponse response) {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		Company company = subrogationRelease.getCompany();
		List<Invoice> invoiceList = subrogationReleaseService.retrieveInvoices(company);
		response.setValue("invoiceSet", invoiceList);
	}

	public void transmitRelease(ActionRequest request, ActionResponse response) {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		subrogationRelease = Beans.get(SubrogationReleaseRepository.class).find(subrogationRelease.getId());
		subrogationReleaseService.transmitRelease(subrogationRelease);
		response.setReload(true);
	}

	public void printToPDF(ActionRequest request, ActionResponse response) throws AxelorException {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		String name = String.format("%s %s", I18n.get("Subrogation release"), subrogationRelease.getSequenceNumber());
		String fileLink = subrogationReleaseService.printToPDF(subrogationRelease, name);
		response.setView(ActionView.define(name).add("html", fileLink).map());
		response.setReload(true);
	}

	public void exportToCSV(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		subrogationReleaseService.exportToCSV(subrogationRelease);
		response.setReload(true);
	}

	public void enterReleaseInTheAccounts(ActionRequest request, ActionResponse response) throws AxelorException {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		subrogationRelease = Beans.get(SubrogationReleaseRepository.class).find(subrogationRelease.getId());
		subrogationReleaseService.enterReleaseInTheAccounts(subrogationRelease);
		response.setReload(true);
	}

}
