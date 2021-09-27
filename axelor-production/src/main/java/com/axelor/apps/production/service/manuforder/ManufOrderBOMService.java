package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.util.List;

public interface ManufOrderBOMService {

  /**
   * Method to generate bill of material list from manuf order and prodProduct list.
   *
   * @param mo : {@link ManufOrder}
   * @param prodProductList : {@link ProdProduct} list
   * @return {@link BillOfMaterial} list
   */
  List<BillOfMaterial> generateBOMList(ManufOrder mo, List<ProdProduct> prodProductList);
}
