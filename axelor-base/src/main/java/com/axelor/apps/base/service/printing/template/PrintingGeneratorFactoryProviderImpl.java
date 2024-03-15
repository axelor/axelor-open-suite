package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.db.repo.PrintingTemplateLineRepository;
import java.util.HashMap;
import java.util.Map;

public class PrintingGeneratorFactoryProviderImpl implements PrintingGeneratorFactoryProvider {

  @Override
  public Class<? extends PrintingGeneratorFactory> get(Integer type) {
    Map<Integer, Class<? extends PrintingGeneratorFactory>> data = new HashMap<>();
    data.put(
        PrintingTemplateLineRepository.PRINTING_TEMPLATE_LINE_TYPE_SELECT_BIRT,
        PrintingGeneratorFactoryBirt.class);
    data.put(
        PrintingTemplateLineRepository.PRINTING_TEMPLATE_LINE_TYPE_SELECT_FILE,
        PrintingGeneratorFactoryFile.class);
    return data.get(type);
  }
}
