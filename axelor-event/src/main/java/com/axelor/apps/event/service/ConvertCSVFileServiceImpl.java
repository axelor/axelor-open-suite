
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.axelor.apps.base.service.imports.importer.ExcelToCSV;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
public class ConvertCSVFileServiceImpl {

	//
//	 @Inject private ExcelToCSV excelToCSV;
//
//	  @Inject private MetaFiles metaFiles;
//
//	  @Override
//	  public MetaFile convertExcelFile(File excelFile)
//	      throws IOException, AxelorException, ParseException {
//
//	    File zipFile = this.createZIPFromExcel(excelFile);
//	    FileInputStream inStream = new FileInputStream(zipFile);
//	    MetaFile metaFile =
//	        metaFiles.upload(
//	            inStream,
//	            "demo_data_" + new SimpleDateFormat("ddMMyyyHHmm").format(new Date()) + ".zip");
//	    inStream.close();
//	    zipFile.delete();
//
//	    return metaFile;
//	  }
}
