package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.ApprovalItem;
import com.axelor.apps.businessproject.db.repo.ApprovalItemRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApprovalItemController {

  private static final Logger log = LoggerFactory.getLogger(ApprovalItemController.class);

  public void openLinkedValidationItem(ActionRequest request, ActionResponse response) {
    try {
      ApprovalItem item = request.getContext().asType(ApprovalItem.class);
      Boolean canCloseParentForm = (Boolean) request.getContext().get("_canCloseParentForm");

      if (!Boolean.FALSE.equals(canCloseParentForm)) {
        response.setCanClose(true);
      }

      if (ApprovalItemRepository.TIMESHEET_LINE_APPROVAL_ITEM.equals(item.getItemTypeSelect())
          && item.getLinkedItemToValidateId() != null
          && item.getLinkedItemToValidateId() != 0) {

        String labelName = getApprovalItemTypeSelectLabel(item.getItemTypeSelect());
        response.setView(
            com.axelor.meta.schema.actions.ActionView.define(I18n.get(labelName))
                .model(TimesheetLine.class.getName())
                .add("form", "mgm-approval-item-timesheet-line-form")
                .param("popup", "true")
                .param("forceEdit", "true")
                .context("_showRecord", item.getLinkedItemToValidateId())
                .map());

      } else if (ApprovalItemRepository.EXPENSE_APPROVAL_ITEM.equals(item.getItemTypeSelect())
          && item.getLinkedItemToValidateId() != null
          && item.getLinkedItemToValidateId() != 0) {

        String labelName = getApprovalItemTypeSelectLabel(item.getItemTypeSelect());
        response.setView(
            com.axelor.meta.schema.actions.ActionView.define(I18n.get(labelName))
                .model(Expense.class.getName())
                .add("form", "mgm-approval-item-expense-form")
                .param("popup", "true")
                .param("forceEdit", "true")
                .context("_showRecord", item.getLinkedItemToValidateId())
                .map());
      }

    } catch (Exception e) {
      response.setError(
          "An error occurred and we could not open the Approval Item: " + e.getMessage());
    }
  }

  public void buildProjectChartDataSet(ActionRequest request, ActionResponse response) {

    List<Object[]> results =
        JPA.em()
            .createQuery(
                "SELECT p.projectStatus.name, p.projectStatus.sequence, COUNT(p) "
                    + "FROM Project p "
                    + "GROUP BY p.projectStatus.name, p.projectStatus.sequence "
                    + "ORDER BY p.projectStatus.sequence",
                Object[].class)
            .getResultList();

    response.setData(buildChartData(results));
  }

  public void buildAccountantProjectChartDataSet(ActionRequest request, ActionResponse response) {
    List<Object[]> results =
        JPA.em()
            .createQuery(
                "SELECT _status.name, _status.sequence, COUNT(self.id) "
                    + "FROM Project self "
                    + "LEFT JOIN self.projectStatus AS _status "
                    + "WHERE UPPER(_status.name) IN ('TO INVOICE', 'INVOICED', 'PAID') "
                    + "GROUP BY _status "
                    + "ORDER BY _status.sequence",
                Object[].class)
            .getResultList();

    response.setData(buildChartData(results));
  }

  public void buildApprovalItemChartDataSet(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();

    List<Object[]> results =
        JPA.em()
            .createQuery(
                "SELECT ai.itemTypeSelect, COUNT(ai) "
                    + "FROM ApprovalItem ai "
                    + "WHERE ai.project.assignedTo = :user "
                    + "   OR :user MEMBER OF ai.project.membersUserSet "
                    + "GROUP BY ai.itemTypeSelect "
                    + "ORDER BY ai.itemTypeSelect",
                Object[].class)
            .setParameter("user", user)
            .getResultList();

    List<Map<String, Object>> data = new ArrayList<>();

    for (Object[] row : results) {

      String label = getApprovalItemTypeSelectLabel((String) row[0]);
      Map<String, Object> map = new HashMap<>();
      map.put("itemType", I18n.get(label));
      map.put("count", row[1]);

      data.add(map);
    }

    response.setData(data);
  }

  /**
   * Refresh browser tab. Refreshes the whole app
   *
   * @param request Request
   * @param response Response
   */
  public void refreshApp(ActionRequest request, ActionResponse response) {
    response.setSignal("refresh-app", null);
  }

  /**
   * Refresh Tab. Refresh the tab of the form from which it's called. If called on a popup form, it
   * will refresh the tab on which the popup is.
   *
   * @param request Request
   * @param response Response
   */
  public void refreshTab(ActionRequest request, ActionResponse response) {
    response.setSignal("refresh-tab", null);
  }

  private List<Map<String, Object>> buildChartData(List<Object[]> rawData) {
    List<Map<String, Object>> data = new ArrayList<>();

    for (Object[] row : rawData) {

      Map<String, Object> map = new HashMap<>();
      map.put("status", I18n.get("value:" + row[0]));
      map.put("count", row[2]);
      data.add(map);
    }

    return data;
  }

  private String getApprovalItemTypeSelectLabel(String value) {
    Option item = MetaStore.getSelectionItem("business.project.approval.item.type.select", value);
    if (item != null) {
      return item.getTitle();
    }

    return "";
  }
}
