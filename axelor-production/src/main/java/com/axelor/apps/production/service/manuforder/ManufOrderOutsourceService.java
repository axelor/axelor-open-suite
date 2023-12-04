package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufOrder;
import java.util.Optional;

public interface ManufOrderOutsourceService {

  Optional<Partner> getOutsourcePartner(ManufOrder manufOrder) throws AxelorException;

  boolean isOutsource(ManufOrder manufOrder);
}
