package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.EquipmentModel;
import com.axelor.apps.intervention.db.ParkModel;
import com.axelor.db.JPA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;

public class ParkModelServiceImpl implements ParkModelService {

  @Override
  public List<Map<String, Object>> getEquipmentList(ParkModel parkModel) {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<EquipmentModel> cr = cb.createQuery(EquipmentModel.class);
    Root<EquipmentModel> root = cr.from(EquipmentModel.class);
    cr.select(root);

    Predicate belongToPark = cb.equal(root.get("parkModel"), parkModel);

    cr.where(belongToPark);

    List<EquipmentModel> products = JPA.em().createQuery(cr).getResultList();

    if (CollectionUtils.isEmpty(products)) {
      return new ArrayList<>();
    }

    return products.stream().map(this::buildMap).collect(Collectors.toList());
  }

  protected Map<String, Object> buildMap(EquipmentModel equipmentModel) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", equipmentModel.getId());
    map.put("code", equipmentModel.getCode());
    map.put("name", equipmentModel.getName());
    map.put("$qtyToGenerate", equipmentModel.getNumberOfElementsToGenerate());
    map.put("updatableQuantities", equipmentModel.getUpdatableQuantities());
    return map;
  }
}
