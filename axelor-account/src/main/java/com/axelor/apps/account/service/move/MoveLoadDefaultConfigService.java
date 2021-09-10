package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;

public interface MoveLoadDefaultConfigService {

  Account getAccountingAccountFromAccountConfig(Move move);

  TaxLine getTaxLine(Move move, MoveLine moveLine, Account accountingAccount);
}
