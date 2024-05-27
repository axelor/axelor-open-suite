package com.axelor.apps.base.service;

import com.axelor.apps.base.db.File;

public interface FileService {

  void setDMSFile(File file);

  String getInlineUrl(File file);
}
