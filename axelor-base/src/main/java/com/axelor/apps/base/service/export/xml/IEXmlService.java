package com.axelor.apps.base.service.export.xml;

import com.axelor.apps.base.xml.models.ExportedModel;
import com.axelor.meta.db.MetaFile;

public interface IEXmlService {

  <T extends ExportedModel> MetaFile exportXML(T srcObject, String fileName, Class<T> classObject)
      throws Exception;

  <T extends ExportedModel> T importXMLToModel(String pathFile, Class<T> classObject)
      throws Exception;
}
