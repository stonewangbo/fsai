package com.cat.fsai.error;

/**
 * BaseExceptpion 基础错误类
 * @author wangbo
 * @version Feb 16, 2018 4:46:23 PM
 */
public class BaseExceptpion extends RuntimeException {

	/**	 */
	private static final long serialVersionUID = 6552179978780513183L;

	public BaseExceptpion() {
		// TODO Auto-generated constructor stub
	}

	public BaseExceptpion(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public BaseExceptpion(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public BaseExceptpion(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public BaseExceptpion(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
