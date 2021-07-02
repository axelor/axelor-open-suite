/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class BankStatementController {

	protected BankStatementService bankStatementService;
	protected BankStatementLineRepository bankStatementLineRepository;
	protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;

	@Inject
	public BankStatementController(BankStatementService bankStatementService,
			BankStatementLineRepository bankStatementLineRepository,
			BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {

		this.bankStatementService = bankStatementService;
		this.bankStatementLineRepository = bankStatementLineRepository;
		this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
	}

	public void runImport(ActionRequest request, ActionResponse response) {
		try {
			BankStatement bankStatement = request.getContext().asType(BankStatement.class);
			bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
			bankStatementService.runImport(bankStatement, true);
			List<BankStatementLineAFB120> bankStatementLines = bankStatementLineAFB120Repository.all()
					.filter("self.lineTypeSelect = :lineTypeSelect AND self.bankStatement = :bankStatement")
					.bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT)
					.bind("bankStatement", bankStatement).fetch();
			for (BankStatementLineAFB120 bankStatementLineAFB120 : bankStatementLines) {
				bankStatementService.generateMove(bankStatementLineAFB120, null);
				// TODO link moveline 2 to bankStatementLine
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		response.setReload(true);
	}

	public void print(ActionRequest request, ActionResponse response) {
		try {
			BankStatement bankStatement = request.getContext().asType(BankStatement.class);
			bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
			String name = bankStatement.getName();
			String fileLink = Beans.get(BankStatementService.class).print(bankStatement);
			response.setView(ActionView.define(name).add("html", fileLink).map());

		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}

		response.setReload(true);
	}

	public void runBankReconciliation(ActionRequest request, ActionResponse response) {

		try {
			BankStatement bankStatement = request.getContext().asType(BankStatement.class);
			bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
			List<BankReconciliation> bankReconciliationList = Beans.get(BankReconciliationCreateService.class)
					.createAllFromBankStatement(bankStatement);

			if (bankReconciliationList != null) {
				response.setView(ActionView.define(I18n.get("Bank reconciliations"))
						.model(BankReconciliation.class.getName()).add("grid", "bank-reconciliation-grid")
						.add("form", "bank-reconciliation-form").param("search-filters", "bank-reconciliation-filters")
						.domain("self.id in (" + Joiner.on(",")
								.join(bankReconciliationList.stream().map(BankReconciliation::getId).toArray()) + ")")
						.map());
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		response.setReload(true);
	}
}
