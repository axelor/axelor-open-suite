package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.util.List;
import java.util.Optional;

public interface ManufOrderOutsourceService {

  Optional<Partner> getOutsourcePartner(ManufOrder manufOrder);

  boolean isOutsource(ManufOrder manufOrder);

  void validateOutsourceDeclaration(
      ManufOrder manufOrder, Partner outsourcePartner, List<ProdProduct> productList)
      throws AxelorException;
}
