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
import com.axelor.apps.quality.db.repo.RequiredDocumentRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class RequiredDocumentStatusServiceImpl implements RequiredDocumentStatusService {

  protected final RequiredDocumentRepository requiredDocumentRepository;

  @Inject
  public RequiredDocumentStatusServiceImpl(RequiredDocumentRepository requiredDocumentRepository) {
    this.requiredDocumentRepository = requiredDocumentRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void markAvailable(RequiredDocument requiredDocument) {
    requiredDocument.setStatusSelect(RequiredDocumentRepository.STATUS_AVAILABLE);
    requiredDocument.setIsActiveVersion(true);
    requiredDocumentRepository.save(requiredDocument);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void markObsolete(RequiredDocument requiredDocument) {
    requiredDocument.setStatusSelect(RequiredDocumentRepository.STATUS_OBSOLETE);
    requiredDocument.setIsActiveVersion(false);
    requiredDocumentRepository.save(requiredDocument);
  }
}
