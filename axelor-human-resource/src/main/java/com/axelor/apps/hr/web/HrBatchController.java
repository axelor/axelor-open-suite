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
package com.axelor.apps.hr.web;

import java.math.BigDecimal;
import java.util.Locale;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.joda.time.Days;
import org.joda.time.Years;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.batch.HrBatchService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class HrBatchController {
	
	
	private static final char TEMPLATE_DELIMITER = '$';
	protected TemplateMaker maker;

	@Inject
	HrBatchService hrBatchService;
	@Inject
	HrBatchRepository hrBatchRepo;
	
	
	/**
	 * Lancer le batch d'ajout de congés
	 *
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void actionLeaveManagement(ActionRequest request, ActionResponse response) throws AxelorException{

		HrBatch hrBatch = request.getContext().asType(HrBatch.class);

		Batch batch = hrBatchService.run(hrBatchRepo.find(hrBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}
	
	public void actionSeniorityLeaveManagement(ActionRequest request, ActionResponse response) throws AxelorException{

		
		HrBatch hrBatch = request.getContext().asType(HrBatch.class);

		Batch batch = hrBatchService.run(hrBatchRepo.find(hrBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}
}
