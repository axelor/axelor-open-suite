package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ProdProcessLine;
import java.util.Optional;

public interface ProdProcessLineOutsourceService {

  Optional<Partner> getOutsourcePartner(ProdProcessLine prodProcessLine);
}
