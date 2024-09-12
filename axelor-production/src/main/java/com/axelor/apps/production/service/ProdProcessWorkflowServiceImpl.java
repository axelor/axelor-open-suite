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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProdProcessWorkflowServiceImpl implements ProdProcessWorkflowService {

  protected ProdProcessRepository prodProcessRepo;

  @Inject
  public ProdProcessWorkflowServiceImpl(ProdProcessRepository prodProcessRepo) {
    this.prodProcessRepo = prodProcessRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public ProdProcess setDraftStatus(ProdProcess prodProcess) throws AxelorException {
    if (prodProcess.getStatusSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_NULL_STATUS));
    } else if (prodProcess.getStatusSelect() != null
        && prodProcess.getStatusSelect() == ProdProcessRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_ALREADY_DRAFT_STATUS));
    }
    prodProcess.setStatusSelect(ProdProcessRepository.STATUS_DRAFT);
    return prodProcessRepo.save(prodProcess);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public ProdProcess setValidateStatus(ProdProcess prodProcess) throws AxelorException {
    if (prodProcess.getStatusSelect() == null
        || prodProcess.getStatusSelect() != ProdProcessRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_VALIDATED_WRONG_STATUS));
    }
    prodProcess.setStatusSelect(ProdProcessRepository.STATUS_VALIDATED);
    return prodProcessRepo.save(prodProcess);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public ProdProcess setApplicableStatus(ProdProcess prodProcess) throws AxelorException {
    if (prodProcess.getStatusSelect() == null
        || prodProcess.getStatusSelect() != ProdProcessRepository.STATUS_VALIDATED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_APPLICABLE_WRONG_STATUS));
    }
    prodProcess.setStatusSelect(ProdProcessRepository.STATUS_APPLICABLE);
    return prodProcessRepo.save(prodProcess);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public ProdProcess setObsoleteStatus(ProdProcess prodProcess) throws AxelorException {
    if (prodProcess.getStatusSelect() == null
        || prodProcess.getStatusSelect() != ProdProcessRepository.STATUS_APPLICABLE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_OBSOLETE_WRONG_STATUS));
    }
    prodProcess.setStatusSelect(ProdProcessRepository.STATUS_OBSOLETE);
    return prodProcessRepo.save(prodProcess);
  }
}
