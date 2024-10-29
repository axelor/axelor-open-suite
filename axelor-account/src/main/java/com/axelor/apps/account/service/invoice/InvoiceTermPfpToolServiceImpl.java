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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InvoiceTermPfpToolServiceImpl implements InvoiceTermPfpToolService {

  protected AppBaseService appBaseService;
  protected AccountingSituationService accountingSituationService;
  protected InvoiceTermRepository invoiceTermRepository;

  @Inject
  public InvoiceTermPfpToolServiceImpl(
      AppBaseService appBaseService,
      AccountingSituationService accountingSituationService,
      InvoiceTermRepository invoiceTermRepository) {
    this.appBaseService = appBaseService;
    this.accountingSituationService = accountingSituationService;
    this.invoiceTermRepository = invoiceTermRepository;
  }

  @Override
  public int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getPfpValidateStatusSelect()
        == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED) {
      return InvoiceTermRepository.PFP_STATUS_VALIDATED;
    } else {
      return invoiceTerm.getPfpValidateStatusSelect();
    }
  }

  @Override
  public List<Integer> getAlreadyValidatedStatusList() {
    return Arrays.asList(
        InvoiceTermRepository.PFP_STATUS_NO_PFP,
        InvoiceTermRepository.PFP_STATUS_VALIDATED,
        InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
  }

  @Override
  public boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser =
        currentUser.getIsSuperPfpUser()
            || (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
                && currentUser.equals(invoiceTerm.getPfpValidatorUser()));
    if (isValidUser) {
      return true;
    }
    return validateUser(invoiceTerm, currentUser)
        && this.checkPfpValidatorUser(invoiceTerm)
        && !invoiceTerm.getIsPaid();
  }

  protected boolean validateUser(InvoiceTerm invoiceTerm, User currentUser) {
    if (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())) {
      List<SubstitutePfpValidator> substitutePfpValidatorList =
          invoiceTerm.getPfpValidatorUser().getSubstitutePfpValidatorList();
      LocalDate todayDate = appBaseService.getTodayDate(invoiceTerm.getInvoice().getCompany());

      for (SubstitutePfpValidator substitutePfpValidator : substitutePfpValidatorList) {
        if (substitutePfpValidator.getSubstitutePfpValidatorUser().equals(currentUser)) {
          LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
          LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();
          if (substituteStartDate == null) {
            if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
              return true;
            }
          } else {
            if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
              return true;
            } else if (substituteStartDate.isBefore(todayDate)
                && substituteEndDate.isAfter(todayDate)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkPfpValidatorUser(InvoiceTerm invoiceTerm) {
    return invoiceTerm != null
        && invoiceTerm.getPfpValidatorUser() != null
        && Objects.equals(
            invoiceTerm.getPfpValidatorUser(),
            this.getPfpValidatorUser(invoiceTerm.getPartner(), invoiceTerm.getCompany()));
  }

  @Override
  public User getPfpValidatorUser(Partner partner, Company company) {
    AccountingSituation accountingSituation =
        accountingSituationService.getAccountingSituation(partner, company);
    if (accountingSituation == null) {
      return null;
    }
    return accountingSituation.getPfpValidatorUser();
  }

  @Override
  public boolean isPfpValidatorUser(InvoiceTerm invoiceTerm, User user) {
    return user != null
        && (user.getIsSuperPfpUser()
            || (invoiceTerm != null
                && invoiceTerm.getPfpValidatorUser() != null
                && user.equals(invoiceTerm.getPfpValidatorUser())));
  }
}
