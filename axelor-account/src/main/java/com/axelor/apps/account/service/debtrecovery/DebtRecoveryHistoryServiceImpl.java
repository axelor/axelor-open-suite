package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.util.FileExportTools;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.file.PdfTool;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class DebtRecoveryHistoryServiceImpl implements DebtRecoveryHistoryService {

  protected AppBaseService appBaseService;

  @Inject
  public DebtRecoveryHistoryServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public String printDebtRecoveryHistory(List<Integer> ids) throws IOException, AxelorException {
    List<File> printedDebtRecoveryHistory =
        Query.of(DMSFile.class)
            .filter(
                "self.relatedId IN (:ids) AND self.relatedModel = :model AND self.isDirectory = false AND self.fileName LIKE '%.pdf'")
            .bind("ids", ids).bind("model", DebtRecoveryHistory.class.getName()).fetch().stream()
            .map(dmsFile -> MetaFiles.getPath(dmsFile.getMetaFile()).toFile())
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(printedDebtRecoveryHistory)) {
      return null;
    }

    String fileName =
        I18n.get("Debt recovery history")
            + " - "
            + appBaseService
                .getTodayDate(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
                .format(DateTimeFormatter.BASIC_ISO_DATE)
            + ".pdf";
    return PdfTool.mergePdfToFileLink(printedDebtRecoveryHistory, fileName);
  }

  @Override
  public Optional<Path> zipDebtRecoveryHistoryAttachments(List<Integer> ids) {

    List<File> printedDebtRecoveryHistory =
        Query.of(DMSFile.class)
            .filter(
                "self.relatedId IN (:ids) AND self.relatedModel = :model AND self.isDirectory = false AND self.fileName LIKE '%.pdf'")
            .bind("ids", ids).bind("model", DebtRecoveryHistory.class.getName()).fetch().stream()
            .map(dmsFile -> MetaFiles.getPath(dmsFile.getMetaFile()).toFile())
            .collect(Collectors.toList());

    if (!CollectionUtils.isEmpty(printedDebtRecoveryHistory)) {
      return FileExportTools.zip(
          I18n.get("Debt recovery history")
              + " - "
              + appBaseService
                  .getTodayDate(
                      Optional.ofNullable(AuthUtils.getUser())
                          .map(User::getActiveCompany)
                          .orElse(null))
                  .format(DateTimeFormatter.BASIC_ISO_DATE),
          printedDebtRecoveryHistory);
    }

    return Optional.empty();
  }
}
