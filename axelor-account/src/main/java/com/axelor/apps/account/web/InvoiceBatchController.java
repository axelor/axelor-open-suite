/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.db.repo.InvoiceBatchRepository;
import com.axelor.apps.account.service.invoice.InvoiceBatchService;
import com.axelor.apps.account.service.invoice.generator.batch.BatchWkf;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InvoiceBatchController {

  @Inject private InvoiceBatchService invoiceBatchService;

  @Inject private InvoiceBatchRepository invoiceBatchRepo;

  /**
   * Lancer le batch de mise à jour de statut.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void actionStatus(ActionRequest request, ActionResponse response) throws AxelorException {

    InvoiceBatch invoiceBatch = request.getContext().asType(InvoiceBatch.class);

    Batch batch = invoiceBatchService.wkf(invoiceBatchRepo.find(invoiceBatch.getId()));

    response.setFlash(batch.getComments());
    response.setReload(true);
  }

  /**
   * Lancer le batch à travers un web service.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void run(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    Batch batch = invoiceBatchService.run((String) context.get("code"));

    Map<String, Object> mapData = new HashMap<String, Object>();
    mapData.put("anomaly", batch.getAnomaly());
    response.setData(mapData);
  }

  /**
   * Appliquer le domaine à la liste de facture à ventiler ou valider.
   *
   * @param request
   * @param response
   */
  public void invoiceSetDomain(ActionRequest request, ActionResponse response) {

    InvoiceBatch invoiceBatch = request.getContext().asType(InvoiceBatch.class);

    switch (invoiceBatch.getActionSelect()) {
      case 1:
        response.setAttr("invoiceSet", "domain", BatchWkf.invoiceQuery(invoiceBatch, true));
        break;
      default:
        response.setAttr("invoiceSet", "domain", BatchWkf.invoiceQuery(invoiceBatch, false));
        break;
    }
  }
}
