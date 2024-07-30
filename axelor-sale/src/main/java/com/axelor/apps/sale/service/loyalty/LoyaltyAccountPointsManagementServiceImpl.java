package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoyaltyAccountPointsManagementServiceImpl
    implements LoyaltyAccountPointsManagementService {

  protected LoyaltyAccountRepository loyaltyAccountRepository;
  protected LoyaltyAccountServiceImpl loyaltyAccountService;

  @Inject
  public LoyaltyAccountPointsManagementServiceImpl(
      LoyaltyAccountRepository loyaltyAccountRepository,
      LoyaltyAccountServiceImpl loyaltyAccountService) {
    this.loyaltyAccountRepository = loyaltyAccountRepository;
    this.loyaltyAccountService = loyaltyAccountService;
  }

  @Override
  public void incrementLoyaltyPointsFromAmount(
      Partner partner, Company company, BigDecimal amount) {
    LoyaltyAccount loyaltyAccount = loyaltyAccountService.getLoyaltyAccount(partner, company);
    BigDecimal earnedPoints = pointsEarningComputation(amount);
    incrementPoints(loyaltyAccount, earnedPoints);
  }

  @Transactional
  public void updatePoints(LoyaltyAccount loyaltyAccount, BigDecimal points, boolean isIncrement) {
    BigDecimal updatedBalance =
        isIncrement
            ? loyaltyAccount.getPointsBalance().add(points)
            : loyaltyAccount.getPointsBalance().subtract(points);
    loyaltyAccount.setPointsBalance(updatedBalance.setScale(0, RoundingMode.FLOOR));
    loyaltyAccountRepository.save(loyaltyAccount);
  }

  @Override
  public void incrementPoints(LoyaltyAccount loyaltyAccount, BigDecimal points) {
    updatePoints(loyaltyAccount, points, true);
  }

  @Override
  public void decrementPoints(LoyaltyAccount loyaltyAccount, BigDecimal points) {
    updatePoints(loyaltyAccount, points, false);
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
