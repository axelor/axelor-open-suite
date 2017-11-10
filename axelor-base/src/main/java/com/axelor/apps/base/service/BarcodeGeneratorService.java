/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.FormatException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;

public class BarcodeGeneratorService {
	
	public static InputStream createBarCode(long inputId) {
		
		try {
			EAN13Writer wt = new EAN13Writer();
			String data = String.format("%012d", inputId);
			int check = getStandardUPCEANChecksum(data);
	        data += check;
			BitMatrix bt = wt.encode(data, BarcodeFormat.EAN_13, 200, 50);
			int width = bt.getWidth();
		    int height = bt.getHeight();
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
		} catch (FormatException | WriterException | IOException e) {
        	e.printStackTrace();
        }
	    
		return null;
	}
	
	//Method should be removed in future, as future version of zxing library will cover it(reference from zxing git).
	private static int getStandardUPCEANChecksum(CharSequence s) throws FormatException {
		int length = s.length();
	    int sum = 0;
	    for (int i = length - 1; i >= 0; i -= 2) {
	    	int digit = s.charAt(i) - '0';
	    	if (digit < 0 || digit > 9) {
	    		throw FormatException.getFormatInstance();
	    	}
	    	sum += digit;
	    }
	    sum *= 3;
	    for (int i = length - 2; i >= 0; i -= 2) {
	    	int digit = s.charAt(i) - '0';
	    	if (digit < 0 || digit > 9) {
	    		throw FormatException.getFormatInstance();
	    	}
	    	sum += digit;
	    }
	    
	    return (1000 - sum) % 10;
	 }	
}
