package com.axelor.apps.base.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.report.IReport;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintServiceImpl implements PrintService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public String generatePDF(Print print) throws AxelorException {
    String name = I18n.get("PRINT_NOUN") + " " + print.getName();

    return ReportFactory.createReport(IReport.PRINT, name + "-${date}")
        .addParam("PrintId", print.getId())
        .generate()
        .getFileLink();
  }

  @Override
  public void attachMetaFiles(Print print, Set<MetaFile> metaFiles) {
    Preconditions.checkNotNull(print.getId());

    if (metaFiles == null || metaFiles.isEmpty()) {
      return;
    }

    LOG.debug("Add metafiles to object {} : {}", Print.class.getName(), print.getId());

    for (MetaFile metaFile : metaFiles) {
      Beans.get(MetaFiles.class).attach(metaFile, metaFile.getFileName(), print);
    }
  }
}
