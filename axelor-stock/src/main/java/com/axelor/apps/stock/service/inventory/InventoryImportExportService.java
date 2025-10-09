package com.axelor.apps.stock.service.inventory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.nio.file.Path;

public interface InventoryImportExportService {

  Path importFile(Inventory inventory) throws AxelorException;

  MetaFile exportInventoryAsCSV(Inventory inventory) throws IOException;
}
