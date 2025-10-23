package com.axelor.apps.base.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

/**
 * Listener to enforce constraints that a start date/time must not be after its corresponding end
 * date/time.
 */
public class DateConstraintListener {

  /** List of field name pairs (Start Field, End Field) to check for date constraints. */
  private static final List<SimpleEntry<String, String>> DATE_FIELD_PAIRS =
      List.of(
          new SimpleEntry<>("startDate", "endDate"),
          new SimpleEntry<>("fromDate", "toDate"),
          new SimpleEntry<>("startDateTime", "endDateTime"),
          new SimpleEntry<>("start", "stop"),
          new SimpleEntry<>("validFrom", "validTo"),
          new SimpleEntry<>("dateFrom", "dateTo"),
          new SimpleEntry<>("dateStart", "dateEnd"),
          new SimpleEntry<>("deliveryDateFrom", "deliveryDateTo"),
          new SimpleEntry<>("startTime", "endTime"));

  /**
   * Validates date constraints on the given entity. It checks all defined date/time pairs to ensure
   * the end date/time is not before the start date/time.
   *
   * @param entity The object instance to validate.
   * @throws AxelorException if a date constraint is violated.
   */
  public static void validateDateConstraint(Object entity) throws AxelorException {
    for (SimpleEntry<String, String> pair : DATE_FIELD_PAIRS) {
      String startFieldName = pair.getKey();
      String endFieldName = pair.getValue();

      Temporal startTemporal = getTemporalField(entity, startFieldName);
      Temporal endTemporal = getTemporalField(entity, endFieldName);

      if (startTemporal != null && endTemporal != null) {
        if (isBefore(endTemporal, startTemporal)) {

          String entityName = entity.getClass().getSimpleName();
          String errorMessage =
              String.format(
                  "Das zieldatum (%s) darf nicht vor dem Anfangsdatum liegen (%s).",
                  endTemporal, startTemporal);

          throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, errorMessage);
        }
      }
    }
  }

  /**
   * Uses reflection to get the value of a field and casts it to a Temporal type.
   *
   * @param entity The object to read the field from.
   * @param fieldName The name of the field.
   * @return The Temporal value of the field, or null if the field doesn't exist or isn't a
   *     Temporal.
   * @throws RuntimeException if a reflection error occurs other than NoSuchFieldException.
   */
  private static Temporal getTemporalField(Object entity, String fieldName) {
    try {
      Field field = entity.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(entity);
      if (value instanceof Temporal) {
        return (Temporal) value;
      }
      return null;
    } catch (NoSuchFieldException e) {
      return null;
    } catch (Exception e) {
      throw new RuntimeException(
          "Reflection error during date constraint check for " + fieldName, e);
    }
  }

  /**
   * Compares two Temporal objects (LocalDate or LocalDateTime) to check if the end date/time is
   * before the start date/time.
   *
   * @param endTemporal The end date or date/time.
   * @param startTemporal The start date or date/time.
   * @return True if the end date/time is strictly before the start date/time, false otherwise.
   */
  private static boolean isBefore(Temporal endTemporal, Temporal startTemporal) {
    if (endTemporal instanceof LocalDate && startTemporal instanceof LocalDate) {
      return ((LocalDate) endTemporal).isBefore((LocalDate) startTemporal);
    }
    if (endTemporal instanceof LocalDateTime && startTemporal instanceof LocalDateTime) {
      return ((LocalDateTime) endTemporal).isBefore((LocalDateTime) startTemporal);
    }
    if (endTemporal instanceof LocalDate && startTemporal instanceof LocalDateTime) {
      return ((LocalDate) endTemporal).atStartOfDay().isBefore((LocalDateTime) startTemporal);
    }
    if (endTemporal instanceof LocalDateTime && startTemporal instanceof LocalDate) {
      return ((LocalDateTime) endTemporal).isBefore(((LocalDate) startTemporal).atStartOfDay());
    }
    return false;
  }
}
