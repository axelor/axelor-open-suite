package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;

public interface LoyaltyAccountPointsManagementService {

  void incrementLoyaltyPointsFromAmount(SaleOrder saleOrder);

  void updatePoints(LoyaltyAccount loyaltyAccount, BigDecimal points, SaleOrder saleOrder);

  BigDecimal pointsEarningComputation(BigDecimal amount);
}
