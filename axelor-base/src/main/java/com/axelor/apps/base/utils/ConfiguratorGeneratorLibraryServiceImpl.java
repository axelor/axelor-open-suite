package com.axelor.apps.base.utils;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ConfiguratorGeneratorLibraryServiceImpl
    implements ConfiguratorGeneratorLibraryService {

  protected ProductRepository productRepository;

  @Inject
  public ConfiguratorGeneratorLibraryServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  /**
   * Configurator built in service to create a product
   *
   * @param params
   * @return
   */
  @Transactional
  @Override
  public Product createProductFromConfigurator(Map<String, Object> params) {
    Product product = new Product();

    Mapper mapper = Mapper.of(Product.class);

    params.forEach(
        (key, value) ->
            ConfiguratorGeneratorMapToObject.fillObjectPropertyIfExists(
                product, mapper, key, value));
    return Beans.get(ProductRepository.class).save(product);
  }
}
