package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public interface FixedAssetLineToolService {

  /**
   * This method group and sort {@link FixedAsset#getFixedAssetLineList()} and {@link
   * FixedAsset#getFiscalFixedAssetLineList()} by period of [month multiplied by periodicityInMonth]
   * in {@link FixedAssetLine#getDepreciationDate()}. Because it sorted, the method will explicitly
   * return a {@link LinkedHashMap}.
   *
   * @param fixedAsset
   * @return generated {@link LinkedHashMap}
   * @throws NullPointerException if fixedAsset is null
   */
  LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset);
}
