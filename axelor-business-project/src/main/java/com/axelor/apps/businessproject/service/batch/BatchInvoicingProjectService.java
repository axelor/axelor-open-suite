/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.ProjectInvoicingAssistantBatchRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchInvoicingProjectService extends AbstractBatch {

  protected ProjectRepository projectRepo;

  protected InvoicingProjectService invoicingProjectService;

  @Inject
  public BatchInvoicingProjectService(
      ProjectRepository projectRepo, InvoicingProjectService invoicingProjectService) {
    this.projectRepo = projectRepo;
    this.invoicingProjectService = invoicingProjectService;
  }

  @Override
  protected void process() {

    Map<String, Object> contextValues = null;
    try {
      contextValues = ProjectInvoicingAssistantBatchService.createJsonContext(batch);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    List<Object> generatedInvoicingProjectList = new ArrayList<Object>();

    List<Project> projectList =
        projectRepo
            .all()
            .filter(
                "self.isBusinessProject = true "
                    + "AND self.toInvoice = true AND "
                    + "self.projectStatus.isCompleted = false")
            .fetch();

    for (Project project : projectList) {
      try {
        InvoicingProject invoicingProject =
            invoicingProjectService.generateInvoicingProject(
                project, batch.getProjectInvoicingAssistantBatch().getConsolidatePhaseSelect());

        if (invoicingProject != null && invoicingProject.getId() != null) {
          incrementDone();
          if (batch.getProjectInvoicingAssistantBatch().getActionSelect()
              == ProjectInvoicingAssistantBatchRepository.ACTION_GENERATE_INVOICING_PROJECT) {
            invoicingProject.setDeadlineDate(
                batch.getProjectInvoicingAssistantBatch().getDeadlineDate());
          }

          Map<String, Object> map = new HashMap<String, Object>();
          map.put("id", invoicingProject.getId());
          generatedInvoicingProjectList.add(map);
        }
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            e,
            String.format(
                I18n.get(BusinessProjectExceptionMessage.BATCH_INVOICING_PROJECT_1),
                project.getId()),
            batch.getId());
      }
    }
    ProjectInvoicingAssistantBatchService.updateJsonObject(
        batch, generatedInvoicingProjectList, "generatedInvoicingProjectSet", contextValues);
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            "\t* %s " + I18n.get(BusinessProjectExceptionMessage.BATCH_INVOICING_PROJECT_2) + "\n",
            batch.getDone());

    comment +=
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    addComment(comment);
    super.stop();
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PROJECT_INVOICING_ASSISTANT_BATCH);
  }
}
