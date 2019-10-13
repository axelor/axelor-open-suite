/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.optaplanner.examples.projectjobscheduling.domain.solver;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;

public class PredecessorsDoneDateUpdatingVariableListener implements VariableListener<Allocation> {

  @Override
  public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    // Do nothing
  }

  @Override
  public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    updateAllocation(scoreDirector, allocation);
  }

  @Override
  public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
    // Do nothing
  }

  @Override
  public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
    updateAllocation(scoreDirector, allocation);
  }

  @Override
  public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    // Do nothing
  }

  @Override
  public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
    // Do nothing
  }

  protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
    Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
    uncheckedSuccessorQueue.addAll(originalAllocation.getSuccessorAllocationList());
    while (!uncheckedSuccessorQueue.isEmpty()) {
      Allocation allocation = uncheckedSuccessorQueue.remove();
      boolean updated = updatePredecessorsDoneDate(scoreDirector, allocation);
      if (updated) {
        uncheckedSuccessorQueue.addAll(allocation.getSuccessorAllocationList());
      }
    }
  }

  /**
   * @param scoreDirector never null
   * @param allocation never null
   * @return true if the startDate changed
   */
  protected boolean updatePredecessorsDoneDate(ScoreDirector scoreDirector, Allocation allocation) {
    // For the source the doneDate must be 0.
    Integer doneDate = 0;
    for (Allocation predecessorAllocation : allocation.getPredecessorAllocationList()) {
      int endDate = predecessorAllocation.getEndDate();
      doneDate = Math.max(doneDate, endDate);
    }
    if (Objects.equals(doneDate, allocation.getPredecessorsDoneDate())) {
      return false;
    }
    scoreDirector.beforeVariableChanged(allocation, "predecessorsDoneDate");
    allocation.setPredecessorsDoneDate(doneDate);
    scoreDirector.afterVariableChanged(allocation, "predecessorsDoneDate");
    return true;
  }
}
