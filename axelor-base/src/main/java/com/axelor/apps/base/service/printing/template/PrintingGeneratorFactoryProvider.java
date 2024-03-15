package com.axelor.apps.base.service.printing.template;

public interface PrintingGeneratorFactoryProvider {

  public Class<? extends PrintingGeneratorFactory> get(Integer type);
}
