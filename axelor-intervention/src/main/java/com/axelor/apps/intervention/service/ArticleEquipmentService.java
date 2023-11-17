package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.ArticleEquipment;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.stock.db.TrackingNumber;
import java.util.List;

public interface ArticleEquipmentService {

  TrackingNumber createTrackingNumber(ArticleEquipment articleEquipment) throws AxelorException;

  void changeEquipment(List<ArticleEquipment> articleEquipmentList, Equipment equipment);
}
