package com.axelor.apps.sale.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.utils.ConfiguratorGeneratorMapToObject;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class ConfiguratorGeneratorLibrarySaleServiceImpl
    implements ConfiguratorGeneratorLibrarySaleService {

  @Transactional
  @Override
  public SaleOrderLine createSaleOrderLineFromConfigurator(Map<String, Object> params)
      throws AxelorException {
    SaleOrder saleOrder = (SaleOrder) params.get("saleOrder");
    Product product = (Product) params.get("product");
    BigDecimal qty = (BigDecimal) params.get("qty");

    if (saleOrder == null || product == null || qty == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.CONFIGURATOR_MISSING_REQUIRED_FIELDS));
    }

    SaleOrderLine saleOrderLine =
        Beans.get(SaleOrderLineGeneratorService.class).createSaleOrderLine(saleOrder, product, qty);

    Mapper mapper = Mapper.of(SaleOrderLine.class);
    params.remove("saleOrder");
    params.remove("product");
    params.remove("qty");

    params.forEach(
        (key, value) ->
            ConfiguratorGeneratorMapToObject.fillObjectPropertyIfExists(
                saleOrderLine, mapper, key, value));

    return Beans.get(SaleOrderLineRepository.class).save(saleOrderLine);
  }
}
