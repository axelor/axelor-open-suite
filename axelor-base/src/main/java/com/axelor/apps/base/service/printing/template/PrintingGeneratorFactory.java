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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplateLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.printing.template.model.TemplatePrint;
import com.axelor.common.FileUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface PrintingGeneratorFactory {

  public TemplatePrint generate(PrintingTemplateLine printTemplateLine, Model model)
      throws AxelorException;

  default File renameFile(File file, PrintingTemplateLine printTemplateLine) throws IOException {
    String fileName =
        String.format(
            "%s-%s.%s",
            FileUtils.stripExtension(file.getName()),
            printTemplateLine.getSequence(),
            FileUtils.getExtension(file.getName()));
    Path src = file.toPath();
    Path dest = Files.move(src, src.resolveSibling(fileName), StandardCopyOption.REPLACE_EXISTING);
    return dest.toFile();
  }

  static PrintingGeneratorFactory getFactory(PrintingTemplateLine line) throws AxelorException {

    Integer typeSelect = line.getTypeSelect();
    Class<? extends PrintingGeneratorFactory> printingGeneratorFactory =
        Beans.get(PrintingGeneratorFactoryProvider.class).get(typeSelect);

    if (printingGeneratorFactory == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FACTORY_NO_FOUND));
    }

    return Beans.get(printingGeneratorFactory);
  }
}
