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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ContractFileServiceImpl implements ContractFileService {
  protected FileRepository contractFileRepository;
  protected DMSFileRepository dmsFileRepository;

  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;

  @Inject
  public ContractFileServiceImpl(
      FileRepository contractFileRepository,
      DMSFileRepository dmsFileRepository,
      MetaFiles metaFiles,
      AppBaseService appBaseService) {
    this.contractFileRepository = contractFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void remove(List<Integer> contractFileIds) {
    for (Integer id : contractFileIds) {
      File contractFile = contractFileRepository.find(Long.parseLong(String.valueOf(id)));
      contractFileRepository.remove(contractFile);
    }
  }
}
