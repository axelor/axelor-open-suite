/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.supplierdispute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;

public class SupplierDisputeWorkflowServiceImpl implements SupplierDisputeWorkflowService {

  protected SupplierDisputeRepository supplierDisputeRepository;
  protected AppBaseService appBaseService;

  @Inject
  public SupplierDisputeWorkflowServiceImpl(
      SupplierDisputeRepository supplierDisputeRepository, AppBaseService appBaseService) {
    this.supplierDisputeRepository = supplierDisputeRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void investigate(SupplierDispute supplierDispute) throws AxelorException {
    checkStatus(
        supplierDispute,
        SupplierDisputeRepository.STATUS_OPEN,
        SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE);

    supplierDispute.setStatusSelect(SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION);
    supplierDispute.setInvestigationByUser(AuthUtils.getUser());
    supplierDispute.setInvestigationDateTime(getTodayDateTime(supplierDispute));
    supplierDisputeRepository.save(supplierDispute);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void awaitSupplierResponse(SupplierDispute supplierDispute) throws AxelorException {
    checkStatus(
        supplierDispute,
        SupplierDisputeRepository.STATUS_OPEN,
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION);

    supplierDispute.setStatusSelect(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE);
    supplierDispute.setAwaitingResponseByUser(AuthUtils.getUser());
    supplierDispute.setAwaitingResponseDateTime(getTodayDateTime(supplierDispute));
    supplierDisputeRepository.save(supplierDispute);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void resolve(SupplierDispute supplierDispute) throws AxelorException {
    checkStatus(
        supplierDispute,
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
        SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE);

    if (supplierDispute.getResolutionOutcomeSelect() == null
        || supplierDispute.getResolutionOutcomeSelect() == 0) {
      throw new AxelorException(
          supplierDispute,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(
              SupplychainExceptionMessage.SUPPLYCHAIN_SUPPLIER_DISPUTE_MISSING_RESOLUTION_OUTCOME));
    }

    supplierDispute.setStatusSelect(SupplierDisputeRepository.STATUS_RESOLVED);
    supplierDispute.setResolutionByUser(AuthUtils.getUser());
    supplierDispute.setResolutionDateTime(getTodayDateTime(supplierDispute));
    supplierDisputeRepository.save(supplierDispute);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void close(SupplierDispute supplierDispute) throws AxelorException {
    checkStatus(supplierDispute, SupplierDisputeRepository.STATUS_RESOLVED);

    supplierDispute.setStatusSelect(SupplierDisputeRepository.STATUS_CLOSED);
    supplierDispute.setClosingByUser(AuthUtils.getUser());
    supplierDispute.setClosingDateTime(getTodayDateTime(supplierDispute));
    supplierDisputeRepository.save(supplierDispute);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(SupplierDispute supplierDispute) throws AxelorException {
    checkStatus(
        supplierDispute,
        SupplierDisputeRepository.STATUS_OPEN,
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
        SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE);

    supplierDispute.setStatusSelect(SupplierDisputeRepository.STATUS_CANCELLED);
    supplierDispute.setCancellationByUser(AuthUtils.getUser());
    supplierDispute.setCancellationDateTime(getTodayDateTime(supplierDispute));
    supplierDisputeRepository.save(supplierDispute);
  }

  protected void checkStatus(SupplierDispute supplierDispute, Integer... allowedStatuses)
      throws AxelorException {
    Integer statusSelect = supplierDispute.getStatusSelect();
    if (statusSelect == null || !Arrays.asList(allowedStatuses).contains(statusSelect)) {
      throw new AxelorException(
          supplierDispute,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SUPPLYCHAIN_SUPPLIER_DISPUTE_WRONG_STATUS),
          supplierDispute.getDisputeSeq());
    }
  }

  protected LocalDateTime getTodayDateTime(SupplierDispute supplierDispute) {
    return appBaseService.getTodayDateTime(supplierDispute.getCompany()).toLocalDateTime();
  }
}
