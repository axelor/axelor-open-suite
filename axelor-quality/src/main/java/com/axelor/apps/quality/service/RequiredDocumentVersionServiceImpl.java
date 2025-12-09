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
import java.util.Optional;

public class RequiredDocumentVersionServiceImpl implements RequiredDocumentVersionService {

  protected final RequiredDocumentRepository requiredDocRepo;
  protected final RequiredDocumentStatusService requiredDocumentStatusService;

  @Inject
  public RequiredDocumentVersionServiceImpl(
      RequiredDocumentRepository requiredDocRepo,
      RequiredDocumentStatusService requiredDocumentStatusService) {
    this.requiredDocRepo = requiredDocRepo;
    this.requiredDocumentStatusService = requiredDocumentStatusService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public RequiredDocument createNewVersion(RequiredDocument original) {
    RequiredDocument newVersion = prepareNewVersion(original);
    requiredDocumentStatusService.markObsolete(original);
    return newVersion;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void activateVersion(RequiredDocument requiredDocument) {
    Optional.ofNullable(requiredDocRepo.findActiveVersionBySequence(requiredDocument.getSequence()))
        .ifPresent(requiredDocumentStatusService::markObsolete);
    requiredDocumentStatusService.markAvailable(requiredDocument);
  }

  protected RequiredDocument prepareNewVersion(RequiredDocument original) {
    RequiredDocument copy = requiredDocRepo.copy(original, false);
    copy.setStatusSelect(RequiredDocumentRepository.STATUS_REQUIRED);
    copy.setDocVersion(computeNextVersion(original));
    return requiredDocRepo.save(copy);
  }

  protected int computeNextVersion(RequiredDocument original) {
    RequiredDocument highest = requiredDocRepo.findBySequence(original.getSequence());
    int base =
        Optional.ofNullable(highest)
            .map(RequiredDocument::getDocVersion)
            .orElse(original.getDocVersion());
    return base + 1;
  }
}
