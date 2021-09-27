package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManufOrderBOMService {

  /**
   * Method to generate bill of material list from manuf order and prodProduct list.
   *
   * @param mo : {@link ManufOrder}
   * @param prodProductList : {@link ProdProduct} list
   * @return {@link BillOfMaterial} list
   */
  List<BillOfMaterial> generateBOMList(ManufOrder mo, List<ProdProduct> prodProductList);

  /**
   * Method to generate bill of material map from manuf order and prodProduct list. The map is a
   * entry of {@link BillOfMaterial} and a qty from prodProduct.
   *
   * @param mo : {@link ManufOrder}
   * @param prodProductList : {@link ProdProduct} list
   * @return Map of BillMaterial and BigDecimal.
   */
  Map<BillOfMaterial, BigDecimal> generateBOMMap(ManufOrder mo, List<ProdProduct> prodProductList);
}
