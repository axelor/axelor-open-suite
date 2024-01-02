package com.axelor.apps.base.service;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import java.util.List;

public interface DMSService {

  void addLinkedDMSFiles(List<? extends Model> entityList, Model entityMerged);

  DMSFile getDMSRoot(Model model);

  DMSFile getDMSHome(Model model, DMSFile dmsRoot);
}
