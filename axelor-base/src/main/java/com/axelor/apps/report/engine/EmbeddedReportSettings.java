/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.report.engine;

import com.axelor.apps.base.service.ReportingTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.axelor.report.ReportGenerator;
import java.io.IOException;
import org.eclipse.birt.core.exception.BirtException;

public class EmbeddedReportSettings extends ReportSettings {

  public EmbeddedReportSettings(String rptdesign, String outputName) {

    super(rptdesign, outputName);
  }

  @Override
  public EmbeddedReportSettings generate() throws AxelorException {

    super.generate();

    try {

      final ReportGenerator generator = Beans.get(ReportGenerator.class);

      this.output = generator.generate(rptdesign, format, params, ReportingTool.getCompanyLocale());

      this.attach();

    } catch (IOException | BirtException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    return this;
  }
}
