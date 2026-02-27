package com.axelor.apps.businessproject.service.approvalitem;

import com.axelor.apps.businessproject.db.ApprovalItem;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.db.Model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ApprovalItemManagementService {

  /**
   * Creates a corresponding approval item for the itemToValidate model passed to it. If a
   * corresponding Approval item already exist it simply updates it. An approval item is a model
   * within the MGM project management workflow which requires validation for the process to
   * continue.
   *
   * @param itemToValidate The Item which needs validation
   * @param project The project the itemToValidate is linked to
   * @param employee The employee the itemToValidate is linked to
   * @param approvalItemTypeSelect The type of approval item (Use the ApprovalItemRepository
   *     constants to get more accurate types)
   * @param description The description field of the itemToValidate
   * @param unit The unit of measurement for the itemToValidate
   * @param itemDateTime Any relevant dateTime field on the itemToValidate
   * @param amount The relevant amount considered on the itemToValidate
   * @return Created Approval item
   */
  ApprovalItem createApprovalItem(
      Model itemToValidate,
      Project project,
      Employee employee,
      String approvalItemTypeSelect,
      String description,
      String unit,
      LocalDateTime itemDateTime,
      BigDecimal amount);

  /**
   * Updates some relevant fields of an approval item if they changed on the linkedItemToValidate
   *
   * @param linkedItemToValidate Item which already has a corresponding approval item
   * @param itemDateTime the updated date time
   * @param description Updated description
   * @param amount Updated amount
   * @return Updated Approval Item
   */
  ApprovalItem updateApprovalItem(
      Model linkedItemToValidate,
      LocalDateTime itemDateTime,
      String description,
      BigDecimal amount);

  /**
   * Deletes the corresponding approval item for the itemToValidate passed to it.
   *
   * @param itemToValidate Validation item for which to delete its corresponding approval item
   */
  void deleteApprovalItem(Model itemToValidate);

  /**
   * Checks if a Validation item has an existing corresponding Approval Item
   *
   * @param itemToValidate Validation Item to check for
   * @return True if a corresponding Approval Item exists else False
   */
  boolean hasApprovalItem(Model itemToValidate);
}
