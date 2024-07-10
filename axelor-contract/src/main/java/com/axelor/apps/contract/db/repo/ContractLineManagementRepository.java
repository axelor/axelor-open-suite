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
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.Map;

public class ContractLineManagementRepository extends ContractLineRepository {

  protected InvoiceLineRepository invoiceLineRepository;
  protected CurrencyScaleService currencyScaleService;
  protected AppBaseService appBaseService;

  @Inject
  public ContractLineManagementRepository(
      InvoiceLineRepository invoiceLineRepository,
      CurrencyScaleService currencyScaleService,
      AppBaseService appBaseService) {
    this.invoiceLineRepository = invoiceLineRepository;
    this.currencyScaleService = currencyScaleService;
    this.appBaseService = appBaseService;
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
    try {
      if (context.containsKey("_field")
          && context.get("_field").equals("contractLineList")
          && context.get("_parent") != null) {
        Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");
        Class model = Class.forName((String) _parent.get("_model"));

        if (ContractVersion.class.equals(model)) {
          ContractVersion contractVersion =
              JPA.find(ContractVersion.class, Long.parseLong(_parent.get("id").toString()));
          json.put(
              "$currencyNumberOfDecimals",
              currencyScaleService.getScale(contractVersion.getContract()));
        } else if (ContractTemplate.class.equals(model)) {
          ContractTemplate contractTemplate =
              JPA.find(ContractTemplate.class, Long.parseLong(_parent.get("id").toString()));
          json.put("$currencyNumberOfDecimals", currencyScaleService.getScale(contractTemplate));
        }
      }
    } catch (ClassNotFoundException e) {
      TraceBackService.trace(e);
    }

    AppBase appBase = appBaseService.getAppBase();

    json.put("$nbDecimalDigitForQty", appBase.getNbDecimalDigitForQty());
    json.put("$nbDecimalDigitForUnitPrice", appBase.getNbDecimalDigitForUnitPrice());

    return super.populate(json, context);
  }
}
