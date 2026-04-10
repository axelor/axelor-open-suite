package com.axelor.apps.base.utils;

import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import java.time.LocalDateTime;
import java.util.List;

public class TimeOverlapValidatorImpl implements TimeOverlapValidator {

  @Override
  public <T extends Model> List<T> findOverlapping(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField) {

    if (startTime == null || endTime == null || employeeId == null) {
      return List.of();
    }

    // Get the repository for the entity class
    JpaRepository<T> repo = JpaRepository.of(entityClass);

    String filter =
        String.format(
            "self.%s.id = :employeeId AND self.%s < :endTime AND self.%s > :startTime",
            employeeField, startTimeField, endTimeField);

    // Exclude current record if editing so we don't check if a record overlaps with itself
    if (currentRecordId != null) {
      filter += " AND self.id != :currentRecordId";
    }

    var query =
        repo.all()
            .filter(filter)
            .bind("employeeId", employeeId)
            .bind("startTime", startTime)
            .bind("endTime", endTime);

    if (currentRecordId != null) {
      query.bind("currentRecordId", currentRecordId);
    }

    return query.fetch();
  }

  public <T extends Model> boolean hasOverlap(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField) {
    return !findOverlapping(
            entityClass,
            startTime,
            endTime,
            employeeId,
            currentRecordId,
            startTimeField,
            endTimeField,
            employeeField)
        .isEmpty();
  }

  @Override
  public <T extends Model> T findConflictingRecord(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField) {
    List<T> conflicts =
        findOverlapping(
            entityClass,
            startTime,
            endTime,
            employeeId,
            currentRecordId,
            startTimeField,
            endTimeField,
            employeeField);

    return conflicts.isEmpty() ? null : conflicts.get(0);
  }
}
