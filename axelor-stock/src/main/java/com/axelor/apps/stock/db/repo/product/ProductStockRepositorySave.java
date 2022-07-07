package com.axelor.apps.stock.db.repo.product;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductStockRepositorySave {

  protected AppBaseService appBaseService;
  public WeightedAveragePriceService weightedAveragePriceService;

  @Inject
  public ProductStockRepositorySave(
      AppBaseService appBaseService, WeightedAveragePriceService weightedAveragePriceService) {
    this.appBaseService = appBaseService;
    this.weightedAveragePriceService = weightedAveragePriceService;
  }

  public void addProductCompanies(Product product) {
    Set<MetaField> specificProductFieldSet =
        appBaseService.getAppBase().getCompanySpecificProductFieldsSet();
    if (ObjectUtils.isEmpty(specificProductFieldSet)
        || !appBaseService.getAppBase().getEnableMultiCompany()
        || ObjectUtils.isEmpty(product.getProductCompanyList())) {
      return;
    }

    List<Company> productCompanies =
        product.getProductCompanyList().stream()
            .map(ProductCompany::getCompany)
            .collect(Collectors.toList());

    List<StockConfig> stockConfigList = Beans.get(StockConfigRepository.class).all().fetch();
    if (ObjectUtils.isEmpty(stockConfigList)) {
      return;
    }

    for (StockConfig stockConfig : stockConfigList) {
      Company company = stockConfig.getCompany();
      if (company != null
          && !productCompanies.contains(company)
          && stockConfig.getReceiptDefaultStockLocation() != null
          && (company.getArchived() == null || !company.getArchived())) {
        ProductCompany productCompany =
            createProductCompany(product, specificProductFieldSet, company);
        product.addProductCompanyListItem(productCompany);
      }
    }
  }

  protected ProductCompany createProductCompany(
      Product product, Set<MetaField> specificProductFieldSet, Company company) {
    Mapper mapper = Mapper.of(Product.class);

    ProductCompany productCompany = new ProductCompany();
    for (MetaField specificField : specificProductFieldSet) {
      mapper.set(
          productCompany, specificField.getName(), mapper.get(product, specificField.getName()));
    }

    // specific case for avgPrice per company
    productCompany.setAvgPrice(
        weightedAveragePriceService.computeAvgPriceForCompany(product, company));
    productCompany.setCompany(company);
    productCompany.setProduct(product);
    return productCompany;
  }
}
