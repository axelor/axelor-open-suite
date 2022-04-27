package com.axelor.apps.tool.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResponseConstructor {

    public static Response build(int statusCode, String message, ApiStructure object) {
        return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ResponseBody(statusCode, message, object))
                .build();
    }

}
