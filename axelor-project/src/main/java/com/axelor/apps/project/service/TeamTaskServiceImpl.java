package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.team.db.TeamTask;

public class TeamTaskServiceImpl implements TeamTaskService {
    @Override
    public TeamTask create(String subject, Project project) {
        TeamTask task = new TeamTask();
        task.setName(subject);
        project.addTeamTaskListItem(task);
        return task;
    }

    @Override
    public TeamTask create(String subject, Project project, SaleOrderLine saleOrderLine) {
        return create(subject, project);
    }
}
