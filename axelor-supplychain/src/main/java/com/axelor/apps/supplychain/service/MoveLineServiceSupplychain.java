package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Batch;
import java.util.List;

public interface MoveLineServiceSupplychain {
  Batch validateCutOffBatch(List<Long> recordIdList, Long batchId);
}
