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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects everything that happened during a product merge, so that it can be written in the merge
 * log and shown to the user.
 */
public class ProductMergeResult {

  protected final Map<String, Long> transferredReferenceMap = new LinkedHashMap<>();
  protected final List<String> informationList = new ArrayList<>();
  protected final List<String> warningList = new ArrayList<>();

  /**
   * Records the number of references transferred from the absorbed product to the kept product.
   *
   * @param reference the transferred reference, as "Object.field"
   */
  public void addTransferredReferences(String reference, long count) {
    if (count <= 0) {
      return;
    }
    transferredReferenceMap.merge(reference, count, Long::sum);
  }

  /** Records what the merge did, for the merge log. */
  public void addInformation(String information) {
    informationList.add(information);
  }

  /** Records something the user has to look at, without blocking the merge. */
  public void addWarning(String warning) {
    warningList.add(warning);
  }

  public Map<String, Long> getTransferredReferenceMap() {
    return Collections.unmodifiableMap(transferredReferenceMap);
  }

  public List<String> getInformationList() {
    return Collections.unmodifiableList(informationList);
  }

  public List<String> getWarningList() {
    return Collections.unmodifiableList(warningList);
  }

  /**
   * Returns the content of the merge log: what has been transferred, what the merge did, then the
   * warnings.
   */
  public List<String> getLogLines() {
    List<String> logLines = new ArrayList<>();
    transferredReferenceMap.forEach(
        (reference, count) ->
            logLines.add(
                String.format(
                    I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_TRANSFERRED_REFERENCES),
                    reference,
                    count)));
    logLines.addAll(informationList);
    logLines.addAll(warningList);
    return logLines;
  }
}
