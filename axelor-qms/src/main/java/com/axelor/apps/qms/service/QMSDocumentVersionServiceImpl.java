package com.axelor.apps.qms.service;

import java.util.List;

import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.db.QMSDocumentVersion;

public class QMSDocumentVersionServiceImpl implements QMSDocumentVersionService {
	@Override
	public String getNextVersionIndex(QMSDocument document) {
		final List<QMSDocumentVersion> versions = document.getVersions();
		if(versions.size() == 0) {
			return "A";
		}
		return integerToVersionIndex(versionIndexToInteger(versions.get(versions.size() - 1).getVersionIndex()) + 1);
	}

	int versionIndexToInteger(final String str) {
		int index = 0;
		for(int k = str.length() - 1, i = 0 ; k >= 0 ; --k, ++i) {
			char c = str.charAt(k);
			// Character.getNumericValue() returns the values
			// 10-35 for the letter A-Z
			index += (Character.getNumericValue(c) - 9) * ((int) Math.pow(26, i));
		}
		return index - 1;
	}

	String integerToVersionIndex(final int indexInteger) {
		final StringBuilder versionIndex = new StringBuilder(3);

		for(int remainder = indexInteger + 1 ; remainder > 0 ;) {
			int digit = remainder % 26;
			if (digit == 0)
			 {
				digit = 26; // We have a 1-offset (A is 1, Z is 26)
			}
			versionIndex.append((char) (digit + 64));

			remainder = (remainder - digit) / 26;
		}

		return versionIndex.reverse().toString();
	}

}
