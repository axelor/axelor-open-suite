package com.axelor.apps.sale.service.loyalty;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.LoyaltyAccount;
import java.util.Optional;

public class LoyaltyAccountServiceImpl implements LoyaltyAccountService {

  @Override
  public Optional<LoyaltyAccount> getLoyaltyAccount(Partner partner, Company company) {
    Optional<LoyaltyAccount> loyaltyAccount = Optional.empty();
    if (partner != null && company != null) {
      loyaltyAccount =
          partner.getLoyaltyAccountList().stream()
              .filter(account -> company.equals(account.getCompany()))
              .findFirst();
    }
    return loyaltyAccount;
  }
}
