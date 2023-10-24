/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryHistoryService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

  public void printDmsFile(ActionRequest request, ActionResponse response) {
    try {
      DebtRecoveryHistory debtRecoveryHistory =
          request.getContext().asType(DebtRecoveryHistory.class);

      debtRecoveryHistory =
          Beans.get(DebtRecoveryHistoryRepository.class).find(debtRecoveryHistory.getId());

      if (!ObjectUtils.isEmpty(debtRecoveryHistory.getDebtRecovery())) {

        response.setView(
            ActionView.define(I18n.get("Attachments"))
                .model(DMSFile.class.getName())
                .add("grid", "dms-file-grid")
                .add("form", "dms-file-form")
                .domain(
                    "self.relatedModel = :relatedModel AND self.relatedId = :relatedId AND self.isDirectory = false")
                .context("relatedModel", debtRecoveryHistory.getClass().getName())
                .context("relatedId", debtRecoveryHistory.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportDmsFile(ActionRequest request, ActionResponse response) {
    try {

      List<Integer> debtRecoveryHistoryIds = (List<Integer>) request.getContext().get("_ids");

      if (debtRecoveryHistoryIds != null && !debtRecoveryHistoryIds.isEmpty()) {
        Optional<Path> zipPath =
            Beans.get(DebtRecoveryHistoryService.class)
                .zipDebtRecoveryHistoryAttachments(debtRecoveryHistoryIds);

        if (zipPath.isPresent()) {
          response.setExportFile(zipPath.get().toString());
          return;
        } else {
          response.setInfo(I18n.get(AccountExceptionMessage.NO_DEBT_RECOVERY_HISTORY_FILE));
          return;
        }

      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_DEBT_RECOVERY_HISTORY_SELECTED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printMultipleDmsFile(ActionRequest request, ActionResponse response) {
    try {
      List<Integer> debtRecoveryHistoryIds = (List<Integer>) request.getContext().get("_ids");

      if (debtRecoveryHistoryIds != null && !debtRecoveryHistoryIds.isEmpty()) {

        String fileLink =
            Beans.get(DebtRecoveryHistoryService.class)
                .printDebtRecoveryHistory(debtRecoveryHistoryIds);
        if (StringUtils.isEmpty(fileLink)) {
          response.setInfo(I18n.get(AccountExceptionMessage.NO_DEBT_RECOVERY_HISTORY_FILE));
          return;
        }
        String title = I18n.get("Debt recovery history");
        response.setView(ActionView.define(title).add("html", fileLink).map());

      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_DEBT_RECOVERY_HISTORY_SELECTED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
