package com.axelor.apps.contract.service.attributes;

import com.axelor.apps.contract.db.Contract;
import java.util.Map;

public interface ContractLineAttrsService {

  Map<String, Map<String, Object>> setScaleAndPrecision(Contract contract);
}
