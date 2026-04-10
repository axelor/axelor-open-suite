package com.axelor.apps.base.utils;

import com.axelor.db.Model;
import java.time.LocalDateTime;
import java.util.List;

public interface TimeOverlapValidator {

  /**
   * Check if a time range overlaps with existing records
   *
   * @param entityClass The entity class to check
   * @param startTime Start time of the new record
   * @param endTime End time of the new record
   * @param employeeId The employee/user ID to check against
   * @param currentRecordId ID of current record
   * @param startTimeField Name of the start time field
   * @param endTimeField Name of the end time field
   * @param employeeField Name of the employee field
   * @return List of overlapping records
   */
  <T extends Model> List<T> findOverlapping(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField);

  /** Check if there's any overlap */
  <T extends Model> boolean hasOverlap(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField);

  /** Find a single conflicting record, if any */
  <T extends Model> T findConflictingRecord(
      Class<T> entityClass,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long employeeId,
      Long currentRecordId,
      String startTimeField,
      String endTimeField,
      String employeeField);
}
