package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;

import java.time.LocalDate;

public class TeamTaskServiceImpl implements TeamTaskService {

    @Override
    public TeamTask create(String subject, Project project, User assignedTo) {
        TeamTask task = new TeamTask();
        task.setName(subject);
        task.setAssignedTo(assignedTo);
        task.setTaskDate(LocalDate.now());
        task.setStatus("new");
        task.setPriority("normal");
        project.addTeamTaskListItem(task);
        return task;
    }

    @Override
    public TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo) {
        return create(saleOrderLine.getFullName() + "_task", project, assignedTo);
    }
}
