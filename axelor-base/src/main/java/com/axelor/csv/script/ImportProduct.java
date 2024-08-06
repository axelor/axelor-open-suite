/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportProduct {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ProductService productService;

  @Inject private ProductRepository productRepo;

  @Inject MetaFiles metaFiles;

  public Object importProduct(Object bean, Map<String, Object> values) {

    assert bean instanceof Product;

    Product product = (Product) bean;
    String fileName = (String) values.get("picture_fileName");

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) values.get("__path__");

      try {
        final File image = path.resolve(fileName).toFile();
        if (image != null && image.isFile()) {
          final MetaFile metaFile = metaFiles.upload(image);
          product.setPicture(metaFile);
        } else {
          LOG.debug(
              "No image file found: {}",
              image == null ? path.toAbsolutePath() : image.getAbsolutePath());
        }

      } catch (Exception e) {
        LOG.error("Error when importing product picture : {}", e);
      }
    }

    return productRepo.save(product);
  }

  public Object generateVariant(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof Product;

    Product product = (Product) bean;

    LOG.debug(
        "Product : {}, Variant config: {}", product.getCode(), product.getProductVariantConfig());

    if (product.getProductVariantConfig() != null) {
      productService.generateProductVariants(product);
    }

    return bean;
  }
}
