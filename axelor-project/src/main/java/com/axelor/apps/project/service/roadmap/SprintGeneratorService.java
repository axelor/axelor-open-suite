package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Sprint;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface SprintGeneratorService {
  Map<String, Object> initDefaultValues(Long projectId, Long projectVersionId);

  Set<Sprint> generateSprints(
      Long projectId,
      Long projectVersionId,
      LocalDate fromDate,
      LocalDate toDate,
      Integer numberDays);
}
