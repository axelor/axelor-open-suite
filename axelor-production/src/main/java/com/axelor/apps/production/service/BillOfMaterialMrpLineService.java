package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.supplychain.db.MrpLine;
import java.util.Optional;

public interface BillOfMaterialMrpLineService {

  Optional<BillOfMaterial> getEligibleBillOfMaterialOfProductInMrpLine(
      MrpLine mrpLine, Product product);
}
