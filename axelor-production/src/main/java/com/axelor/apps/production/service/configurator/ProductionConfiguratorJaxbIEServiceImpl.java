package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.export.xml.IEXmlService;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.ConfiguratorProdProcess;
import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ConfiguratorProdProduct;
import com.axelor.apps.production.db.repo.ConfiguratorBOMRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.xml.models.ProductionConfiguratorExport;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.apps.sale.service.configurator.ConfiguratorJaxbIEServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBException;
import org.apache.commons.collections.CollectionUtils;

/**
 * This class is a implementation on ConfiguratorIEService. It uses library jaxb in order to export
 * or import xml of Configurators creators. This class does manage ConfiguratorBOM that may be
 * located in ConfiguratorCreator.
 */
public class ProductionConfiguratorJaxbIEServiceImpl extends ConfiguratorJaxbIEServiceImpl {

  private static int MAX_DEPTH = 50;

  @Inject
  public ProductionConfiguratorJaxbIEServiceImpl(
      IEXmlService exportXmlService,
      AppBaseService appBaseService,
      ConfiguratorCreatorRepository configuratorCreatorRepository,
      ConfiguratorCreatorService configuratorCreatorService) {
    super(
        exportXmlService,
        appBaseService,
        configuratorCreatorRepository,
        configuratorCreatorService);
  }

