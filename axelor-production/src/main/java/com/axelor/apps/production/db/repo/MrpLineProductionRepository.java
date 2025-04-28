package com.axelor.apps.production.db.repo;

import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineManagementRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import java.util.Map;

public class MrpLineProductionRepository extends MrpLineManagementRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long mrpLineId = (Long) json.get("id");
    MrpLine mrpLine = find(mrpLineId);

    Map<String, Object> mrpLineMap = super.populate(json, context);
    if (mrpLine.getMrpLineType().getElementSelect()
            == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && mrpLine.getMaturityDate() != null
        && !mrpLine.getMaturityDate().isEqual(mrpLine.getDeliveryDelayDate())) {
      //json.put("respectDeliveryDelayDate", mrpLine.getDeliveryDelayDate());
    }
    return mrpLineMap;
  }
}
