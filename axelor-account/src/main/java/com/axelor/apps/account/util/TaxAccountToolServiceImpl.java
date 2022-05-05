package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
      if (accountingSituation.getVatSystemSelect()
          == AccountingSituationRepository.VAT_COMMON_SYSTEM) {
        return account.getVatSystemSelect().intValue();
      } else if (accountingSituation.getVatSystemSelect()
          == AccountingSituationRepository.VAT_DELIVERY) {
        return MoveLineRepository.VAT_COMMON_SYSTEM;
      }
    }
    return MoveLineRepository.VAT_SYSTEM_DEFAULT;
  }

  public void checkExpenseVatSystemPreconditions(Partner partner, Company company, Account account)
      throws AxelorException {
    if (partner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.MOVE_PARTNER_FOR_TAX_NOT_FOUND));
    }
    AccountingSituation accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(company, partner);
    if (CollectionUtils.isEmpty(partner.getAccountingSituationList())
        || accountingSituation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNTING_SITUATION_NOT_FOUND),
          company.getName(),
          partner.getFullName());
    }
    if (accountingSituation.getVatSystemSelect() == null
        || accountingSituation.getVatSystemSelect()
            == AccountingSituationRepository.VAT_SYSTEM_DEFAULT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNTING_SITUATION_VAT_SYSTEM_NOT_FOUND),
          company.getName(),
          partner.getFullName());
    }
    if (account.getVatSystemSelect() == null
        || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNT_VAT_SYSTEM_NOT_FOUND),
          account.getCode());
    }
  }

  public void checkSaleVatSystemPreconditions(Partner partner, Company company, Account account)
      throws AxelorException {
    if (company.getPartner() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_NOT_FOUND),
          company.getName());
    }
    AccountingSituation accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(company, company.getPartner());
    if (CollectionUtils.isEmpty(company.getPartner().getAccountingSituationList())
        || accountingSituation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_ACCOUNTING_SITUATION_NOT_FOUND),
          company.getName(),
          company.getPartner().getFullName());
    }
    if (accountingSituation.getVatSystemSelect() == null
        || accountingSituation.getVatSystemSelect()
            == AccountingSituationRepository.VAT_SYSTEM_DEFAULT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_VAT_SYSTEM_NOT_FOUND),
          company.getName(),
          company.getPartner().getFullName());
    }
    if (account.getVatSystemSelect() == null
        || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNT_VAT_SYSTEM_NOT_FOUND),
          account.getCode());
    }
  }
}
