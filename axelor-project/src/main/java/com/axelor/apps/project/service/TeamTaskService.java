package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.team.db.TeamTask;

public interface TeamTaskService {
    TeamTask create(String subject, Project project);
    TeamTask create(String subject, Project project, SaleOrderLine saleOrderLine);
}
