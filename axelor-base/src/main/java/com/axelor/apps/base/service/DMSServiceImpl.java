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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.interfaces.PdfViewer;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;

public class DMSServiceImpl implements DMSService {

  protected DMSFileRepository dmsFileRepository;
  protected MetaFiles metaFiles;

  @Inject
  public DMSServiceImpl(DMSFileRepository dmsFileRepository, MetaFiles metaFiles) {
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public DMSFile setDmsFile(MetaFile metaFile, PdfViewer pdfViewer) {
    if (metaFile == null) {
      pdfViewer.setDmsFile(null);

      DMSFile previousDmsFile = pdfViewer.getDmsFile();
      if (previousDmsFile != null) {
        dmsFileRepository.remove(previousDmsFile);
      }
      return null;
    }
    DMSFile dmsFile = metaFiles.attach(metaFile, metaFile.getFileName(), (Model) pdfViewer);
    pdfViewer.setDmsFile(dmsFile);
    return dmsFile;
  }

  @Override
  public String getInlineUrl(DMSFile dmsFile) {
    if (dmsFile == null) {
      return "";
    }
    return String.format("ws/dms/inline/%d", dmsFile.getId());
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
  public void unzip(String zipFilePath, Model model) throws AxelorException {
    try {
      Map<String, DMSFile> dmsParentsMap = new HashMap<>();
      DMSFile dmsRoot = getDMSRoot(model);
      DMSFile dmsHome = getDMSHome(model, dmsRoot);
      dmsParentsMap.put(File.separator, dmsHome);

      ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();
        Path entryNamePath = Paths.get(entryName);
        String fileName = entryNamePath.getFileName().toString();
        String parentPath =
            Optional.ofNullable(entryNamePath.getParent()).map(Path::toString).orElse("")
                + File.separator;
        DMSFile currentFile;
        if (entry.isDirectory()) {
          currentFile = getDMSFolder(model, fileName, dmsParentsMap.get(parentPath));
        } else {
          MetaFile metaFile = metaFiles.upload(createFile(zis));
          currentFile =
              addDMSFileToParentFolder(model, fileName, dmsParentsMap.get(parentPath), metaFile);
        }
        dmsParentsMap.put(entryName, currentFile);
        zis.closeEntry();
      }
      zis.close();
    } catch (IOException ioe) {
      throw new AxelorException(
          ioe, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, ioe.getLocalizedMessage());
    }
  }

  protected File createFile(InputStream inputStream) throws IOException {
    File file = File.createTempFile("tmp", "");
    OutputStream outputStream = new FileOutputStream(file);
    IOUtils.copy(inputStream, outputStream);
    return file;
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

  public DMSFile getDMSHome(Model model, DMSFile dmsRoot) {
    String homeName = null;
    final Mapper mapper = Mapper.of(model.getClass());
    homeName = mapper.getNameField().get(model).toString();

    if (homeName == null) {
      homeName = Strings.padStart("" + model.getId(), 5, '0');
    }

    return getDMSFolder(model, homeName, dmsRoot);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public DMSFile getDMSFolder(Model model, String fileName, DMSFile dmsRoot) {
    DMSFile dmsHome = new DMSFile();
    dmsHome.setFileName(fileName);
    dmsHome.setRelatedId(model.getId());
    dmsHome.setRelatedModel(EntityHelper.getEntityClass(model).getName());
    dmsHome.setParent(dmsRoot);
    dmsHome.setIsDirectory(true);

    return dmsFileRepository.save(dmsHome);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public DMSFile addDMSFileToParentFolder(
      Model model, String fileName, DMSFile dmsFolder, MetaFile metaFile) {
    DMSFile dmsFile = new DMSFile();
    dmsFile.setFileName(fileName);
    dmsFile.setMetaFile(metaFile);
    dmsFile.setRelatedId(model.getId());
    dmsFile.setRelatedModel(EntityHelper.getEntityClass(model).getName());
    dmsFile.setParent(dmsFolder);

    return dmsFileRepository.save(dmsFile);
  }
}
