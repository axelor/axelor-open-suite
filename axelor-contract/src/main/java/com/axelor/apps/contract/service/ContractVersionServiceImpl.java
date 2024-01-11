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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.AbstractContractVersionRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ContractVersionServiceImpl extends ContractVersionRepository
    implements ContractVersionService {

  protected AppBaseService appBaseService;

  @Inject
  public ContractVersionServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void waiting(ContractVersion version) throws AxelorException {
    waiting(
        version,
        appBaseService.getTodayDate(
            version.getContract() != null
                ? version.getContract().getCompany()
                : Optional.ofNullable(AuthUtils.getUser())
                    .map(User::getActiveCompany)
                    .orElse(null)));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void waiting(ContractVersion version, LocalDate date) throws AxelorException {

    if (version.getStatusSelect() == null
        || version.getStatusSelect() != AbstractContractVersionRepository.DRAFT_VERSION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_WAITING_WRONG_STATUS));
    }

    Contract contract =
        Stream.of(version.getContract(), version.getNextContract())
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(ContractExceptionMessage.CONTRACT_MISSING_FROM_VERSION)));

    if (contract.getIsInvoicingManagement()
        && version.getIsPeriodicInvoicing()
        && (contract.getFirstPeriodEndDate() == null || version.getInvoicingDuration() == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ContractExceptionMessage.CONTRACT_MISSING_FIRST_PERIOD));
    }
    version.setStatusSelect(WAITING_VERSION);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void ongoing(ContractVersion version) throws AxelorException {
    ongoing(
        version,
        appBaseService
            .getTodayDateTime(
                version.getContract() != null
                    ? version.getContract().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
            .toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void ongoing(ContractVersion version, LocalDateTime dateTime) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(AbstractContractVersionRepository.WAITING_VERSION);
    authorizedStatus.add(AbstractContractVersionRepository.DRAFT_VERSION);
    if (version.getStatusSelect() == null
        || !authorizedStatus.contains(version.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_ONGOING_WRONG_STATUS));
    }

    version.setActivationDateTime(dateTime);
    version.setActivatedByUser(AuthUtils.getUser());
    version.setStatusSelect(ONGOING_VERSION);

    if (version.getVersion() != null
        && version.getVersion() >= 0
        && version.getIsWithEngagement()
        && version.getEngagementStartFromVersion()) {
      Preconditions.checkNotNull(
          version.getContract(), I18n.get("No contract is associated to version."));
      version.getContract().setEngagementStartDate(dateTime.toLocalDate());
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
  @Transactional(rollbackOn = {Exception.class})
  public void terminate(ContractVersion version) throws AxelorException {
    terminate(
        version,
        appBaseService
            .getTodayDateTime(
                version.getContract() != null
                    ? version.getContract().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
            .toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void terminate(ContractVersion version, LocalDateTime dateTime) throws AxelorException {

    if (version.getStatusSelect() == null
        || version.getStatusSelect() != AbstractContractVersionRepository.ONGOING_VERSION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_TERMINATE_WRONG_STATUS));
    }

    version.setEndDateTime(dateTime);
    version.setStatusSelect(TERMINATED_VERSION);

    save(version);
  }

  @Override
  public ContractVersion newDraft(Contract contract) {
    return copy(contract);
  }
}
