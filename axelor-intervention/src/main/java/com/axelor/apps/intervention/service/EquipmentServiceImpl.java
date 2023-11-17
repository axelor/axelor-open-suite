package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.intervention.db.ArticleEquipment;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.auth.AuthUtils;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class EquipmentServiceImpl implements EquipmentService {

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeEquipment(Equipment equipment) throws AxelorException {
    /*try { TODO
      equipmentRepository.remove(equipment);
    } catch (IllegalArgumentException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }*/
  }

  @Override
  public MetaFile loadFormatFile() {
    // return appBarkeneService.getAppBarkene().getClientParkImportFormat();
    return null; // TODO
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createAndRealizeStockMovesForArticleEquipments(
      List<ArticleEquipment> articleEquipments) throws AxelorException {
    if (CollectionUtils.isEmpty(articleEquipments) || AuthUtils.getUser() == null) {
      return;
    }
    Company company = AuthUtils.getUser().getActiveCompany();
    TradingName tradingName = AuthUtils.getUser().getTradingName();
    /*for (ArticleEquipment articleEquipment : articleEquipments) {
      if (articleEquipment.getStockMove() == null && articleEquipment.getTrackingNumber() != null) {
        StockMove stockMove =
                stockMoveBarkeneService.createAndRealizeStockMoveForArticleEquipment(
                        articleEquipment, company, tradingName);
        articleEquipment.setStockMove(stockMove);
        articleEquipmentRepository.save(articleEquipment);
      }
    }*/
  }
}
