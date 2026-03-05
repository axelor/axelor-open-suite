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
package com.axelor.apps.base.service.administration;

/**
 * Immutable result of a sequence increment operation.
 *
 * <p>Contains the sequence version ID and the next number that was reserved during the increment
 * operation in an isolated transaction.
 */
public final class IncrementResult {

  private final Long sequenceVersionId;
  private final Long nextNum;

  public IncrementResult(Long sequenceVersionId, Long nextNum) {
    this.sequenceVersionId = sequenceVersionId;
    this.nextNum = nextNum;
  }

  /**
   * Gets the ID of the SequenceVersion that was used for this increment.
   *
   * @return the sequence version ID
   */
  public Long getSequenceVersionId() {
    return sequenceVersionId;
  }

  /**
   * Gets the reserved next number value.
   *
   * @return the next number that was reserved
   */
  public Long getNextNum() {
    return nextNum;
  }

  @Override
  public String toString() {
    return "IncrementResult{sequenceVersionId=" + sequenceVersionId + ", nextNum=" + nextNum + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IncrementResult that = (IncrementResult) o;
    return java.util.Objects.equals(sequenceVersionId, that.sequenceVersionId)
        && java.util.Objects.equals(nextNum, that.nextNum);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(sequenceVersionId, nextNum);
  }
}
