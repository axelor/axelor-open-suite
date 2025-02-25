package com.axelor.apps.production.model.machine;

import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;

import java.util.Objects;

/**
 * Represent a pair of priority (int) and workCenter
 */
public class WorkCenterPriorityPair {

    private final WorkCenter workCenter;
    private final Integer priority;

    public WorkCenterPriorityPair(Integer priority, WorkCenter workCenter) {
        this.workCenter = Objects.requireNonNull(workCenter);
        this.priority = Objects.requireNonNull(priority);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkCenterPriorityPair) {
            WorkCenterPriorityPair other = (WorkCenterPriorityPair) obj;
            if (!workCenter.getWorkCenterTypeSelect().equals(other.workCenter.getWorkCenterTypeSelect())
            || workCenter.getWorkCenterTypeSelect().equals(WorkCenterRepository.WORK_CENTER_TYPE_HUMAN)) {
                //Type are different or Human so it depends on priority.
                return priority.equals(other.priority);
            }
            //Past this point, both are same type and not human.
            Machine machine = this.workCenter.getMachine();
            Machine otherMachine = other.workCenter.getMachine();

            return machine.equals(otherMachine) && priority.equals(other.priority);
        }
        return false;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public Integer getPriority() {
        return priority;
    }

    @Override
    public int hashCode() {
        return workCenter.hashCode() + priority.hashCode();
    }
}
