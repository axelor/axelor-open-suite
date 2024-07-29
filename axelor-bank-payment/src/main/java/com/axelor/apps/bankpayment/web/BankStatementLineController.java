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

import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLinePrintAFB120Service;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.PrintingTemplateRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementLineController {

  public void print(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      LocalDate fromDate = LocalDate.parse(context.get("fromDate").toString());
      LocalDate toDate = LocalDate.parse(context.get("toDate").toString());
      Long bankDetail = Long.valueOf((Integer) ((Map) context.get("bankDetails")).get("id"));
      BankDetails bankDetails = Beans.get(BankDetailsRepository.class).find(bankDetail);
      PrintingTemplate bankStatementLinesPrintTemplate = null;
      if (context.get("bankStatementLinesPrintTemplate") != null) {
        bankStatementLinesPrintTemplate =
            Mapper.toBean(
                PrintingTemplate.class,
                (Map<String, Object>) context.get("bankStatementLinesPrintTemplate"));
        bankStatementLinesPrintTemplate =
            Beans.get(PrintingTemplateRepository.class)
                .find(bankStatementLinesPrintTemplate.getId());
      }

      String fileLink =
          Beans.get(BankStatementLinePrintAFB120Service.class)
              .print(fromDate, toDate, bankDetails, bankStatementLinesPrintTemplate);
      if (StringUtils.notEmpty(fileLink)) {
        response.setView(
            ActionView.define(I18n.get("Bank statement lines")).add("html", fileLink).map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }
}
