package com.cat.fsai.util.http;

@FunctionalInterface
public interface HttpRes {
	void parseRes(String res,Exception error);
}
