/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.persistence.PersistenceException;

public class InvoiceManagementRepository extends InvoiceRepository {
  @Override
  public Invoice copy(Invoice entity, boolean deep) {

    Invoice copy = super.copy(entity, deep);

    InvoiceToolService.resetInvoiceStatusOnCopy(copy);
    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      invoice = super.save(invoice);
      Beans.get(InvoiceService.class).setDraftSequence(invoice);

      return invoice;
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      if (context.get("_model") != null
          && context.get("_model").toString().contains("SubrogationRelease")) {
        if (context.get("id") != null) {
          long id = (long) context.get("id");
          SubrogationRelease subrogationRelease =
              Beans.get(SubrogationReleaseRepository.class).find(id);
          if (subrogationRelease != null && subrogationRelease.getStatusSelect() != null) {
            json.put("$subrogationStatusSelect", subrogationRelease.getStatusSelect());
          } else {
            json.put("$subrogationStatusSelect", SubrogationReleaseRepository.STATUS_NEW);
          }
        }
      } else {
        json.put("$subrogationStatusSelect", SubrogationReleaseRepository.STATUS_NEW);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }
}
