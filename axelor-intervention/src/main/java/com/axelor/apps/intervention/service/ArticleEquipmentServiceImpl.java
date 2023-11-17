package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.intervention.db.ArticleEquipment;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;

public class ArticleEquipmentServiceImpl implements ArticleEquipmentService {

  protected final TrackingNumberService trackingNumberService;
  protected final AppBaseService appBaseService;

  @Inject
  public ArticleEquipmentServiceImpl(
      TrackingNumberService trackingNumberService, AppBaseService appBaseService) {
    this.trackingNumberService = trackingNumberService;
    this.appBaseService = appBaseService;
  }

  @Override
  public TrackingNumber createTrackingNumber(ArticleEquipment articleEquipment)
      throws AxelorException {
    Company company = AuthUtils.getUser().getActiveCompany();
    return trackingNumberService.createTrackingNumber(
        articleEquipment.getProduct(), company, appBaseService.getTodayDate(company), null);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void changeEquipment(List<ArticleEquipment> articleEquipmentList, Equipment equipment) {
    if (CollectionUtils.isEmpty(articleEquipmentList) || equipment == null) {
      return;
    }

    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaUpdate<ArticleEquipment> cr = cb.createCriteriaUpdate(ArticleEquipment.class);
    Root<ArticleEquipment> root = cr.from(ArticleEquipment.class);
    cr.set("equipment", equipment);

    Predicate inList =
        root.get("id")
            .in(
                articleEquipmentList.stream()
                    .map(ArticleEquipment::getId)
                    .collect(Collectors.toList()));

    cr.where(inList);
    JPA.em().createQuery(cr).executeUpdate();
  }
}
