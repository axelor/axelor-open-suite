package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.export.xml.IEXmlService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.xml.models.ConfiguratorExport;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

/**
 * This class is a implementation on ConfiguratorIEService. It uses library jaxb in order to export
 * or import xml of Configurators creators. This class does not manage ConfiguratorBOM that may be
 * located in ConfiguratorCreator.
 */
public class ConfiguratorJaxbIEServiceImpl implements ConfiguratorJaxbIEService {

  public static final String CONFIGURATOR_ALREADY_EXIST =
      "ConfiguratorCreator %s already exist and can not be imported";

  public static final String XML_NAME_TEMPLATE = "ConfiguratorCreatorExport-%s";

  protected IEXmlService ieXmlService;

  protected AppBaseService appBaseService;

  protected ConfiguratorCreatorRepository configuratorCreatorRepository;

  protected ConfiguratorCreatorService configuratorCreatorService;

  @Inject
  public ConfiguratorJaxbIEServiceImpl(
      IEXmlService ieXmlService,
      AppBaseService appBaseService,
      ConfiguratorCreatorRepository configuratorCreatorRepository,
      ConfiguratorCreatorService configuratorCreatorService) {

    this.ieXmlService = ieXmlService;
    this.appBaseService = appBaseService;
    this.configuratorCreatorRepository = configuratorCreatorRepository;
    this.configuratorCreatorService = configuratorCreatorService;
  }

  @Override
  public MetaFile exportConfiguratorsToXML(List<ConfiguratorCreator> ccList)
      throws AxelorException {

    try {
      return ieXmlService.exportXML(
          new ConfiguratorExport(ccList),
          String.format(
              XML_NAME_TEMPLATE,
              appBaseService.getTodayDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
          ConfiguratorExport.class);

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
      ConfiguratorExport configuratorExport =
          ieXmlService.importXMLToModel(inputStream, ConfiguratorExport.class);
      StringBuilder importLog = new StringBuilder();
      linkConfiguratorFormulaToCC(configuratorExport.getConfiguratorsCreators());

      int totalImport =
          saveConfiguratorCreators(configuratorExport.getConfiguratorsCreators(), importLog);
      importLog.append(
          "Total records: "
              + configuratorExport.getConfiguratorsCreators().size()
              + ", Total imported: "
              + totalImport);
      return importLog.toString();

    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public String importXMLToConfigurators(String pathDiff) throws AxelorException {

    Path path = MetaFiles.getPath(pathDiff);

    try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
      return importXMLToConfigurators(fileInputStream);
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected void linkConfiguratorFormulaToCC(List<ConfiguratorCreator> configuratorCreators) {
    configuratorCreators.forEach(
        configuratorCreator -> {
          configuratorCreator
              .getConfiguratorProductFormulaList()
              .forEach(
                  productFormula -> {
                    productFormula.setProductCreator(configuratorCreator);
                  });
          configuratorCreator
              .getConfiguratorSOLineFormulaList()
              .forEach(
                  SOLineFormula -> {
                    SOLineFormula.setSoLineCreator(configuratorCreator);
                  });
        });
  }

  @Transactional
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
              completeAfterImport(configuratorCreator);
              totalImport.addAndGet(1);
            }
          } catch (Exception e) {
            importLog.append("Error in import: " + Arrays.toString(e.getStackTrace()));
          }
        });

    return totalImport.get();
  }

  @Override
  public void fixAttributesName(ConfiguratorCreator creator) throws AxelorException {
    List<MetaJsonField> attributes = creator.getAttributes();
    if (attributes == null) {
      return;
    }
    for (MetaJsonField attribute : attributes) {
      String name = attribute.getName();
      if (name != null && name.contains("_")) {
        attribute.setName(name.substring(0, name.lastIndexOf('_')) + '_' + creator.getId());
      }
      updateOtherFieldsInAttribute(creator, attribute);
      updateAttributeNameInFormulas(creator, name, attribute.getName());
    }
  }

  /**
   * Update the configurator id in other fields of the attribute.
   *
   * @param creator
   * @param attribute attribute to update
   */
  protected void updateOtherFieldsInAttribute(
      ConfiguratorCreator creator, MetaJsonField attribute) {
    try {
      List<Field> fieldsToUpdate =
          Arrays.stream(attribute.getClass().getDeclaredFields())
              .filter(field -> field.getType().equals(String.class))
              .collect(Collectors.toList());
      for (Field field : fieldsToUpdate) {
        Mapper mapper = Mapper.of(attribute.getClass());
        Method getter = mapper.getGetter(field.getName());
        String fieldString = (String) getter.invoke(attribute);
        if (fieldString != null && fieldString.contains("_")) {
          Method setter = mapper.getSetter(field.getName());
          String updatedFieldString =
              fieldString.substring(0, fieldString.lastIndexOf('_')) + '_' + creator.getId();
          setter.invoke(attribute, updatedFieldString);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Update the changed attribute in all formula O2M.
   *
   * @param creator
   * @param oldName
   * @param newName
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorCreator creator, String oldName, String newName) throws AxelorException {
    if (creator.getConfiguratorProductFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorProductFormulaList(), oldName, newName);
    }
    if (creator.getConfiguratorSOLineFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorSOLineFormulaList(), oldName, newName);
    }
  }

  /**
   * Update the changed attribute in formulas.
   *
   * @param formulas
   * @param oldAttributeName
   * @param newAttributeName
   */
  protected void updateAttributeNameInFormulas(
      List<? extends ConfiguratorFormula> formulas,
      String oldAttributeName,
      String newAttributeName) {

    formulas.forEach(
        configuratorFormula -> {
          if (!StringUtils.isEmpty(configuratorFormula.getFormula())) {
            configuratorFormula.setFormula(
                configuratorFormula.getFormula().replace(oldAttributeName, newAttributeName));
          }
        });
  }

  /**
   * Complete import by fixing attribute name (name_XX where XX is id of ConfiguratorCreator in
   * Database) Update attributes and indicators All fields that are related to id of
   * ConfiguratorCreator in Database will be also fixed
   *
   * @param creator
   * @throws AxelorException
   */
  protected void completeAfterImport(ConfiguratorCreator creator) throws AxelorException {
    fixAttributesName(creator);
    configuratorCreatorService.updateAttributes(creator);
    configuratorCreatorService.updateIndicators(creator);
  }
}
