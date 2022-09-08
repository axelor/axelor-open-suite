package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.List;

public class MrpLineTypeServiceImpl implements MrpLineTypeService {

  protected MrpLineTypeRepository mrpLineTypeRepository;

  @Inject
  public MrpLineTypeServiceImpl(MrpLineTypeRepository mrpLineTypeRepository) {
    this.mrpLineTypeRepository = mrpLineTypeRepository;
  }

  @Override
  public MrpLineType getMrpLineType(int elementSelect, int mrpTypeSelect) {

    return getMrpLineTypeQuery(elementSelect, mrpTypeSelect).fetchOne();
  }

  @Override
  public List<MrpLineType> getMrpLineTypeList(int elementSelect, int mrpTypeSelect) {

    return getMrpLineTypeQuery(elementSelect, mrpTypeSelect).fetch();
  }

  protected Query<MrpLineType> getMrpLineTypeQuery(int elementSelect, int mrpTypeSelect) {
    int applicationFieldSelect = getApplicationField(mrpTypeSelect);
    return mrpLineTypeRepository
        .all()
        .filter(
            "self.elementSelect = ?1 and self.applicationFieldSelect LIKE ?2",
            elementSelect,
            "%" + applicationFieldSelect + "%");
  }

  protected int getApplicationField(int mrpTypeSelect) {

    switch (mrpTypeSelect) {
      case MrpRepository.MRP_TYPE_MRP:
        return MrpLineTypeRepository.APPLICATION_FIELD_MRP;
      case MrpRepository.MRP_TYPE_MPS:
        return MrpLineTypeRepository.APPLICATION_FIELD_MPS;
      default:
        return 0;
    }
  }
}
