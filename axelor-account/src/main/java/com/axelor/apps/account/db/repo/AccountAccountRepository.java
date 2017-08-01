package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

import javax.persistence.PersistenceException;
import java.util.Set;

public class AccountAccountRepository extends AccountRepository {

    @Override
    public Account save(Account account) {
        try {
            if (account.getId() == null) {
                return super.save(account);
            }

            if (account.getReconcileOk()) {
                Set<Account> accountList = account.getCompatibleAccountSet();

                for (Account acc : accountList) {
                    acc.setReconcileOk(true);
                    acc.addCompatibleAccountSetItem(account);
                    JPA.save(acc);
                }
            }

            return super.save(account);
        } catch (Exception e) {
            throw new PersistenceException(e.getLocalizedMessage());
        }
    }
}
