package com.axelor.apps.businessproject.service.approvalitem;

import com.axelor.apps.businessproject.db.ApprovalItem;
import com.axelor.apps.businessproject.db.repo.ApprovalItemRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.db.Model;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApprovalItemManagementServiceImpl implements ApprovalItemManagementService {

  private static final Logger log =
      LoggerFactory.getLogger(ApprovalItemManagementServiceImpl.class);
  protected ApprovalItemRepository approvalItemRepository;

  @Inject
  public ApprovalItemManagementServiceImpl(ApprovalItemRepository approvalItemRepository) {
    this.approvalItemRepository = approvalItemRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ApprovalItem createApprovalItem(
      Model linkedItemToValidate,
      Project project,
      Employee employee,
      String approvalItemTypeSelect,
      String description,
      String unit,
      LocalDateTime itemDateTime,
      BigDecimal amount) {
    if (linkedItemToValidate == null) return null;

    // Update the relevant fields of the approval item if it already exists.
    if (hasApprovalItem(linkedItemToValidate, approvalItemTypeSelect)) {
      log.debug(
          "Approval item for {} with id {} already exist updating it",
          approvalItemTypeSelect,
          linkedItemToValidate.getId());
      return updateApprovalItem(
          linkedItemToValidate, itemDateTime, description, amount, approvalItemTypeSelect);
    }

    log.debug("Creating Approval Item for a {}", approvalItemTypeSelect);
    ApprovalItem approvalItem = new ApprovalItem();

    setApprovalItemInfo(
        linkedItemToValidate.getId(),
        project,
        employee,
        approvalItemTypeSelect,
        description,
        unit,
        itemDateTime,
        amount,
        approvalItem);

    return approvalItemRepository.save(approvalItem);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ApprovalItem updateApprovalItem(
      Model linkedItemToValidate,
      LocalDateTime itemDateTime,
      String description,
      BigDecimal amount,
      String approvalItemTypeSelect) {
    ApprovalItem approvalItem = getApprovalItem(linkedItemToValidate, approvalItemTypeSelect);
    if (approvalItem == null) return null;

    boolean hasChanged = false;

    if (!Objects.equals(approvalItem.getItemDateTime(), itemDateTime)) {
      approvalItem.setItemDateTime(itemDateTime);
      hasChanged = true;
    }

    if (!Objects.equals(approvalItem.getDescription(), description)) {
      approvalItem.setDescription(description);
      hasChanged = true;
    }

    if (!Objects.equals(approvalItem.getAmount(), amount)) {
      approvalItem.setAmount(amount);
      hasChanged = true;
    }

    if (hasChanged) {
      log.debug(
          "Some relevant changed on corresponding item to validate and have been updated {}: {}",
          linkedItemToValidate.getClass().getName(),
          linkedItemToValidate.getId());
      return approvalItemRepository.save(approvalItem);
    }

    return approvalItem;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deleteApprovalItem(Model itemToValidate, String approvalItemTypeSelect) {
    if (itemToValidate == null) {
      return;
    }

    ApprovalItem approvalItem = getApprovalItem(itemToValidate, approvalItemTypeSelect);

    if (approvalItem != null) {
      log.debug(
          "Deleting approval item for {} with id {} as it is no longer needed",
          itemToValidate.getClass(),
          itemToValidate.getId());
      approvalItemRepository.remove(approvalItem);
    }
  }

  @Override
  public boolean hasApprovalItem(Model itemToValidate, String approvalItemTypeSelect) {
    return approvalItemRepository
            .all()
            .filter(
                "self.linkedItemToValidateId = :itemToValidateId AND self.itemTypeSelect = :itemTypeSelect")
            .bind("itemToValidateId", itemToValidate.getId())
            .bind("itemTypeSelect", approvalItemTypeSelect)
            .count()
        > 0;
  }

  protected void setApprovalItemInfo(
      long linkedItemToValidateId,
      Project project,
      Employee employee,
      String approvalItemTypeSelect,
      String description,
      String unit,
      LocalDateTime itemDateTime,
      BigDecimal amount,
      ApprovalItem approvalItem) {
    approvalItem.setLinkedItemToValidateId(linkedItemToValidateId);
    approvalItem.setProject(project);
    approvalItem.setEmployee(employee);
    approvalItem.setItemTypeSelect(approvalItemTypeSelect);
    approvalItem.setDescription(description);
    approvalItem.setUnit(unit);
    approvalItem.setItemDateTime(itemDateTime);
    approvalItem.setAmount(amount);
  }

  protected ApprovalItem getApprovalItem(Model itemToValidate, String approvalItemTypeSelect) {
    return approvalItemRepository
        .all()
        .filter(
            "self.linkedItemToValidateId = :itemToValidateId AND self.itemTypeSelect = :itemTypeSelect")
        .bind("itemToValidateId", itemToValidate.getId())
        .bind("itemTypeSelect", approvalItemTypeSelect)
        .fetchOne();
  }
}
