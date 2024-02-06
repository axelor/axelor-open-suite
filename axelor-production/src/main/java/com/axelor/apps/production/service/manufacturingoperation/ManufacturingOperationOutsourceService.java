package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufacturingOperation;
import java.util.Optional;

public interface ManufacturingOperationOutsourceService {

  Optional<Partner> getOutsourcePartner(ManufacturingOperation manufacturingOperation);

  boolean getUseLineInGeneratedPO(ManufacturingOperation manufacturingOperation);
}
