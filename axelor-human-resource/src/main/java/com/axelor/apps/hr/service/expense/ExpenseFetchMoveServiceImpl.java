package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.hr.db.Expense;
import com.google.inject.Inject;

public class ExpenseFetchMoveServiceImpl implements ExpenseFetchMoveService {
  protected MoveRepository moveRepository;

  @Inject
  public ExpenseFetchMoveServiceImpl(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  @Override
  public Move getExpenseMove(Expense expense) {
    return moveRepository
        .all()
        .filter("self.expense.id = :expenseId")
        .bind("expenseId", expense.getId())
        .fetchOne();
  }

  @Override
  public Move getExpensePaymentMove(Expense expense) {
    return moveRepository
        .all()
        .filter("self.expensePayment.id = :expenseId")
        .bind("expenseId", expense.getId())
        .fetchOne();
  }
}
