package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.team.db.TeamTask;

public interface TeamTaskService {
    TeamTask create(String subject, Project project, User assignedTo);
    TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo);
}
