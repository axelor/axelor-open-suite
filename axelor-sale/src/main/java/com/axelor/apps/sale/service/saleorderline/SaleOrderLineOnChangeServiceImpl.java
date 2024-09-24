package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnChangeServiceImpl implements SaleOrderLineOnChangeService {
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLineTaxService saleOrderLineTaxService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;
  protected SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService;

  @Inject
  public SaleOrderLineOnChangeServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService) {
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLineTaxService = saleOrderLineTaxService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.saleOrderLineComplementaryProductService = saleOrderLineComplementaryProductService;
  }

  @Override
  public Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineDiscountService.getDiscount(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        saleOrderLineComplementaryProductService.setIsComplementaryProductsUnhandledYet(
            saleOrderLine));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> taxLineOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    saleOrderLineMap.putAll(saleOrderLineTaxService.setTaxEquiv(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLinePriceService.updateInTaxPrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> priceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLinePriceService.updateInTaxPrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> inTaxPriceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLinePriceService.updatePrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> typeSelectOnChange(SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      saleOrderLineMap.putAll(emptyLine(saleOrderLine));
    }
    if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
      saleOrderLine.setProductName(I18n.get(ITranslation.SALE_ORDER_LINE_END_OF_PACK));
      saleOrderLineMap.put("productName", saleOrderLine.getProductName());
    }
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> productOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }

  protected Map<String, Object> emptyLine(SaleOrderLine saleOrderLine) {
    Map<String, Object> newSaleOrderLine = Mapper.toMap(new SaleOrderLine());
    newSaleOrderLine.put("qty", BigDecimal.ZERO);
    newSaleOrderLine.put("id", saleOrderLine.getId());
    newSaleOrderLine.put("version", saleOrderLine.getVersion());
    newSaleOrderLine.put("typeSelect", saleOrderLine.getTypeSelect());
    return newSaleOrderLine;
  }
}
