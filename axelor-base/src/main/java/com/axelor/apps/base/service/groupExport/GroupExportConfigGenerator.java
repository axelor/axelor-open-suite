package com.axelor.apps.base.service.groupExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupExportConfigGenerator {

  private final Logger log = LoggerFactory.getLogger(GroupExportConfigGenerator.class);

  File config;
  StringBuilder configBuilder;
  static String configFileName = "import-config";

  protected void initialize() throws AxelorException {
    try {

      config = File.createTempFile(configFileName, ".xml");
      configBuilder = new StringBuilder();
      configBuilder.append(ConfigGeneratorText.CONFIG_START);

    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected void endConfig() throws AxelorException {
    configBuilder.append(ConfigGeneratorText.CONFIG_END);

    String content = configBuilder.toString();

    if (!StringUtils.isBlank(content)) {
      try (FileWriter fileWriter = new FileWriter(config)) {

        fileWriter.write(content);
        fileWriter.close();
      } catch (IOException e) {
        TraceBackService.trace(e);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Config file is not found."));
      }
    }
  }

  protected boolean addFileConfig(File file, AdvancedExport advancedExport) {

    try {
      configBuilder.append("<input ");
      configBuilder.append("file=\"" + file.getName() + "\" ");
      configBuilder.append("separator=\";\" ");
      configBuilder.append("type=\"" + advancedExport.getMetaModel().getFullName() + "\"");
      configBuilder.append(">");
      this.addInnerBinding(advancedExport);
      configBuilder.append("</input>\n\n");
    } catch (Exception e) {
      log.debug("Error while adding config of {}", advancedExport.getMetaModel().getName());
      TraceBackService.trace(e);
      ;
      return false;
    }

    log.debug("Adding config of {}", advancedExport.getMetaModel().getName());
    return true;
  }

  private void addInnerBinding(AdvancedExport advancedExport) {
    return;
  }

  public File getConfigFile() {
    return config;
  }
}
