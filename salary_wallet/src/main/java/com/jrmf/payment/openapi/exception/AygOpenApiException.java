/**
 * 
 */
package com.jrmf.payment.openapi.exception;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2016年10月25日
 */
public class AygOpenApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AygOpenApiException() {
		super();
	}

	public AygOpenApiException(String message) {
		super(message);
	}
	
	public AygOpenApiException(String message, Throwable cause) {
		super(message, cause);
	}

}
