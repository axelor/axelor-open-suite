package com.axelor.csv.script;

import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ImportVehicle {

  @Inject private MetaFiles metaFiles;

  public Object importVehicle(Object bean, Map<String, Object> values) {
    assert bean instanceof Vehicle;

    Vehicle vehicle = (Vehicle) bean;

    final Path path = (Path) values.get("__path__");
    String fileName = (String) values.get("image_fileName");
    if (Strings.isNullOrEmpty(fileName)) {
      return bean;
    }

    final File image = path.resolve(fileName).toFile();

    try {
      final MetaFile metaFile = metaFiles.upload(image);
      vehicle.setImage(metaFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bean;
  }
}
