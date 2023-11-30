package com.axelor.apps.intervention.repo;

import com.axelor.apps.intervention.db.EquipmentModel;
import com.axelor.apps.intervention.db.repo.EquipmentModelRepository;
import com.axelor.apps.intervention.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.Map;
import javax.persistence.PersistenceException;

public class InterventionEquipmentModelRepository extends EquipmentModelRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      final EquipmentModel entity = find((Long) json.get("id"));
      if (entity != null
          && entity.getTypeSelect().equals(EquipmentRepository.INTERVENTION_TYPE_EQUIPMENT)) {
        json.put(
            "_inService",
            Boolean.TRUE.equals(entity.getInService())
                ? I18n.get("In service")
                : I18n.get("Out of service"));
      } else {
        json.put("_inService", "---");
      }
    }
    return json;
  }

  @Override
  public void remove(EquipmentModel entity) {
    if (all().filter("self.parentEquipmentModel = :parent").bind("parent", entity).count() > 0) {
      throw new PersistenceException(
          I18n.get(IExceptionMessage.EQUIPMENT_MODEL_REMOVE_NOT_ALLOWED));
    }
    super.remove(entity);
  }
}
