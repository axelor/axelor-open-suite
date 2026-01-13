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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.RequiredDocument;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class RequiredDocumentFileOnChangeServiceImpl
    implements RequiredDocumentFileOnChangeService {

  protected final RequiredDocumentDMSService requiredDocumentDMSService;
  protected final RequiredDocumentStatusService requiredDocumentStatusService;

  @Inject
  public RequiredDocumentFileOnChangeServiceImpl(
      RequiredDocumentDMSService requiredDocumentDMSService,
      RequiredDocumentStatusService requiredDocumentStatusService) {
    this.requiredDocumentDMSService = requiredDocumentDMSService;
    this.requiredDocumentStatusService = requiredDocumentStatusService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void onChange(RequiredDocument requiredDocument) {
    requiredDocumentDMSService.setDMSFile(requiredDocument);
    if (requiredDocument.getMetaFile() != null) {
      requiredDocumentStatusService.markAvailable(requiredDocument);
    }
  }
}
