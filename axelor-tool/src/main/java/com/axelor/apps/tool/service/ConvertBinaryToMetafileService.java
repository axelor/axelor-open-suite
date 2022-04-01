package com.axelor.apps.tool.service;

import com.axelor.meta.db.MetaFile;
import java.io.IOException;

public interface ConvertBinaryToMetafileService {
  public MetaFile convertByteTabPictureInMetafile(byte[] bytePicture) throws IOException;
}
