package com.axelor.apps.base.service.signature;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class SignatureServiceImpl implements SignatureService {

  @Override
  public byte[] sign(InputStream inputStream, Certificate[] certificateChain, PrivateKey signingKey)
      throws AxelorException, IOException {

    // Create the signature
    CMSTypedData msg = new CMSProcessableByteArray(inputStream.readAllBytes());
    CMSSignedData signedData = getCmsSignedData(certificateChain, signingKey, msg);
    return getSignatureBytes(signedData);
  }

  protected CMSSignedData getCmsSignedData(
      Certificate[] certificateChain, PrivateKey signingKey, CMSTypedData msg)
      throws AxelorException {
    try {
      BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
      Security.addProvider(bouncyCastleProvider);
      CMSSignedDataGenerator signedDataGen = new CMSSignedDataGenerator();
      X509Certificate cert = (X509Certificate) certificateChain[0];

      ContentSigner signer =
          new JcaContentSignerBuilder("SHA256withRSA")
              .setProvider(bouncyCastleProvider)
              .build(signingKey);
      signedDataGen.addSignerInfoGenerator(
          new JcaSignerInfoGeneratorBuilder(
                  new JcaDigestCalculatorProviderBuilder()
                      .setProvider(bouncyCastleProvider)
                      .build())
              .build(signer, cert));
      JcaCertStore certs = new JcaCertStore(Collections.singletonList(cert));
      signedDataGen.addCertificates(certs);

      return signedDataGen.generate(msg, false);
    } catch (OperatorCreationException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error when generating signer.");
    } catch (CMSException | CertificateEncodingException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error when generating signature.");
    }
  }

  protected byte[] getSignatureBytes(CMSSignedData signedData) throws AxelorException {
    try {
      return signedData.getEncoded();
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error while encoding signature.");
    }
  }
}
