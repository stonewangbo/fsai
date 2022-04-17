/**
 * 
 */
package com.cat.fsai.inter;

import com.cat.fsai.inter.pojo.StandRes;

/**
 * ProgressRes
 * @author wangbo
 * @version Mar 11, 2018 10:28:38 PM
 */
@FunctionalInterface
public interface ProgressRes {
  void res(Double progress);
}
