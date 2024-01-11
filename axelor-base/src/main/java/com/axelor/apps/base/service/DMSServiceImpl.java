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
package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;

public class DMSServiceImpl implements DMSService {

  protected DMSFileRepository dmsFileRepository;
  protected MetaFiles metaFiles;

  @Inject
  public DMSServiceImpl(DMSFileRepository dmsFileRepository, MetaFiles metaFiles) {
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public void addLinkedDMSFiles(List<? extends Model> entityList, Model entityMerged) {
    DMSFile dmsRoot = getDMSRoot(entityMerged);
    DMSFile dmsHome = getDMSHome(entityMerged, dmsRoot);

    for (Model entity : entityList) {
      List<DMSFile> dmsFileList =
          dmsFileRepository
              .all()
              .filter("self.relatedId = :id AND self.relatedModel = :model")
              .bind("id", entity.getId())
              .bind("model", entity.getClass().getName())
              .fetch();

      for (DMSFile dmsFile : dmsFileList) {
        if (dmsFile.getParent() != null
            && dmsRoot != null
            && dmsFile.getParent().getId() == dmsRoot.getId()) {
          dmsFile.setParent(dmsHome);
        }
        dmsFile.setRelatedId(entityMerged.getId());
        dmsFileRepository.save(dmsFile);
      }
    }
  }

  @Override
  public DMSFile getDMSRoot(Model model) {
    return dmsFileRepository
        .all()
        .filter(
            "COALESCE(self.isDirectory, FALSE) = TRUE AND self.relatedModel = :model AND COALESCE(self.relatedId, 0) = 0")
        .bind("model", model.getClass().getName())
        .fetchOne();
  }

  @Override
  public DMSFile getDMSHome(Model model, DMSFile dmsRoot) {
    String homeName = null;
    final Mapper mapper = Mapper.of(model.getClass());
    homeName = mapper.getNameField().get(model).toString();

    if (homeName == null) {
      homeName = Strings.padStart("" + model.getId(), 5, '0');
    }

    DMSFile dmsHome = new DMSFile();
    dmsHome.setFileName(homeName);
    dmsHome.setRelatedId(model.getId());
    dmsHome.setRelatedModel(model.getClass().getName());
    dmsHome.setParent(dmsRoot);
    dmsHome.setIsDirectory(true);

    return dmsFileRepository.save(dmsHome);
  }
}
