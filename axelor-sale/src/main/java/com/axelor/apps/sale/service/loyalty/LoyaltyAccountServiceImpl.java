package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;

public class LoyaltyAccountServiceImpl implements LoyaltyAccountService {

  @Override
  public LoyaltyAccount getLoyaltyAccount(Partner partner, Company company) {
    LoyaltyAccount loyaltyAccount = null;
    if (partner != null && company != null) {
      loyaltyAccount =
          partner.getLoyaltyAccountList().stream()
              .filter(account -> company.equals(account.getCompany()))
              .findFirst()
              .orElse(null);
    }
    return loyaltyAccount;
  }
}
