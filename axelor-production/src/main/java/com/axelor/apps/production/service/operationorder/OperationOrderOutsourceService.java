package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.OperationOrder;
import java.util.Optional;

public interface OperationOrderOutsourceService {

  Optional<Partner> getOutsourcePartner(OperationOrder operationOrder);
}
