package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class LoyaltyAccountServiceImpl implements LoyaltyAccountService {

  protected final AppSaleService appSaleService;

  @Inject
  public LoyaltyAccountServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public Optional<LoyaltyAccount> getLoyaltyAccount(
      Partner partner, Company company, TradingName tradingName) {
    switch (appSaleService.getAppSale().getLoyaltyAccountsManagement()) {
      case LoyaltyAccountRepository.LOYALTY_ACCOUNT_MANAGED_ON_COMPANY:
        return getLoyaltyAccount(partner, company);
      case LoyaltyAccountRepository.LOYALTY_ACCOUNT_MANAGED_ON_TRADING_NAME:
        return getLoyaltyAccount(partner, tradingName);
      case LoyaltyAccountRepository.LOYALTY_ACCOUNT_MANAGED_ON_PARTNER:
        return getLoyaltyAccount(partner);
      default:
        return Optional.empty();
    }
  }

  protected Optional<LoyaltyAccount> getLoyaltyAccount(Partner partner, Company company) {
    Optional<LoyaltyAccount> loyaltyAccount = Optional.empty();
    if (partner != null && company != null) {
      loyaltyAccount =
          partner.getLoyaltyAccountList().stream()
              .filter(account -> company.equals(account.getCompany()))
              .findFirst();
    }
    return loyaltyAccount;
  }

  protected Optional<LoyaltyAccount> getLoyaltyAccount(Partner partner, TradingName tradingName) {
    Optional<LoyaltyAccount> loyaltyAccount = Optional.empty();
    if (partner != null && tradingName != null) {
      loyaltyAccount =
          partner.getLoyaltyAccountList().stream()
              .filter(account -> tradingName.equals(account.getTradingName()))
              .findFirst();
    }
    return loyaltyAccount;
  }

  protected Optional<LoyaltyAccount> getLoyaltyAccount(Partner partner) {
    Optional<LoyaltyAccount> loyaltyAccount = Optional.empty();
    if (partner != null) {
      loyaltyAccount = partner.getLoyaltyAccountList().stream().findFirst();
    }
    return loyaltyAccount;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LoyaltyAccount acquirePoints(LoyaltyAccount loyaltyAccount, LocalDateTime limitDateTime) {
    loyaltyAccount.getHistoryLineList().stream()
        .filter(
            historyLine ->
                !historyLine.getPointsAcquired()
                    && historyLine.getSaleOrder().getConfirmationDate().isAfter(limitDateTime))
        .forEach(
            historyLine -> {
              BigDecimal pointsBalance = historyLine.getPointsBalance();
              historyLine.setRemainingPoints(pointsBalance);
              historyLine.setAcquisitionDateTime(LocalDateTime.now());
              historyLine.setPointsAcquired(true);
              loyaltyAccount.setPointsBalance(
                  loyaltyAccount.getPointsBalance().add(historyLine.getPointsBalance()));
            });
    return loyaltyAccount;
  }
}
