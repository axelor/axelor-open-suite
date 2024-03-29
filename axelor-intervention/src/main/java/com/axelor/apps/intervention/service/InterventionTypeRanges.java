package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.InterventionType;
import com.axelor.apps.intervention.db.OrderedRange;
import com.axelor.apps.intervention.db.Range;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class InterventionTypeRanges {

  private final Long advancedStartupMonitoringRangeId;
  private final Collection<Long> headerRangeIds;
  private final Collection<Long> equipmentRangeIds;
  private final Collection<Long> footerRangeIds;

  private InterventionTypeRanges(
      Long advancedStartupMonitoringRangeId,
      Collection<Long> headerRangeIds,
      Collection<Long> equipmentRangeIds,
      Collection<Long> footerRangeIds) {
    this.advancedStartupMonitoringRangeId = advancedStartupMonitoringRangeId;
    this.headerRangeIds = headerRangeIds;
    this.equipmentRangeIds = equipmentRangeIds;
    this.footerRangeIds = footerRangeIds;
  }

  public Long getAdvancedStartupMonitoringRangeId() {
    return advancedStartupMonitoringRangeId;
  }

  public Collection<Long> getHeaderRangeIds() {
    return headerRangeIds;
  }

  public Collection<Long> getEquipmentRangeIds() {
    return equipmentRangeIds;
  }

  public Collection<Long> getFooterRangeIds() {
    return footerRangeIds;
  }

  public static InterventionTypeRanges of(InterventionType interventionType) {
    return new InterventionTypeRanges(
        Optional.ofNullable(interventionType.getAdvancedStartupMonitoringRange())
            .map(Range::getId)
            .orElse(null),
        Optional.ofNullable(interventionType.getHeaderRangeList()).orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .map(OrderedRange::getRangeVal)
            .filter(Objects::nonNull)
            .map(Range::getId)
            .collect(Collectors.toList()),
        Optional.ofNullable(interventionType.getEquipmentRangeList())
            .orElse(Collections.emptyList()).stream()
            .filter(Objects::nonNull)
            .map(OrderedRange::getRangeVal)
            .filter(Objects::nonNull)
            .map(Range::getId)
            .collect(Collectors.toList()),
        Optional.ofNullable(interventionType.getFooterRangeList()).orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .map(OrderedRange::getRangeVal)
            .filter(Objects::nonNull)
            .map(Range::getId)
            .collect(Collectors.toList()));
  }
}
