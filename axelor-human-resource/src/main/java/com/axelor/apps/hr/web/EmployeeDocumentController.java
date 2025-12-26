package com.axelor.apps.hr.web;

import com.axelor.dms.db.DMSFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EmployeeDocumentController {

  public void showEmployeeDocuments(ActionRequest request, ActionResponse response) {
    String domain =
        "self.id IN ("
            + "  SELECT child.id FROM DMSFile child"
            + "  LEFT JOIN child.parent parent1"
            + "  LEFT JOIN parent1.parent parent2"
            + "  LEFT JOIN parent2.parent parent3"
            + "  WHERE "
            + "    (child.relatedModel = 'com.axelor.apps.hr.db.Employee' AND child.relatedId IS NOT NULL)"
            + "    OR (parent1.relatedModel = 'com.axelor.apps.hr.db.Employee' AND parent1.relatedId IS NOT NULL)"
            + "    OR (parent2.relatedModel = 'com.axelor.apps.hr.db.Employee' AND parent2.relatedId IS NOT NULL)"
            + "    OR (parent3.relatedModel = 'com.axelor.apps.hr.db.Employee' AND parent3.relatedId IS NOT NULL)"
            + ")";

    response.setView(
        ActionView.define("Employee Documents")
            .model(DMSFile.class.getName())
            .add("grid", "dms-file-grid")
            .add("form", "dms-file-form")
            .param("ui-template:grid", "dms-file-list")
            .param("search-filters", "dms-file-filters")
            .domain(domain)
            .map());
  }
}
