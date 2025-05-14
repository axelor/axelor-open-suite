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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.BirtTemplateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaScanner;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;

public class BirtTemplateViewServiceImpl implements BirtTemplateViewService {

  private BirtTemplateRepository birtTemplateRepo;
  private MetaFiles metaFiles;

  @Inject
  public BirtTemplateViewServiceImpl(BirtTemplateRepository birtTemplateRepo, MetaFiles metaFiles) {
    this.birtTemplateRepo = birtTemplateRepo;
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setTemplateMetaFile(Long birtId, String templateFileName)
      throws AxelorException, IOException {
    BirtTemplate birtTemplate = birtTemplateRepo.find(birtId);
    MetaFile templateFile = getTemplateFile(templateFileName);
    if (ObjectUtils.notEmpty(templateFile)) {
      birtTemplate.setTemplateMetaFile(templateFile);
    }
  }

  @Override
  public MetaFile getTemplateFile(String templateFileName) throws AxelorException, IOException {
    URL url =
        MetaScanner.findAll(templateFileName).stream()
            .filter(u -> templateFileName.equals(FilenameUtils.getName(u.getPath())))
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        String.format(
                            I18n.get(BaseExceptionMessage.FILE_NOT_FOUND_IN_STANDARD_APPLICATION),
                            templateFileName)));
    return metaFiles.upload(url.openStream(), templateFileName);
  }
}
