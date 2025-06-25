package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MoveGroupOnChangeServiceImpl implements MoveGroupOnChangeService {

  private MoveRecordSetService moveRecordSetService;
  private MoveAttrsService moveAttrsService;

  @Inject
  public MoveGroupOnChangeServiceImpl(
      MoveRecordSetService moveRecordSetService, MoveAttrsService moveAttrsService) {
    this.moveRecordSetService = moveRecordSetService;
    this.moveAttrsService = moveAttrsService;
  }

  @Override
  public Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveRecordSetService.setCompanyBankDetails(move);

    valuesMap.put("companyBankDetails", move.getCompanyBankDetails());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getHeaderChangeAttrsMap() {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveAttrsService.addHeaderChangeValue(true, attrsMap);

    return attrsMap;
  }
}
