package com.axelor.apps.event.service;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;

public interface ConvertCSVFileService {
  
public MetaFile convertExcelFile(File excelFile)
    throws IOException, AxelorException, ParseException;
}
