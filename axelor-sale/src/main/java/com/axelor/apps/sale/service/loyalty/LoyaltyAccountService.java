package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;

public interface LoyaltyAccountService {
  LoyaltyAccount getLoyaltyAccount(Partner partner, Company company);
}