  @Override
  public MetaFile exportConfiguratorsToXML(List<ConfiguratorCreator> ccList)
      throws AxelorException {

    try {
      ProductionConfiguratorExport productionConfiguratorExport =
          new ProductionConfiguratorExport(ccList);
      return ieXmlService.exportXML(
          productionConfiguratorExport,
          String.format(
              XML_NAME_TEMPLATE,
              appBaseService.getTodayDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
          ProductionConfiguratorExport.class);

    } catch (JAXBException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public String importXMLToConfigurators(InputStream inputStream) throws AxelorException {

    try {
      ProductionConfiguratorExport configuratorExport =
          ieXmlService.importXMLToModel(inputStream, ProductionConfiguratorExport.class);
      StringBuilder importLog = new StringBuilder();
      super.linkConfiguratorFormulaToCC(configuratorExport.getConfiguratorsCreators());
      List<ConfiguratorCreator> configuratorsCreators =
          configuratorExport.getConfiguratorsCreators();
      configuratorsCreators.forEach(
          configuratorCreator -> {
            ConfiguratorBOM configuratorBom = configuratorCreator.getConfiguratorBom();
            // we check if id is not null since we may have already retrieve from database the
            // configuratorBom
            if (configuratorBom != null && configuratorBom.getId() == null) {
              linkChildToParentConfiguratorBOM(configuratorBom);
            }
          });

      int totalImport = saveConfiguratorCreators(configuratorsCreators, importLog);

      importLog.append(
          "\nTotal records: "
              + configuratorExport.getConfiguratorsCreators().size()
              + ", Total imported: "
              + totalImport);
      return importLog.toString();

    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected void linkChildToParentConfiguratorBOM(ConfiguratorBOM configuratorBom) {

    if (configuratorBom.getConfiguratorBomList() != null) {
      configuratorBom
          .getConfiguratorBomList()
          .forEach(
              configuratorBomChild -> {
                configuratorBomChild.setParentConfiguratorBOM(configuratorBom);
                linkChildToParentConfiguratorBOM(configuratorBomChild);
              });
    }
  }

  @Transactional(ignore = {PersistenceException.class})
  protected int saveConfiguratorCreators(
      List<ConfiguratorCreator> configuratorsCreators, StringBuilder importLog) {

    AtomicInteger totalImport = new AtomicInteger(0);

    configuratorsCreators.forEach(
        configuratorCreator -> {
          try {
            if (configuratorCreatorRepository.findByName(configuratorCreator.getName()) != null) {
              importLog.append(
                  "\nError in import: "
                      + String.format(CONFIGURATOR_ALREADY_EXIST, configuratorCreator.getName()));
            } else {
              configuratorCreatorRepository.save(configuratorCreator);
              // Complete import
              completeAfterImport(configuratorCreator);
              totalImport.addAndGet(1);
            }
          } catch (Exception e) {
            importLog.append(
                "\nError in import: "
                    + configuratorCreator.getName()
                    + "\n"
                    + Arrays.toString(e.getStackTrace()));
          }
        });

    return totalImport.get();
  }

  /**
   * Update the changed attribute in all formula O2M. This implementation also update formulas in
   * configurator BOM and configurator prod process.
   *
   * @param creator
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  @Override
  protected void updateAttributeNameInFormulas(
      ConfiguratorCreator creator, String oldName, String newName) throws AxelorException {
    super.updateAttributeNameInFormulas(creator, oldName, newName);

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return;
    }
    ConfiguratorBOM configuratorBom = creator.getConfiguratorBom();
    if (configuratorBom != null) {
      updateAttributeNameInFormulas(configuratorBom, oldName, newName, 0);
    }
  }

  /**
   * Update attribute name in formulas for a configurator bom.
   *
   * @param configuratorBom
   * @param oldName
   * @param newName
   * @param counter used to count the recursive call.
   * @throws AxelorException if we got too many recursive call.
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorBOM configuratorBom, String oldName, String newName, int counter)
      throws AxelorException {
    if (counter > MAX_DEPTH) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.CONFIGURATOR_BOM_IMPORT_TOO_MANY_CALLS));
    }
    updateAllFormulaFields(configuratorBom, oldName, newName);
    ConfiguratorProdProcess configuratorProdProcess = configuratorBom.getConfiguratorProdProcess();
    if (configuratorProdProcess != null) {
      updateAttributeNameInFormulas(configuratorBom.getConfiguratorProdProcess(), oldName, newName);
    }

    // recursive call for child BOMs
    List<ConfiguratorBOM> childConfiguratorBomList =
        Beans.get(ConfiguratorBOMRepository.class)
            .all()
            .filter("self.parentConfiguratorBOM.id = :parentId")
            .bind("parentId", configuratorBom.getId())
            .fetch();
    if (childConfiguratorBomList != null) {
      for (ConfiguratorBOM childConfiguratorBom : childConfiguratorBomList) {
        updateAttributeNameInFormulas(childConfiguratorBom, oldName, newName, counter + 1);
      }
    }
  }

  /**
   * Update attribute name in formulas for a configurator prod process.
   *
   * @param configuratorBom
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorProdProcess configuratorProdProcess, String oldName, String newName)
      throws AxelorException {
    List<ConfiguratorProdProcessLine> configuratorProdProcessLines =
        configuratorProdProcess.getConfiguratorProdProcessLineList();
    if (configuratorProdProcessLines == null) {
      return;
    }
    for (ConfiguratorProdProcessLine configuratorProdProcessLine : configuratorProdProcessLines) {
      updateAllFormulaFields(configuratorProdProcessLine, oldName, newName);
      List<ConfiguratorProdProduct> confProdProductList =
          configuratorProdProcessLine.getConfiguratorProdProductList();
      if (CollectionUtils.isNotEmpty(confProdProductList)) {
        for (ConfiguratorProdProduct confProdProduct : confProdProductList) {
          updateAllFormulaFields(confProdProduct, oldName, newName);
        }
      }
    }
  }

  /**
   * Replace oldName by newName in all string fields of the given object.
   *
   * @param obj
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  protected void updateAllFormulaFields(Object obj, String oldName, String newName)
      throws AxelorException {
    List<Field> formulaFields =
        Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(field -> field.getType().equals(String.class))
            .collect(Collectors.toList());
    for (Field field : formulaFields) {
      try {
        // call getter of the string field
        Object strFormula =
            new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj);
        if (strFormula != null) {
          new PropertyDescriptor(field.getName(), obj.getClass())
              .getWriteMethod()
              .invoke(obj, ((String) strFormula).replace(oldName, newName));
        }
      } catch (IntrospectionException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        // should not happen since we fetched fields from the class
        throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
      }
    }
  }
}
