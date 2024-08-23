package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class LoyaltyAccountPointsManagementServiceImpl
    implements LoyaltyAccountPointsManagementService {

  protected LoyaltyAccountRepository loyaltyAccountRepository;
  protected LoyaltyAccountService loyaltyAccountService;
  protected LoyaltyAccountHistoryLineService loyaltyAccountHistoryLineService;

  @Inject
  public LoyaltyAccountPointsManagementServiceImpl(
      LoyaltyAccountRepository loyaltyAccountRepository,
      LoyaltyAccountService loyaltyAccountService,
      LoyaltyAccountHistoryLineService loyaltyAccountHistoryLineService) {
    this.loyaltyAccountRepository = loyaltyAccountRepository;
    this.loyaltyAccountService = loyaltyAccountService;
    this.loyaltyAccountHistoryLineService = loyaltyAccountHistoryLineService;
  }

  @Override
  public void incrementLoyaltyPointsFromAmount(SaleOrder saleOrder) {
    Optional<LoyaltyAccount> loyaltyAccount =
        loyaltyAccountService.getLoyaltyAccount(
            saleOrder.getClientPartner(), saleOrder.getCompany(), saleOrder.getTradingName());
    BigDecimal earnedPoints = pointsEarningComputation(saleOrder.getExTaxTotal());

    if (earnedPoints.compareTo(BigDecimal.ZERO) != 0) {
      loyaltyAccount.ifPresent(account -> updatePoints(account, earnedPoints, saleOrder));
    }
  }

  /**
   * Update points balance use negative points to subtract
   *
   * @param loyaltyAccount
   * @param points
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePoints(LoyaltyAccount loyaltyAccount, BigDecimal points, SaleOrder saleOrder) {
    Objects.requireNonNull(loyaltyAccount);

    BigDecimal updatedBalance = loyaltyAccount.getFuturePointsBalance().add(points);
    loyaltyAccount.setFuturePointsBalance(updatedBalance.setScale(0, RoundingMode.FLOOR));
    loyaltyAccount.addHistoryLineListItem(
        loyaltyAccountHistoryLineService.createHistoryLine(points, saleOrder));
    loyaltyAccountRepository.save(loyaltyAccount);
  }

  /**
   * get the number of points earned depending on amount
   *
   * @param amount
   * @return
   */
  @Override
  public BigDecimal pointsEarningComputation(BigDecimal amount) {
    return amount.setScale(0, RoundingMode.FLOOR);
  }
}
