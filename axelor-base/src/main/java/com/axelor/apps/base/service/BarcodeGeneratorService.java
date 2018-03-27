/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import com.google.inject.Singleton;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

@Singleton
public class BarcodeGeneratorService {
	
	public static InputStream createBarCode(String serialno,BarcodeTypeConfig barcodeTypeConfig, boolean addPadding) throws  AxelorException {
		boolean isPadding = addPadding;
		int height=barcodeTypeConfig.getHeight();
		int width=barcodeTypeConfig.getWidth();
		BarcodeFormat barcodeFormat = BarcodeFormat.EAN_13;
		final MultiFormatWriter writer = new MultiFormatWriter();
		switch (barcodeTypeConfig.getName()) {
		
		//accepts variable length of alphanumeric input
		case "AZTEC": 
			barcodeFormat = BarcodeFormat.AZTEC;
			break;
			
		//accepts only number with variable length
		case "CODABAR":
			barcodeFormat = BarcodeFormat.CODABAR;
			serialno=checkTypeForVariableLength(serialno,barcodeFormat);
			break;
			
		//accepts variable length of alphanumeric input but alphabet in upperCase only	
		case "CODE_39":
			barcodeFormat = BarcodeFormat.CODE_39;
			serialno=checkTypeForUppercase(serialno,barcodeFormat);	
			break;
			
		//accepts variable length of alphanumeric input
		case "CODE_128":
			barcodeFormat = BarcodeFormat.CODE_128;
			break;
			
		//accepts variable length of alphanumeric input
		case "DATA_MATRIX":
			barcodeFormat = BarcodeFormat.DATA_MATRIX;
			break;
			
		//accepts only number with fixed length of input  
		case "EAN_8":
			barcodeFormat = BarcodeFormat.EAN_8;
			if(isPadding){
				serialno=addPaddingbits(7,serialno);
			}
			serialno=checkTypeForFixedLength(serialno,barcodeFormat,7);
			break;
			
		//accepts alphanumeric input with even length
		case "ITF":
			barcodeFormat = BarcodeFormat.ITF;
			if(isPadding){
				serialno=addEvenPaddingbits(serialno);
			}
			serialno=checkTypeForEvenLength(serialno,barcodeFormat,serialno.length());
			break;
			
		//accepts alphanumeric input with min and max length limit
		case "PDF_417":
			barcodeFormat = BarcodeFormat.PDF_417;
			if(isPadding){
				serialno=addcharPaddingbits(4,serialno);
			}
			serialno=checkTypeForLengthLimit(serialno,barcodeFormat,3,12);
			break;
			
		//accepts alphanumeric input with variable length 
		case "QR_CODE":
			barcodeFormat = BarcodeFormat.QR_CODE;
			break;
			
		//accepts only number with fixed length of input 
		case "UPC_A":
			barcodeFormat = BarcodeFormat.UPC_A;
			if(isPadding){
				serialno=addPaddingbits(11,serialno);
			}
			serialno=checkTypeForFixedLength(serialno,barcodeFormat,11);
			break;

		default:
		//accepts only number with fixed length of input 
		case "EAN_13":
			if(isPadding){
				serialno=addPaddingbits(12,serialno);	
			}
			serialno=checkTypeForFixedLength(serialno,barcodeFormat,12);
			break;
		}

		try {
	        BitMatrix bt = writer.encode(serialno, barcodeFormat, width, height);
		    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    int[] pixels = new int[width * height];
		    int index = 0;
		    for (int y = 0; y < height; y++) {
		    	for (int x = 0; x < width; x++) {
		    		pixels[index++] = bt.get(x, y) ? Color.BLACK.hashCode() : Color.WHITE.hashCode();
		    	}
		    }
		    image.setRGB(0, 0, width, height, pixels, 0, width);
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    ImageIO.write(image, "png", out);
		    return new ByteArrayInputStream(out.toByteArray());
		}
			catch (WriterException | IOException e) {
        	e.printStackTrace();
		}
		return null;
	}
	
	//type check for the barcodes which accept only numbers as input with fixed length like EAN_8, EAN_13, UPC_A, UPC_E
	private static String checkTypeForFixedLength(String serialno,BarcodeFormat barcodeFormat,int barcodeLength) throws AxelorException {
		if(isInteger(serialno)==true && barcodeLength==serialno.length()){
			return serialno;
		}
		else{
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_1),serialno,barcodeFormat,barcodeLength);
		}
	}
	
	//type check for the barcodes which accept only numbers as input with even length like ITF
	private static String checkTypeForEvenLength(String serialno,BarcodeFormat barcodeFormat,int serialLength) throws  AxelorException {
		if(isInteger(serialno)==true && (serialLength%2) == 0){
			return serialno;
		}
		else{
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_2),serialno,barcodeFormat,null);
		}
	}
	
	//type check for the barcodes which accept only numbers as input with variable length like CODABAR
	private static String checkTypeForVariableLength(String serialno,BarcodeFormat barcodeFormat) throws  AxelorException {
		if(isInteger(serialno)==true){
			return serialno;
		}
		else{
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_3),serialno,barcodeFormat,null);
		}
	}
	
	//type check for the barcode which accept upperCase letters like CODE_39
	private static String checkTypeForUppercase(String serialno,BarcodeFormat barcodeFormat) throws AxelorException {
		if(serialno == serialno.toUpperCase()) {
			return serialno;
		}	
		else {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_4),serialno,barcodeFormat,null);
		}
	}
	
	//type check for the barcode which accepts alphanumeric input having min and max length limit like PDF_417
	private static String checkTypeForLengthLimit(String serialno,BarcodeFormat barcodeFormat,int minBarcodeLength,int maxBarcodeLength) throws AxelorException {
		//particularly for PDF_417
		if(serialno.length() == 4 || serialno.length() == 5){
			if(isInteger(serialno)==true){
				throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_6),serialno,barcodeFormat);
			}
		}
		
		if(serialno.length() > minBarcodeLength && serialno.length() < maxBarcodeLength){
			return serialno;
		}
		else{
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.BARCODE_GENERATOR_5),serialno,barcodeFormat,minBarcodeLength,maxBarcodeLength);
		}
	}
	
	 //check input is integer or not
	 private static boolean isInteger(String s) {
	    try { 
	        Long.parseLong(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}
	
	 //add padding bits for barcodes which accepts input with only even length like ITF
	private static String addEvenPaddingbits(String serialno) {
		if((serialno.length() % 2) != 0 ) {
				serialno=serialno+"1";
		}
		return serialno;
	}
	 
	//add padding bits for the barcodes which accepts input with fixed length like EAN_8, EAN_13, UPC_A, UPC_E
	private static String addPaddingbits(int barcodeLength,String serialno) {
		int paddingbits;
		int serialnoLength=serialno.length();
		if(serialnoLength < barcodeLength){
		paddingbits=barcodeLength-serialnoLength;
		for(int i=0;i<paddingbits;i++) {
			serialno=serialno + "1";
			}
		}
		else if(serialnoLength > barcodeLength){
			paddingbits= serialnoLength-barcodeLength;
			String number = serialno.substring(0,serialnoLength-paddingbits);
			serialno=number;
		}
		return serialno;
	}
	
	//add char padding bits
	private static String addcharPaddingbits(int barcodeLength,String serialno) {
		int paddingbits;
		int serialnoLength=serialno.length();
		if(serialnoLength < barcodeLength){
		paddingbits=barcodeLength-serialnoLength;
		for(int i=0;i<paddingbits;i++) {
			serialno=serialno + "a";
			}
		}
		return serialno;
	}
}
