package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;

public interface ConfiguratorCheckService {

    void checkLinkedSaleOrderLine(Configurator configurator, Product product) throws AxelorException;
}
