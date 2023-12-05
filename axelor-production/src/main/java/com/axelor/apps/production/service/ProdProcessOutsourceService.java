package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ProdProcess;
import java.util.Optional;

public interface ProdProcessOutsourceService {

  Optional<Partner> getOutsourcePartner(ProdProcess prodProcess);
}
