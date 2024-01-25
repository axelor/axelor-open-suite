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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class InvoiceTermAccountRepository extends InvoiceTermRepository {
  @Override
  public void remove(InvoiceTerm entity) {
    if (!entity.getIsPaid()) {
      super.remove(entity);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (context.containsKey("_model")
        && context.get("_model").equals(InvoicePayment.class.getName())) {
      long id = (long) json.get("id");
      InvoiceTerm invoiceTerm = this.find(id);
      BigDecimal amountRemaining = BigDecimal.ZERO;

      if (json.containsKey("applyFinancialDiscount")
          && (boolean) json.get("applyFinancialDiscount")) {
        if (json.containsKey("amountRemainingAfterFinDiscount")) {
          amountRemaining = (BigDecimal) json.get("amountRemainingAfterFinDiscount");
        }
      } else if (json.containsKey("amountRemaining")) {
        amountRemaining = (BigDecimal) json.get("amountRemaining");
      }

      json.put("$amountRemaining", amountRemaining);
    }

    long id = (long) json.get("id");
    InvoiceTerm invoiceTerm = this.find(id);
    json.put("$originBasedPaymentDelay", 0);
    if (json.containsKey("amountRemaining")
        && ((BigDecimal) json.get("amountRemaining")).signum() > 0) {
      LocalDate today = Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getCompany());
      if (invoiceTerm.getDueDate() != null) {
        if (invoiceTerm.getDueDate().isBefore(today)) {
          json.put(
              "$originBasedPaymentDelay",
              Duration.between(invoiceTerm.getDueDate().atStartOfDay(), today.atStartOfDay())
                  .toDays());
        }
      } else {
        json.put("$originBasedPaymentDelay", invoiceTerm.getPaymentDelay());
      }
    }

    json.put("$statusSelectPaymentSession", context.get("statusSelect"));
    return super.populate(json, context);
  }

  public List<InvoiceTerm> findByPaymentSessionAndPartner(
      PaymentSession paymentSession, Partner partner) {
    return Query.of(InvoiceTerm.class)
        .filter("self.paymentSession.id = :id AND self.moveLine.partner.id = :partnerId")
        .bind("id", paymentSession.getId())
        .bind("partnerId", partner.getId())
        .fetch();
  }
}
