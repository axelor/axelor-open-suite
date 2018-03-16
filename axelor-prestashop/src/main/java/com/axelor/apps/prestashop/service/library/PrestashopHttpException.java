package com.axelor.apps.prestashop.service.library;

/**
 * Specific subclass used when an HTTP communication error
 * occurs. This allows caller to get the error code back.
 */
public class PrestashopHttpException extends PrestaShopWebserviceException {
	private static final long serialVersionUID = 461341245740997416L;

	private int statusCode;

	public PrestashopHttpException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public PrestashopHttpException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
