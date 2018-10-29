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
package com.axelor.apps;

import com.axelor.apps.report.engine.EmbeddedReportSettings;
import com.axelor.apps.report.engine.ExternalReportSettings;
import com.axelor.apps.report.engine.JasperReportSettings;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.report.ReportResourceLocator;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.birt.report.model.api.IResourceLocator;

import java.net.URL;

public class ReportFactory {

  public static ReportSettings createReport(String rptdesign, String outputName) {
    if (ReportSettings.getReportEngine() == ReportSettings.ReportEngine.JASPER_REPORTS) {
      IResourceLocator locator = new ReportResourceLocator();
      URL reportURL =
          locator.findResource(
              null, FilenameUtils.removeExtension(rptdesign) + ".jrxml", IResourceLocator.OTHERS);
      if (reportURL != null) {
        return new JasperReportSettings(reportURL, outputName);
      }
    }

    if (ReportSettings.useIntegratedEngine()) {

      return new EmbeddedReportSettings(rptdesign, outputName);
    } else {

      return new ExternalReportSettings(rptdesign, outputName);
    }
  }
}
