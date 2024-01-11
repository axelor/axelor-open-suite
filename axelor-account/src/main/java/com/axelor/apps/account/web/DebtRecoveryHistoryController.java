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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.repo.DebtRecoveryHistoryRepository;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DebtRecoveryHistoryController {

  public void printPaymentReminder(ActionRequest request, ActionResponse response) {
    try {
      DebtRecoveryHistory debtRecoveryHistory =
          request.getContext().asType(DebtRecoveryHistory.class);

      debtRecoveryHistory =
          Beans.get(DebtRecoveryHistoryRepository.class).find(debtRecoveryHistory.getId());

      if (!ObjectUtils.isEmpty(debtRecoveryHistory.getDebtRecovery())) {
        String name = I18n.get("Payment reminder") + " " + debtRecoveryHistory.getName();

        String fileLink =
            ReportFactory.createReport(IReport.DEBT_RECOVERY, name + "-${date}")
                .addParam("DebtRecoveryHistoryID", debtRecoveryHistory.getId())
                .addParam("Locale", ReportSettings.getPrintingLocale(null))
                .addFormat("pdf")
                .addParam(
                    "Timezone",
                    debtRecoveryHistory.getDebtRecovery().getCompany() != null
                        ? debtRecoveryHistory.getDebtRecovery().getCompany().getTimezone()
                        : null)
                .toAttach(debtRecoveryHistory)
                .generate()
                .getFileLink();

        response.setView(ActionView.define(name).add("html", fileLink).map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
