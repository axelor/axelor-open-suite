package com.axelor.apps.event.web;

import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ImportController {

   @Inject MetaFileRepository metaFileRepo;

  @Inject ConvertCSVFileService convertCSVFileService;
  
  public void importCSVData(ActionRequest request, ActionResponse response)  throws IOException, AxelorException, ParseException {

    
     MetaFile metaFile =
        metaFileRepo.find(
            Long.valueOf(((Map) request.getContext().get("envimportFile")).get("id").toString()));
    File dataFile = MetaFiles.getPath(metaFile).toFile();

    if (Files.getFileExtension(dataFile.getName()).equals("xlsx")) {
      response.setValue(
          "$csvMetaFile", convertCSVFileService.convertExcelFile(dataFile));
    } else {
      response.setError(I18n.get(IExceptionMessage.VALIDATE_FILE_TYPE));
    }
    
    
    
  	/*
	 * CSVImporter importer = new CSVImporter(
	 * "/home/axelor/project/abs-workspace-master/abs-webapp/modules/abs/axelor-gst/src/main/resources/data/input_config.xml",
	 * "/home/axelor/project/abs-workspace-master/abs-webapp/modules/abs/axelor-gst/src/main/resources/data/input"
	 * );
	 * 
	 * importer.addListener( new Listener() {
	 * 
	 * @Override public void imported(Integer total, Integer success) {
	 * System.out.println("total::" + total); System.out.println("success::" +
	 * success); }
	 * 
	 * @Override public void imported(Model bean) {}
	 * 
	 * @Override public void handle(Model bean, Exception e) {} }); importer.run();
	 */
  }
}
