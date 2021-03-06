/**
 * 
 */
package com.joindata.inf.common.support.idgen.core;

/**
 * 
 * @author <a href="mailto:gaowei1@joindata.com">高伟</a>
 * @date 2017年3月27日
 */
public interface SequenceRepository
{

    public boolean exist(String key) throws Exception;

    public long increaseAndGet(String key, long addValue) throws Exception;

    public long get(String key) throws Exception;

    long set(String key, long value) throws Exception;

    public long getAndIncrease(String key, long addValue) throws Exception;

    public void delete(String key) throws Exception;
}
