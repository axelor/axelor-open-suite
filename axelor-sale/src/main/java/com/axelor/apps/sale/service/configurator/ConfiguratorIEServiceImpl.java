package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.export.xml.IEXmlService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.xml.models.ConfiguratorExport;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.bind.JAXBException;

public class ConfiguratorIEServiceImpl implements ConfiguratorIEService {

  private static final String XML_NAME_TEMPLATE = "ConfiguratorCreatorExport-%s";

  private IEXmlService ieXmlService;

  private AppBaseService appBaseService;

  private ConfiguratorCreatorRepository configuratorCreatorRepository;

  @Inject
  public ConfiguratorIEServiceImpl(
      IEXmlService exportXmlService,
      AppBaseService appBaseService,
      ConfiguratorCreatorRepository configuratorCreatorRepository) {

    this.ieXmlService = exportXmlService;
    this.appBaseService = appBaseService;
    this.configuratorCreatorRepository = configuratorCreatorRepository;
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
  @Transactional
  public String importXMLToConfigurators(String pathDiff) throws AxelorException {

    try {
      ConfiguratorExport configuratorExport =
          ieXmlService.importXMLToModel(pathDiff, ConfiguratorExport.class);
      StringBuilder importLog = new StringBuilder();
      AtomicInteger totalImport = new AtomicInteger(0);
      configuratorExport.getConfiguratorsCreators().stream()
          .forEach(
              configuratorCreator -> {
                try {
                  configuratorCreatorRepository.save(configuratorCreator);
                  totalImport.addAndGet(1);
                } catch (Exception e) {
                  importLog.append("Error in import: " + Arrays.toString(e.getStackTrace()));
                }
              });
      importLog.append(
          "Total records: "
              + configuratorExport.getConfiguratorsCreators().size()
              + ", Total imported: "
              + totalImport.get());
      return importLog.toString();

    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }
}
