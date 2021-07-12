package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.export.xml.ExportXmlService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.xml.mappers.ExportedConfiguratorCreatorMapper;
import com.axelor.apps.sale.xml.models.ExportedConfiguratorCreator;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

public class ConfiguratorIEServiceImpl implements ConfiguratorIEService {

  private static final String XML_NAME_TEMPLATE = "ConfiguratorCreatorExport-%s";

  private ExportedConfiguratorCreatorMapper exportedCCMapper;

  private ExportXmlService exportXmlService;

  private AppBaseService appBaseService;

  @Inject
  public ConfiguratorIEServiceImpl(
      ExportedConfiguratorCreatorMapper exportedCCMapper,
      ExportXmlService exportXmlService,
      AppBaseService appBaseService) {
    this.exportedCCMapper = exportedCCMapper;
    this.exportXmlService = exportXmlService;
    this.appBaseService = appBaseService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void exportConfiguratorsToXML(List<ConfiguratorCreator> ccList) {

    try {
      exportXmlService.exportXML(
          ccList.stream().map(cc -> exportedCCMapper.mapFrom(cc)).collect(Collectors.toList()),
          String.format(
              XML_NAME_TEMPLATE,
              appBaseService.getTodayDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
          ExportedConfiguratorCreator.class);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
