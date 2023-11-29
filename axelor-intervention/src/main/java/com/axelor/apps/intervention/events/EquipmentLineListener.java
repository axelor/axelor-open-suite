package com.axelor.apps.intervention.events;

import com.axelor.apps.intervention.db.EquipmentLine;
import javax.persistence.PostUpdate;

public class EquipmentLineListener {

  @PostUpdate
  private void onPostUpdate(EquipmentLine equipmentLine) {
    if (equipmentLine.getEquipment() == null) {
      equipmentLine.setArchived(true);
    }
  }
}
