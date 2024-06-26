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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Picture;
import com.axelor.apps.intervention.db.repo.PictureRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.rest.dto.EquipmentPicturePutRequest;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EquipmentRestServiceImpl implements EquipmentRestService {
  protected final MetaFileRepository metaFileRepository;
  protected final PictureRepository pictureRepository;

  @Inject
  public EquipmentRestServiceImpl(
      MetaFileRepository metaFileRepository, PictureRepository pictureRepository) {
    this.metaFileRepository = metaFileRepository;
    this.pictureRepository = pictureRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void addPicture(EquipmentPicturePutRequest request, Equipment equipment)
      throws AxelorException {
    equipment.addPictureListItem(createPictureFromRequest(request, equipment));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removePicture(EquipmentPicturePutRequest request, Equipment equipment)
      throws AxelorException {
    Picture picture = pictureRepository.find(request.getPictureId());
    if (picture == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(InterventionExceptionMessage.INTERVENTION_API_PICTURE_NOT_FOUND),
              request.getPictureId()));
    }
    equipment.removePictureListItem(picture);
  }

  protected Picture createPictureFromRequest(
      EquipmentPicturePutRequest request, Equipment equipment) throws AxelorException {
    MetaFile pictureFile = metaFileRepository.find(request.getPictureId());
    if (pictureFile == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(InterventionExceptionMessage.INTERVENTION_API_PICTURE_NOT_FOUND),
              request.getPictureId()));
    }

    Picture picture = new Picture();
    picture.setEquipment(equipment);
    picture.setPictureFile(pictureFile);
    return picture;
  }
}
