/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.google.inject.Inject;
import java.util.Map;

public class ContractLineManagementRepository extends ContractLineRepository {

  protected InvoiceLineRepository invoiceLineRepository;

  @Inject
  public ContractLineManagementRepository(InvoiceLineRepository invoiceLineRepository) {
    this.invoiceLineRepository = invoiceLineRepository;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (context.containsKey("additionalBenefitContractLineList")
        && context.get("_field").equals("additionalBenefitContractLineList")
        && json.get("id") != null) {
      Long contractLineId = (Long) json.get("id");
      InvoiceLine invoiceLine =
          invoiceLineRepository.all().filter("self.contractLine.id=?", contractLineId).fetchOne();

      if (invoiceLine != null) {
        Invoice invoice = invoiceLine.getInvoice();
        json.put("invoiceId", invoice.getInvoiceId());
        json.put("subscriptionFromDate", invoice.getSubscriptionFromDate());
        json.put("subscriptionToDate", invoice.getSubscriptionToDate());

        String statusSelect =
            I18n.get(
                MetaStore.getSelectionItem(
                        "iaccount.invoice.status.select", invoice.getStatusSelect().toString())
                    .getTitle());
        json.put("statusSelect", statusSelect);
      }
    }
    return super.populate(json, context);
  }
}
