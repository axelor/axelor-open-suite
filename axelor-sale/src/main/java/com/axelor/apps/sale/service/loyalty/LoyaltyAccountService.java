package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.LoyaltyAccount;
import java.util.Optional;

public interface LoyaltyAccountService {
  Optional<LoyaltyAccount> getLoyaltyAccount(
      Partner partner, Company company, TradingName tradingName);

  LoyaltyAccount acquirePoints(LoyaltyAccount loyaltyAccount, Integer delay);
}
