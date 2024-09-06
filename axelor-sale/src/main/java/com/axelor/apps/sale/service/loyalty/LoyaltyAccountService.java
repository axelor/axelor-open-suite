package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.LoyaltyAccount;
import java.math.BigDecimal;
import java.util.Optional;

public interface LoyaltyAccountService {
  Optional<LoyaltyAccount> getLoyaltyAccount(
      Partner partner, Company company, TradingName tradingName);

  LoyaltyAccount acquirePoints(LoyaltyAccount loyaltyAccount, int delay);

  LoyaltyAccount spendOutOfValidityPoints(LoyaltyAccount loyaltyAccount, int period)
      throws AxelorException;

  LoyaltyAccount spendPoints(LoyaltyAccount loyaltyAccount, BigDecimal points)
      throws AxelorException;
}
