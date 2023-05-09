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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.db.ProjectFolder;
import com.axelor.apps.businessproject.report.IReport;
import com.axelor.apps.businessproject.report.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.ModelTool;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.file.PdfTool;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectFolderServiceImpl implements ProjectFolderService {

  @Override
  public String printProjectsPlanificationAndCost(ProjectFolder projectFolder)
      throws IOException, AxelorException {
    List<File> printedProjects = new ArrayList<>();
    List<Long> ids = new ArrayList<Long>();

    for (Project project : projectFolder.getProjectSet()) {
      ids.add(project.getId());
    }
    ModelTool.apply(
        Project.class,
        ids,
        new ThrowConsumer<Project, Exception>() {
          @Override
          public void accept(Project project) throws Exception {
            String name = I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_PLANIFICATION_AND_COST);
            if (project.getCode() != null) {
              name += " (" + project.getCode() + ")";
            }
            printedProjects.add(printCopiesToFile(project, name, IReport.PLANNIF_AND_COST));
          }
        });

    String fileName =
        getProjectFilesName(I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_PLANIFICATION_AND_COST));
    return PdfTool.mergePdfToFileLink(printedProjects, fileName);
  }

  @Override
  public String printProjectFinancialReport(ProjectFolder projectFolder)
      throws IOException, AxelorException {
    List<File> printedProjects = new ArrayList<>();
    List<Long> ids = new ArrayList<Long>();

    for (Project project : projectFolder.getProjectSet()) {
      ids.add(project.getId());
    }
    ModelTool.apply(
        Project.class,
        ids,
        new ThrowConsumer<Project, Exception>() {
          @Override
          public void accept(Project project) throws Exception {
            String name =
                I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_FINANCIAL) + " " + project.getCode();
            printedProjects.add(printCopiesToFile(project, name, IReport.PROJECT));
          }
        });

    String fileName =
        getProjectFilesName(I18n.get(ITranslation.PROJECT_REPORT_TITLE_FOR_FINANCIAL));
    return PdfTool.mergePdfToFileLink(printedProjects, fileName);
  }

  public File printCopiesToFile(Project project, String name, String reportDesignName)
      throws AxelorException, IOException {
    File file = print(project, name, reportDesignName);
    return PdfTool.printCopiesToFile(file, 1);
  }

  public File print(Project project, String name, String reportDesignName) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(project, name, reportDesignName);
    return reportSettings.generate().getFile();
  }

  protected ReportSettings prepareReportSettings(
      Project project, String name, String reportDesignName) {

    ReportSettings reportSetting = ReportFactory.createReport(reportDesignName, name);

    return reportSetting
        .addParam("ProjectId", project.getId())
        .addParam(
            "Timezone", project.getCompany() != null ? project.getCompany().getTimezone() : null)
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addFormat(ReportSettings.FORMAT_PDF);
  }

  protected String getProjectFilesName(String fileName) {
    return fileName
        + " - "
        + Beans.get(AppBaseService.class)
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + ReportSettings.FORMAT_PDF;
  }
}
