package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.db.Currency;

public interface Currenciable {

  Currency getCurrency();

  Currency getCompanyCurrency();
}
