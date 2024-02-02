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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.ImportFECType;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.InputStream;

public class ImportFECTypeServiceImpl implements ImportFECTypeService {

  protected MetaFiles metaFiles;

  @Inject
  public ImportFECTypeServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setStandardBindingfile(ImportFECType importFecType, String bindingfileName)
      throws AxelorException, IOException {
    MetaFile bindingFile = getBindMetaFile(bindingfileName);
    if (ObjectUtils.notEmpty(bindingFile)) {
      importFecType.setBindMetaFile(bindingFile);
    }
  }

  @Override
  public MetaFile getBindMetaFile(String bindingfileName) throws AxelorException, IOException {
    InputStream bindFileInputStream =
        this.getClass().getResourceAsStream("/FEC-config/" + bindingfileName);

    if (ObjectUtils.isEmpty(bindFileInputStream)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.FILE_NOT_FOUND_IN_STANDARD_APPLICATION),
              bindingfileName));
    }
    return metaFiles.upload(bindFileInputStream, bindingfileName);
  }
}
