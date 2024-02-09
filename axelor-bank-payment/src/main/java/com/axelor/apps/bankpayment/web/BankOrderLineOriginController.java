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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class BankOrderLineOriginController {

  public void showDMSFiles(ActionRequest request, ActionResponse response) {

    BankOrderLineOrigin bankOrderLineOrigin =
        request.getContext().asType(BankOrderLineOrigin.class);
    Map<String, Object> relatedDataMap =
        Beans.get(BankOrderLineOriginService.class).getRelatedDataMap(bankOrderLineOrigin);

    response.setView(
        ActionView.define(I18n.get("Files"))
            .model(DMSFile.class.getName())
            .add("grid", "dms-file-grid")
            .add("form", "dms-file-form")
            .domain(
                "self.relatedModel = :relatedModel AND self.relatedId = :relatedId AND self.isDirectory = false")
            .context("relatedModel", relatedDataMap.get("relatedModel"))
            .context("relatedId", relatedDataMap.get("relatedId"))
            .context("_showSingle", true)
            .map());

    if (request.getContext().get("_source") != null) {
      response.setCanClose(true);
    }
  }

  public void displayDmsFileButton(ActionRequest request, ActionResponse response) {
    try {
      BankOrderLineOrigin bankOrderLineOrigin =
          request.getContext().asType(BankOrderLineOrigin.class);
      if (!Beans.get(BankOrderLineOriginService.class).dmsFilePresent(bankOrderLineOrigin)) {
        response.setAttr("displayInvoiceBtn", "hidden", true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void alertNoDmsFile(ActionRequest request, ActionResponse response) {
    try {
      BankOrderLineOrigin bankOrderLineOrigin =
          request.getContext().asType(BankOrderLineOrigin.class);
      if (!Beans.get(BankOrderLineOriginService.class).dmsFilePresent(bankOrderLineOrigin)) {
        response.setError(I18n.get(BankPaymentExceptionMessage.BANK_ORDER_LINE_ORIGIN_NO_DMS_FILE));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
