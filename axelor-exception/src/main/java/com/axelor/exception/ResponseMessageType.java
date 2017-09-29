package com.axelor.exception;

import java.util.function.BiConsumer;

import com.axelor.rpc.ActionResponse;

public enum ResponseMessageType {
	INFORMATION(ActionResponse::setFlash),
	WARNING(ActionResponse::setAlert),
	ERROR(ActionResponse::setError),
	NOTIFICATION(ActionResponse::setNotify);

	private BiConsumer<ActionResponse, String> messageMethod;

	ResponseMessageType(BiConsumer<ActionResponse, String> messageMethod) {
		this.messageMethod = messageMethod;
	}

	public void setMessage(ActionResponse response, String message) {
		messageMethod.accept(response, message);
	}

}
