package com.cat.fsai.inter.pojo;

/**
 * StandRes
 * @author wangbo
 * @version Mar 11, 2018 10:29:19 PM
 */
public class StandRes {
	/** 是否调用成功*/
	private boolean sucess;
	
	/** 返回信息*/
	private String msg;

	public boolean isSucess() {
		return sucess;
	}

	public void setSucess(boolean sucess) {
		this.sucess = sucess;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
