package com.axelor.apps.gst.web;

import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ImportController {

  public void importCSVData(ActionRequest request, ActionResponse response) {

    CSVImporter importer =
        new CSVImporter(
            "/home/axelor/project/abs-workspace-master/abs-webapp/modules/abs/axelor-gst/src/main/resources/data/input_config.xml",
            "/home/axelor/project/abs-workspace-master/abs-webapp/modules/abs/axelor-gst/src/main/resources/data/input");

    importer.addListener(
        new Listener() {

          @Override
          public void imported(Integer total, Integer success) {
            System.out.println("total::" + total);
            System.out.println("success::" + success);
          }

          @Override
          public void imported(Model bean) {}

          @Override
          public void handle(Model bean, Exception e) {}
        });
    importer.run();
  }
}
