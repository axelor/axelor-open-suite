package com.axelor.apps.sale.service.configurator.tool;

import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorFormulaRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import org.junit.Assert;
import org.junit.Test;

public class ConfiguratorFormulaToolTest {

  @Test
  public void testComputeMetaFieldName() {
    ConfiguratorFormula configuratorFormula = givenProductMetaFieldConfiguratorFormula();
    Assert.assertEquals(
        "metaFieldName", ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  @Test
  public void testComputeIndicatorName() {
    ConfiguratorFormula configuratorFormula = givenInfoConfiguratorFormula();
    Assert.assertEquals(
        "freeIndicatorName", ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  @Test
  public void testComputeSaleOrderLineMetaFieldName() {
    ConfiguratorFormula configuratorFormula = givenSaleOrderMetaFieldConfiguratorFormula();
    Assert.assertEquals(
        "metaFieldName", ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  @Test
  public void testComputeNullTypeName() {
    ConfiguratorFormula configuratorFormula = givenNullTypeConfiguratorFormula();
    Assert.assertEquals("", ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  @Test
  public void testProductComputeMetaJsonFieldName() {
    ConfiguratorFormula configuratorFormula = givenProductMetaJsonFieldConfiguratorFormula();
    Assert.assertEquals(
        "metaFieldName$metaJsonFieldName",
        ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  @Test
  public void testSaleOrderComputeMetaJsonFieldName() {
    ConfiguratorFormula configuratorFormula = givenSaleOrderMetaJsonFieldConfiguratorFormula();
    Assert.assertEquals(
        "metaFieldName$metaJsonFieldName",
        ConfiguratorFormulaTool.computeFieldName(configuratorFormula));
  }

  protected ConfiguratorFormula givenProductMetaFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(ConfiguratorFormulaRepository.TYPE_PRODUCT);
    return configuratorFormula;
  }

  protected ConfiguratorFormula givenSaleOrderMetaJsonFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaJsonFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(ConfiguratorFormulaRepository.TYPE_SALE_ORDER_LINE);
    return configuratorFormula;
  }

  protected ConfiguratorFormula givenProductMetaJsonFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaJsonFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(ConfiguratorFormulaRepository.TYPE_PRODUCT);
    return configuratorFormula;
  }

  protected ConfiguratorFormula givenSaleOrderMetaFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(ConfiguratorFormulaRepository.TYPE_SALE_ORDER_LINE);
    return configuratorFormula;
  }

  protected ConfiguratorFormula givenInfoConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(ConfiguratorFormulaRepository.TYPE_INFO);
    configuratorFormula.setFreeIndicatorName("freeIndicatorName");
    configuratorFormula.setFreeIndicatorTitle("freeIndicatorTitle");
    return configuratorFormula;
  }

  protected ConfiguratorFormula givenNullTypeConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = this.createMetaFieldConfiguratorFormula();
    configuratorFormula.setTypeSelect(null);
    return configuratorFormula;
  }

  protected ConfiguratorFormula createMetaFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = new ConfiguratorFormula();
    MetaField metaField = new MetaField();
    metaField.setName("metaFieldName");
    configuratorFormula.setMetaField(metaField);
    return configuratorFormula;
  }

  protected ConfiguratorFormula createMetaJsonFieldConfiguratorFormula() {
    ConfiguratorFormula configuratorFormula = createMetaFieldConfiguratorFormula();
    MetaJsonField metaJsonField = new MetaJsonField();
    metaJsonField.setName("metaJsonFieldName");
    configuratorFormula.setMetaJsonField(metaJsonField);
    return configuratorFormula;
  }
}
