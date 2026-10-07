/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.utils;

import com.axelor.db.JPA;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import java.lang.invoke.MethodHandles;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.resource.transaction.spi.TransactionCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionHelper {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private TransactionHelper() {}

  /**
   * Runs the action once the current transaction is committed. The action is dropped when the
   * transaction is rolled back.
   *
   * @param action the action to run after the commit
   * @throws IllegalStateException when no transaction is active
   */
  public static void runAfterCommit(Runnable action) {
    if (action == null) {
      throw new IllegalArgumentException("Action cannot be null");
    }

    TransactionCoordinator transactionCoordinator =
        JPA.em().unwrap(SessionImplementor.class).getTransactionCoordinator();

    if (!transactionCoordinator.isActive()) {
      throw new IllegalStateException("No active transaction to register a post-commit action");
    }

    transactionCoordinator
        .getLocalSynchronizations()
        .registerSynchronization(new PostCommitSynchronization(action));
  }

  private static class PostCommitSynchronization implements Synchronization {

    private final Runnable action;

    PostCommitSynchronization(Runnable action) {
      this.action = action;
    }

    @Override
    public void beforeCompletion() {
      // Nothing to do before the commit
    }

    @Override
    public void afterCompletion(int status) {
      if (status != Status.STATUS_COMMITTED) {
        return;
      }
      try {
        action.run();
      } catch (Exception e) {
        log.error("Error executing the post-commit action", e);
      }
    }
  }
}
