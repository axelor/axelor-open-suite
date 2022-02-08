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

import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementLineController {

  @HandleExceptionResponse
  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    LocalDate fromDate = LocalDate.parse(request.getContext().get("fromDate").toString());
    LocalDate toDate = LocalDate.parse(request.getContext().get("toDate").toString());
    Long bankDetail =
        Long.valueOf((Integer) ((Map) request.getContext().get("bankDetails")).get("id"));
    BankDetails bankDetails = Beans.get(BankDetailsRepository.class).find(bankDetail);
    String exportType = (String) request.getContext().get("exportTypeSelect");

    String fileLink =
        Beans.get(BankStatementLineService.class).print(fromDate, toDate, bankDetails, exportType);
    if (StringUtils.notEmpty(fileLink)) {
      response.setView(ActionView.define("Bank statement lines").add("html", fileLink).map());
    }

    response.setReload(true);
  }
}
