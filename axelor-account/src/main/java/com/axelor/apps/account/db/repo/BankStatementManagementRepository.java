package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.BankStatement;

public class BankStatementManagementRepository extends BankStatementRepository {
	@Override
	public BankStatement copy(BankStatement entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setStartingBalance(null);
		entity.setEndingBalance(null);
		entity.setComputedBalance(null);
		entity.setBankStatementLineList(null);
		
		return super.copy(entity, deep);
	}
}
