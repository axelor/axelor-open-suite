package com.axelor.apps.tool.service;

import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ConvertBinaryToMetafileServiceImpl implements ConvertBinaryToMetafileService {

  public MetaFile convertByteTabPictureInMetafile(byte[] bytePicture) throws IOException {
    if (bytePicture != null) {
      String base64Img = new String(bytePicture);
      String base64ImgData = "";
      if (base64Img.contains(",")) {
        base64ImgData = base64Img.split(",")[1];
      }
      byte[] img = Base64.getDecoder().decode(base64ImgData);
      try (ByteArrayInputStream inImg = new ByteArrayInputStream(img)) {
        MetaFile picture =
            Beans.get(MetaFiles.class)
                .upload(inImg, Files.createTempFile(null, null).toFile().getName());
        return picture;
      }
    }
    return new MetaFile();
  }
}
