package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.inject.Beans;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportJournal {
    public Object importAccountType(Object bean, Map<String, Object> values) {
        assert bean instanceof Journal;
        Journal journal = (Journal) bean;

        // Only 'Manual misc ops' Journal
        if (journal.getImportId() != 24) { return bean; }

        Set<AccountType> accountTypesSet = new HashSet<AccountType>(Beans.get(AccountTypeRepository.class).all().fetch());
        journal.setValidAccountTypeSet(accountTypesSet);

        return journal;
    }
}
