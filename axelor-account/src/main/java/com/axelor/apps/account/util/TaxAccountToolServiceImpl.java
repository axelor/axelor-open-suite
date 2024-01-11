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
package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class TaxAccountToolServiceImpl implements TaxAccountToolService {

  protected AccountingSituationRepository accountingSituationRepository;

  @Inject
  public TaxAccountToolServiceImpl(AccountingSituationRepository accountingSituationRepository) {
    this.accountingSituationRepository = accountingSituationRepository;
  }

  @Override
  public int calculateVatSystem(
      Partner partner, Company company, Account account, boolean isExpense, boolean isSale)
      throws AxelorException {
    AccountingSituation accountingSituation = null;
    if (isExpense) {
      checkExpenseVatSystemPreconditions(partner, company, account);
      accountingSituation = accountingSituationRepository.findByCompanyAndPartner(company, partner);
    } else if (isSale) {
      checkSaleVatSystemPreconditions(partner, company, account);
      accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, company.getPartner());
    }
    if (accountingSituation != null) {
      if (account != null
          && accountingSituation.getVatSystemSelect()
              == AccountingSituationRepository.VAT_COMMON_SYSTEM) {
        return account.getVatSystemSelect();
      } else if (accountingSituation.getVatSystemSelect()
          == AccountingSituationRepository.VAT_DELIVERY) {
        return MoveLineRepository.VAT_COMMON_SYSTEM;
      }
    } else if (account != null) {
      return account.getVatSystemSelect();
    }
    return MoveLineRepository.VAT_SYSTEM_DEFAULT;
  }

  public void checkExpenseVatSystemPreconditions(Partner partner, Company company, Account account)
      throws AxelorException {
    AccountingSituation accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(company, partner);
    if (accountingSituation != null
        && (accountingSituation.getVatSystemSelect() == null
            || accountingSituation.getVatSystemSelect()
                == AccountingSituationRepository.VAT_SYSTEM_DEFAULT)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.ACCOUNTING_SITUATION_VAT_SYSTEM_NOT_FOUND),
          company.getName(),
          partner.getFullName());
    }
    if (account != null
        && (account.getVatSystemSelect() == null
            || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.ACCOUNT_VAT_SYSTEM_NOT_FOUND),
          account.getCode());
    }
  }

  public void checkSaleVatSystemPreconditions(Partner partner, Company company, Account account)
      throws AxelorException {
    if (company.getPartner() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.COMPANY_PARTNER_NOT_FOUND),
          company.getName());
    }
    AccountingSituation accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(company, company.getPartner());
    if (CollectionUtils.isEmpty(company.getPartner().getAccountingSituationList())
        || accountingSituation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.COMPANY_PARTNER_ACCOUNTING_SITUATION_NOT_FOUND),
          company.getName(),
          company.getPartner().getFullName());
    }
    if (accountingSituation.getVatSystemSelect() == null
        || accountingSituation.getVatSystemSelect()
            == AccountingSituationRepository.VAT_SYSTEM_DEFAULT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.COMPANY_PARTNER_VAT_SYSTEM_NOT_FOUND),
          company.getName(),
          company.getPartner().getFullName());
    }
    if (account != null
        && (account.getVatSystemSelect() == null
            || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.ACCOUNT_VAT_SYSTEM_NOT_FOUND),
          account.getCode());
    }
  }
}
