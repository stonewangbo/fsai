package com.cat.fsai.error;

/**
 * ApiException 交易所api错误
 * @author wangbo
 * @version Feb 16, 2018 4:49:33 PM
 */
public class ApiException extends BaseExceptpion {

	/**	 */
	private static final long serialVersionUID = -8519133478813942382L;

	public ApiException() {
		// TODO Auto-generated constructor stub
	}

	public ApiException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ApiException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
