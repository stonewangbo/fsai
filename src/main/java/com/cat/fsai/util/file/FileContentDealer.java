package com.cat.fsai.util.file;

/**
 * @param <T>
 */
public interface FileContentDealer<T> {
    /**
     *
     * @param data 当数据全部读取完毕，最后会传入null代表结束
     */
    void deal(T data);
}
