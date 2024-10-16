package com.axelor.apps.base.service;

import com.axelor.meta.db.MetaFile;
import java.io.IOException;

public interface MetaFileService {
  MetaFile copyMetaFile(MetaFile metaFile) throws IOException;
}
