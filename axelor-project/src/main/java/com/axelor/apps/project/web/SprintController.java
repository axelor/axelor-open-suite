package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.roadmap.SprintGeneratorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SprintController {

  public void generateSprints(ActionRequest request, ActionResponse response) {

    Object projectContext = request.getContext().get("project");
    Object projectVersionContext = request.getContext().get("targetVersion");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Integer numberDaysContext = (Integer) request.getContext().get("numberDays");

    Long projectId =
        projectContext != null
            ? Long.valueOf(((LinkedHashMap<String, Object>) projectContext).get("id").toString())
            : null;
    Long projectVersionId =
        projectVersionContext != null
            ? Long.valueOf(
                ((LinkedHashMap<String, Object>) projectVersionContext).get("id").toString())
            : null;

    if ((projectId == null && projectVersionId == null)
        || fromDateContext == null
        || toDateContext == null
        || LocalDate.parse(fromDateContext.toString())
            .isAfter(LocalDate.parse(toDateContext.toString()))
        || numberDaysContext <= 0) {
      response.setError(I18n.get(ProjectExceptionMessage.SPRINT_FIELDS_MISSING));
      return;
    }

    LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
    LocalDate toDate = LocalDate.parse(toDateContext.toString());

    Set<Sprint> sprintSet =
        Beans.get(SprintGeneratorService.class)
            .generateSprints(projectId, projectVersionId, fromDate, toDate, numberDaysContext);

    if (CollectionUtils.isNotEmpty(sprintSet)) {
      response.setInfo(
          String.format(I18n.get(ProjectExceptionMessage.SPRINT_GENERATED), sprintSet.size()));
      response.setCanClose(true);
    }
  }

  public void initDefaultValues(ActionRequest request, ActionResponse response) {
    Object projectContext = request.getContext().get("project");
    Object targetVersionContext = request.getContext().get("targetVersion");

    Long projectId =
        projectContext != null
            ? Long.valueOf(((LinkedHashMap<String, Object>) projectContext).get("id").toString())
            : null;
    Long projectVersionId =
        targetVersionContext != null
            ? Long.valueOf(
                ((LinkedHashMap<String, Object>) targetVersionContext).get("id").toString())
            : null;

    response.setValues(
        Beans.get(SprintGeneratorService.class).initDefaultValues(projectId, projectVersionId));
  }
}
