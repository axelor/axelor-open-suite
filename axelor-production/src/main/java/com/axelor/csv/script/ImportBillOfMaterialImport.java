package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterialImport;
import com.axelor.apps.production.db.BillOfMaterialImportSource;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ImportBillOfMaterialImport {

  protected final MetaFiles metaFiles;

  @Inject
  public ImportBillOfMaterialImport(
      MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  public Object importBillOfMaterialImport(Object bean, Map<String, Object> values) {
    assert bean instanceof BillOfMaterialImport;

    BillOfMaterialImport billOfMaterialImport = (BillOfMaterialImport) bean;
    final Path path = (Path) values.get("__path__");

    try {
      billOfMaterialImport.setImportMetaFile(
          metaFiles.upload(path.resolve((String) values.get("importMetaFileName")).toFile()));
    } catch (IOException e) {
      e.printStackTrace();
      return bean;
    }

    return bean;
  }

  public Object importBillOfMaterialImportSource(Object bean, Map<String, Object> values) {
    assert bean instanceof BillOfMaterialImportSource;

    BillOfMaterialImportSource billOfMaterialImportSource = (BillOfMaterialImportSource) bean;
    final Path path = (Path) values.get("__path__");

    try {
      billOfMaterialImportSource.setBindingFile(
          metaFiles.upload(path.resolve((String) values.get("bindingFileName")).toFile()));
    } catch (IOException e) {
      e.printStackTrace();
      return bean;
    }

    return bean;
  }
}
