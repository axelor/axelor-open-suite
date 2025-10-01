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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.FreightCarrierCustomerAccountNumber;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogisticalFormServiceImpl implements LogisticalFormService {

  @Override
  public String getStockMoveDomain(LogisticalForm logisticalForm) throws AxelorException {

    if (logisticalForm.getDeliverToCustomerPartner() == null) {
      return "self IS NULL";
    }

    List<String> domainList = new ArrayList<>();

    domainList.add("self.company = :company");
    domainList.add("self.partner = :deliverToCustomerPartner");
    domainList.add(String.format("self.typeSelect = %d", StockMoveRepository.TYPE_OUTGOING));
    domainList.add(
        String.format(
            "self.statusSelect in (%d, %d)",
            StockMoveRepository.STATUS_PLANNED, StockMoveRepository.STATUS_REALIZED));
    domainList.add("COALESCE(self.fullySpreadOverLogisticalFormsFlag, FALSE) = FALSE");
    if (logisticalForm.getStockLocation() != null) {
      domainList.add("self.stockMoveLineList.fromStockLocation = :stockLocation");
    }

    return domainList.stream()
        .map(domain -> String.format("(%s)", domain))
        .collect(Collectors.joining(" AND "));
  }

  @Override
  public Optional<String> getCustomerAccountNumberToCarrier(LogisticalForm logisticalForm)
      throws AxelorException {
    Preconditions.checkNotNull(logisticalForm);
    List<FreightCarrierCustomerAccountNumber> freightCarrierCustomerAccountNumberList = null;

    switch (logisticalForm.getAccountSelectionToCarrierSelect()) {
      case LogisticalFormRepository.ACCOUNT_COMPANY:
        if (logisticalForm.getCompany() != null
            && logisticalForm.getCompany().getStockConfig() != null) {
          freightCarrierCustomerAccountNumberList =
              logisticalForm
                  .getCompany()
                  .getStockConfig()
                  .getFreightCarrierCustomerAccountNumberList();
        }
        break;
      case LogisticalFormRepository.ACCOUNT_CUSTOMER:
        if (logisticalForm.getDeliverToCustomerPartner() != null) {
          freightCarrierCustomerAccountNumberList =
              logisticalForm
                  .getDeliverToCustomerPartner()
                  .getFreightCarrierCustomerAccountNumberList();
        }
        break;
      default:
        throw new AxelorException(
            logisticalForm,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.LOGISTICAL_FORM_UNKNOWN_ACCOUNT_SELECTION));
    }

    if (freightCarrierCustomerAccountNumberList != null) {
      Optional<FreightCarrierCustomerAccountNumber> freightCarrierCustomerAccountNumber =
          freightCarrierCustomerAccountNumberList.stream()
              .filter(it -> it.getCarrierPartner().equals(logisticalForm.getCarrierPartner()))
              .findFirst();

      if (freightCarrierCustomerAccountNumber.isPresent()) {
        return Optional.ofNullable(
            freightCarrierCustomerAccountNumber.get().getCustomerAccountNumber());
      }
    }

    return Optional.empty();
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void carrierValidate(LogisticalForm logisticalForm) throws AxelorException {
    if (logisticalForm.getStatusSelect() == null
        || logisticalForm.getStatusSelect() != LogisticalFormRepository.STATUS_PROVISION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_CARRIER_VALIDATE_WRONG_STATUS));
    }
    logisticalForm.setStatusSelect(LogisticalFormRepository.STATUS_CARRIER_VALIDATED);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void backToProvision(LogisticalForm logisticalForm) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(LogisticalFormRepository.STATUS_CARRIER_VALIDATED);
    authorizedStatus.add(LogisticalFormRepository.STATUS_COLLECTED);
    if (logisticalForm.getStatusSelect() == null
        || !authorizedStatus.contains(logisticalForm.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_PROVISION_WRONG_STATUS));
    }
    logisticalForm.setStatusSelect(LogisticalFormRepository.STATUS_PROVISION);
  }
}
