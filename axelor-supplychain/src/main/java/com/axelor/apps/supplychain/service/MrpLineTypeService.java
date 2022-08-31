package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.MrpLineType;
import java.util.List;

public interface MrpLineTypeService {

  /** Returns found mrp line type given the application field and the mrp type select */
  MrpLineType getMrpLineType(int elementSelect, int mrpTypeSelect);

  /** Returns found mrp line types given the application field and the mrp type select */
  List<MrpLineType> getMrpLineTypeList(int elementSelect, int mrpTypeSelect);
}
