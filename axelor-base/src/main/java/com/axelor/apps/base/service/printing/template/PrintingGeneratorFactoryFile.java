/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplateLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.base.service.printing.template.model.TemplatePrint;
import com.axelor.meta.MetaFiles;
import com.google.common.io.Files;
import java.io.File;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;

public class PrintingGeneratorFactoryFile implements PrintingGeneratorFactory {

  @Override
  public TemplatePrint generate(
      PrintingTemplateLine printTemplateLine, PrintingGenFactoryContext context)
      throws AxelorException {
    TemplatePrint print = new TemplatePrint();
    try {
      Path path = MetaFiles.getPath(printTemplateLine.getMetaFile());
      File output =
          new File(MetaFiles.getPath("tmp").toFile(), FilenameUtils.getName(path.toString()));
      Files.copy(path.toFile(), output);
      print.setPrint(output);
      print.setOutputFormat(FilenameUtils.getExtension(output.toString()));
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    return print;
  }
}
