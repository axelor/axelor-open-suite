/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.manuforder;

import java.util.List;

public class ManufOrderFinalControlResult {

  public enum Status {
    NOT_REQUIRED,
    COMPLIANT,
    NON_COMPLIANT,
    MISSING
  }

  private final Status status;
  private final List<String> nonCompliantEntryNames;
  private final List<String> notControlledEntryNames;
  private final List<String> uncoveredTrackingNumberSeqs;

  public ManufOrderFinalControlResult(Status status) {
    this(status, List.of(), List.of(), List.of());
  }

  public ManufOrderFinalControlResult(
      Status status,
      List<String> nonCompliantEntryNames,
      List<String> notControlledEntryNames,
      List<String> uncoveredTrackingNumberSeqs) {
    this.status = status;
    this.nonCompliantEntryNames = List.copyOf(nonCompliantEntryNames);
    this.notControlledEntryNames = List.copyOf(notControlledEntryNames);
    this.uncoveredTrackingNumberSeqs = List.copyOf(uncoveredTrackingNumberSeqs);
  }

  public Status getStatus() {
    return status;
  }

  public List<String> getNonCompliantEntryNames() {
    return nonCompliantEntryNames;
  }

  public List<String> getNotControlledEntryNames() {
    return notControlledEntryNames;
  }

  public List<String> getUncoveredTrackingNumberSeqs() {
    return uncoveredTrackingNumberSeqs;
  }

  public boolean isCompliant() {
    return status == Status.COMPLIANT;
  }
}
