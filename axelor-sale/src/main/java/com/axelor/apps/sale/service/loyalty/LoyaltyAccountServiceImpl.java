package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.LoyaltyAccountHistoryLine;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoyaltyAccountServiceImpl implements LoyaltyAccountService {

  protected final AppBaseService appBaseService;
  protected final AppSaleService appSaleService;
  protected final LoyaltyAccountHistoryLineService loyaltyAccountHistoryLineService;

  @Inject
  public LoyaltyAccountServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      LoyaltyAccountHistoryLineService loyaltyAccountHistoryLineService) {
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.loyaltyAccountHistoryLineService = loyaltyAccountHistoryLineService;
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
  public LoyaltyAccount acquirePoints(LoyaltyAccount loyaltyAccount, int delay) {
    loyaltyAccount.getHistoryLineList().stream()
        .filter(
            historyLine ->
                !historyLine.getPointsAcquired()
                    && historyLine
                        .getSaleOrder()
                        .getConfirmationDateTime()
                        .plusDays(delay)
                        .isBefore(appBaseService.getTodayDateTime().toLocalDateTime()))
        .forEach(
            historyLine -> {
              BigDecimal pointsBalance = historyLine.getPointsBalance();
              historyLine.setRemainingPoints(pointsBalance);
              historyLine.setAcquisitionDateTime(
                  appBaseService.getTodayDateTime().toLocalDateTime());
              historyLine.setPointsAcquired(true);
              loyaltyAccount.setPointsBalance(
                  loyaltyAccount.getPointsBalance().add(historyLine.getPointsBalance()));
            });
    return loyaltyAccount;
  }

  @Override
  public LoyaltyAccount spendOutOfValidityPoints(LoyaltyAccount loyaltyAccount, int period)
      throws AxelorException {
    BigDecimal outOfValidityPoints =
        loyaltyAccount.getHistoryLineList().stream()
            .filter(
                historyLine ->
                    historyLine.getPointsAcquired()
                        && historyLine
                            .getAcquisitionDateTime()
                            .plusMonths(period)
                            .isBefore(appBaseService.getTodayDateTime().toLocalDateTime()))
            .map(LoyaltyAccountHistoryLine::getRemainingPoints)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    spendPoints(loyaltyAccount, outOfValidityPoints);
    return loyaltyAccount;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LoyaltyAccount spendPoints(LoyaltyAccount loyaltyAccount, BigDecimal points)
      throws AxelorException {
    if (points.compareTo(loyaltyAccount.getPointsBalance()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(SaleExceptionMessage.LOYALTY_ACCOUNT_NOT_ENOUGH_BALANCE_POINTS),
              loyaltyAccount.getId()),
          loyaltyAccount.getId());
    }

    List<LoyaltyAccountHistoryLine> historyLineList =
        loyaltyAccount.getHistoryLineList().stream()
            .filter(
                historyLine ->
                    historyLine.getPointsAcquired()
                        && historyLine.getRemainingPoints().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(LoyaltyAccountHistoryLine::getAcquisitionDateTime))
            .collect(Collectors.toList());

    BigDecimal remainingPoints = points;
    for (LoyaltyAccountHistoryLine historyLine : historyLineList) {
      if (remainingPoints.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      BigDecimal pointsToUse = historyLine.getRemainingPoints();
      if (remainingPoints.compareTo(pointsToUse) > 0) {
        loyaltyAccountHistoryLineService.spendAllPoints(historyLine);
        loyaltyAccount.setPointsBalance(loyaltyAccount.getPointsBalance().subtract(pointsToUse));
        loyaltyAccount.setFuturePointsBalance(
            loyaltyAccount.getPointsBalance().subtract(pointsToUse));
        remainingPoints = remainingPoints.subtract(pointsToUse);
      } else {
        loyaltyAccountHistoryLineService.spendPoints(historyLine, remainingPoints);
        loyaltyAccount.setPointsBalance(
            loyaltyAccount.getPointsBalance().subtract(remainingPoints));
        loyaltyAccount.setFuturePointsBalance(
            loyaltyAccount.getPointsBalance().subtract(remainingPoints));
        remainingPoints = BigDecimal.ZERO;
      }
    }

    return loyaltyAccount;
  }
}
