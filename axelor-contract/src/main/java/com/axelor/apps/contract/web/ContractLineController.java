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
package com.axelor.apps.contract.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ContractLineController {
	
	public void changeProduct(ActionRequest request, ActionResponse response) {
		ContractLine contractLine = request.getContext().asType(ContractLine.class);

		Contract contract = null;
		if(request.getContext().getParent().getContextClass() == Contract.class){
			contract = request.getContext().getParent().asType(Contract.class);
		}else if (request.getContext().getParent().getContextClass() == ContractVersion.class){
			ContractVersion contractVersion = request.getContext().getParent().asType(ContractVersion.class);
			contract = contractVersion.getContractNext() == null ? contractVersion.getContract() : contractVersion.getContractNext() ;
		}
		Product product = contractLine.getProduct();

		ContractLineService contractLineService = Beans.get(ContractLineService.class);
		try  {
			contractLine = contractLineService.update(contractLine, product);
		    contractLine = contractLineService.computePrice(contractLine, contract, product);
		    response.setValues(contractLine);
		}
		catch (Exception e)  {
			TraceBackService.trace(response, e);
			response.setValues(contractLineService.reset(contractLine));
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response) {
		ContractLine contractLine = request.getContext().asType(ContractLine.class);
		Product product = contractLine.getProduct();

		ContractLineService contractLineService = Beans.get(ContractLineService.class);
		if(product == null) {
			response.setValues(contractLineService.reset(contractLine));
			return;
		}

		contractLine = contractLineService.computeTotal(contractLine, product);
		response.setValues(contractLine);
	}
}
