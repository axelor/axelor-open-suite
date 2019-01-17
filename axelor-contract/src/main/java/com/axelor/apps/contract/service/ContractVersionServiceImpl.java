/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public class ContractVersionServiceImpl extends ContractVersionRepository
    implements ContractVersionService {

  protected AppBaseService appBaseService;

  @Inject
  public ContractVersionServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public void waiting(ContractVersion version) throws AxelorException {
    waiting(version, appBaseService.getTodayDate());
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void waiting(ContractVersion version, LocalDate date) throws AxelorException {

    Contract contract =
        Stream.of(version.getContract(), version.getNextContract())
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(IExceptionMessage.CONTRACT_MISSING_FROM_VERSION)));

    if (contract.getIsInvoicingManagement()
        && version.getIsPeriodicInvoicing()
        && (contract.getFirstPeriodEndDate() == null || version.getInvoicingDuration() == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CONTRACT_MISSING_FIRST_PERIOD));
    }
    version.setStatusSelect(WAITING_VERSION);
  }

  @Override
  public void ongoing(ContractVersion version) throws AxelorException {
    ongoing(version, appBaseService.getTodayDate());
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void ongoing(ContractVersion version, LocalDate date) throws AxelorException {
    version.setActivationDate(date);
    version.setActivatedByUser(AuthUtils.getUser());
    version.setStatusSelect(ONGOING_VERSION);

    if (version.getVersion() >= 0
        && version.getIsWithEngagement()
        && version.getEngagementStartFromVersion()) {
      Preconditions.checkNotNull(
          version.getContract(), I18n.get("No contract is associated to version."));
      version.getContract().setEngagementStartDate(date);
    }

    if (version.getContract().getIsInvoicingManagement()
        && version.getIsPeriodicInvoicing()
        && (version.getContract().getFirstPeriodEndDate() == null
            || version.getInvoicingDuration() == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please fill the first period end date and the invoice frequency."));
    }

    save(version);
  }

  @Override
  public void terminate(ContractVersion version) {
    terminate(version, appBaseService.getTodayDate());
  }

  @Override
  @Transactional
  public void terminate(ContractVersion version, LocalDate date) {
    version.setEndDate(date);
    version.setStatusSelect(TERMINATED_VERSION);

    save(version);
  }

  @Override
  public ContractVersion newDraft(Contract contract) {
    return copy(contract);
  }
}
