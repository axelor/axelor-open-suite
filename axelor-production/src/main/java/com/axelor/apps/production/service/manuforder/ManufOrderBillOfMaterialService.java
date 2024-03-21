package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface ManufOrderBillOfMaterialService {

  List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial bom, ManufOrder mo, List<Product> productList) throws AxelorException;
}
