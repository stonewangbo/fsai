package com.cat.fsai.inter;

		import com.cat.fsai.inter.pojo.DepthGroup;
		import com.cat.fsai.inter.pojo.KLine;

		import java.util.List;

/**
 * DepthRes
 * @author wangbo
 * @version Feb 15, 2018 9:06:46 PM
 */
@FunctionalInterface
public interface KLineRes {

	/**
	 * 获得深度数据
	 * @param kLines
	 */
	void kLine(List<KLine> kLines, Throwable e);
}
