package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;
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

  @Inject
  public LoyaltyAccountPointsManagementServiceImpl(
      LoyaltyAccountRepository loyaltyAccountRepository,
      LoyaltyAccountService loyaltyAccountService) {
    this.loyaltyAccountRepository = loyaltyAccountRepository;
    this.loyaltyAccountService = loyaltyAccountService;
  }

  @Override
  public void incrementLoyaltyPointsFromAmount(
      Partner partner, Company company, BigDecimal amount) {
    Optional<LoyaltyAccount> loyaltyAccount =
        loyaltyAccountService.getLoyaltyAccount(partner, company);
    BigDecimal earnedPoints = pointsEarningComputation(amount);
    loyaltyAccount.ifPresent(account -> updatePoints(account, earnedPoints));
  }

  /**
   * Update points balance use negative points to subtract
   *
   * @param loyaltyAccount
   * @param points
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePoints(LoyaltyAccount loyaltyAccount, BigDecimal points) {
    Objects.requireNonNull(loyaltyAccount);

    BigDecimal updatedBalance = loyaltyAccount.getPointsBalance().add(points);
    loyaltyAccount.setPointsBalance(updatedBalance.setScale(0, RoundingMode.FLOOR));
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
