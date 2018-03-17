package com.cat.fsai.inter.pojo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.cat.fsai.error.BussException;

public class AsyncObject<T> {
	private CountDownLatch cdl;
	private String name;
	private Exception e;	
	private T obj;
	public AsyncObject(String name,int size,T obj) {
		super();
		this.name = name;
		this.cdl = new CountDownLatch(size);		
		this.obj = obj;
	}
	public Exception getE() {
		return e;
	}
	public void setE(Exception e) {
		this.e = e;
	}
	public T getObj() {		
		return obj;
	}
	public void setObj(T obj) {
		this.obj = obj;
	}
	public CountDownLatch getCdl() {
		return cdl;
	}
	public String getName() {
		return name;
	}
	public T waitAndGet(long millsecend) throws Exception{
		if(e!=null) throw new BussException(name+"出现异常",e);
		if(!cdl.await(millsecend, TimeUnit.MILLISECONDS))throw new BussException(name+"超时");				
		return obj;
	}
}
