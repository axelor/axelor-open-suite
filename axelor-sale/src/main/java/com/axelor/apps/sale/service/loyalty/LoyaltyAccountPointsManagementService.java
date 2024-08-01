package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;
import java.math.BigDecimal;

public interface LoyaltyAccountPointsManagementService {

  void incrementLoyaltyPointsFromAmount(Partner partner, Company company, BigDecimal amount);

  void updatePoints(LoyaltyAccount loyaltyAccount, BigDecimal points);

  BigDecimal pointsEarningComputation(BigDecimal amount);
}
