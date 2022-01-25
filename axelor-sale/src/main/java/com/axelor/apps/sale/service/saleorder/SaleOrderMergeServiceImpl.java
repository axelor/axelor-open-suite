package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.db.JPA;
import com.axelor.rpc.Context;
import com.axelor.team.db.Team;
import java.util.Map;

public class SaleOrderMergeServiceImpl implements SaleOrderMergeService {

  @Override
  public void computeMapWithContext(Context context, Map<String, SaleOrderMergeObject> map) {
    if (map.get("contactPartner") == null
        || map.get("priceList") == null
        || map.get("team") == null) {
      throw new IllegalStateException(
          "Entry of contactPartner, priceList or team in map should not be null when calling this function");
    }
    // Check if priceList or contactPartner or team are content in parameters
    if (context.get("priceList") != null) {
      map.get("priceList")
          .setCommonObject(
              JPA.em()
                  .find(
                      PriceList.class,
                      new Long((Integer) ((Map) context.get("priceList")).get("id"))));
    }
    if (context.get("contactPartner") != null) {
      map.get("contactPartner")
          .setCommonObject(
              JPA.em()
                  .find(
                      Partner.class,
                      new Long((Integer) ((Map) context.get("contactPartner")).get("id"))));
    }
    if (context.get("team") != null) {
      map.get("team")
          .setCommonObject(
              JPA.em().find(Team.class, new Long((Integer) ((Map) context.get("team")).get("id"))));
    }
  }
}
