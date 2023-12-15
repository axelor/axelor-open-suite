package com.axelor.apps.base.service;

import java.io.IOException;

public interface AddressExportService {

  int export(String path) throws IOException;
}
