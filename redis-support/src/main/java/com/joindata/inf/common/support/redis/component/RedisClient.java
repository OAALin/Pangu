package com.joindata.inf.common.support.redis.component;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Redis 客户端工具
 * 
 * @author Muyv
 * @date 2016年2月16日 下午6:22:57
 */
public abstract class RedisClient
{
    private String namespace;

    protected String getRealKey(String key)
    {
        if(namespace != null)
        {
            return namespace + ":" + key;
        }
        return key;
    }

    /**
     * 设置命名空间
     * 
     * @param namespace 命名空间
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * 获取 Jedis 原生对象 <br />
     * <i>由于 Jedis 操蛋的 API 影响，这个返回值只能设置成泛型，所以在取的时候，请确认你的返回值是用的 JedisCluster 还是单点</i>
     * 
     * @return
     */
    public abstract <T> T getJedis();

    /**
     * 设置字符串
     * 
     * @param key 键
     * @param value 值
     */
    // asdf
    public abstract void put(String key, String value);

    /**
     * 设置会超时的字符串
     * 
     * @param key 键
     * @param seconds 超时秒数
     * @param value 值
     * @return setex 的响应码
     */
    public abstract String putWithExpire(String key, int seconds, String value);

    /**
     * 设置字符串，当键不存在时
     * 
     * @param key 键
     * @param value 值
     * @return true，如果原值不存在且新值设置成功
     * @return 1，如果原值不存在且新值设置成功。否则 0
     */
    public abstract long putIfNone(String key, String value);

    /**
     * 设置会超时的字符串，当键不存在时<br />
     * <i>该方法不是原生的，可能不安全，请酌情使用<i>
     * 
     * @param key 键
     * @param seconds 超时秒数
     * @param value 值
     * @return setex 的响应码
     */
    // TODO 考虑调用原生 API 做成安全的
    public abstract String putWithExpireIfNone(String key, int seconds, String value);

    /**
     * 获取字符串
     * 
     * @param key 键
     */
    public abstract String getString(String key);

    /**
     * 删除数据
     * 
     * @param key 键
     */
    public abstract void delete(String key);

    /**
     * 存储对象
     * 
     * @param key 键
     * @param obj 可序列化对象
     */
    public abstract void put(String key, Serializable obj);

    /**
     * 获取对象
     * 
     * @param key 键
     * @param clz 对象的 Class
     * @return 指定对象实例
     */
    public abstract <T extends Serializable> T get(String key, Class<T> clz);

    /**
     * 存储会超时的对象
     * 
     * @param key 键
     * @param seconds 多少秒后超时
     * @param obj 可序列化对象
     * @return setex 的响应码
     */
    public abstract String putWithExpire(String key, int seconds, Serializable obj);

    /**
     * 存储对象，当键不存在时
     * 
     * @param key 键
     * @param obj 可序列化对象
     * @return 1，如果原值不存在且新值设置成功。否则 0
     */
    public abstract long putIfNone(String key, Serializable obj);

    /**
     * 存储会超时的对象，当键不存在时<br />
     * <i>该方法不是原生的，可能不安全，请酌情使用<i>
     * 
     * @param key 键
     * @param seconds 多少秒后超时
     * @param obj 可序列化对象
     * @return setex 的响应码
     */
    // TODO 考虑调用原生 API 做成安全的
    public abstract String putWithExpireIfNone(String key, int seconds, Serializable obj);

    /**
     * 存储字符串序列（从前插入）<br />
     * <i>values 参数将倒序遍历</i>
     * 
     * @param key 键
     * @param List<String> 字符串列表
     */
    public abstract void prependToList(String key, List<String> values);

    /**
     * 存储字符串序列（从前插入）<br />
     * <i>values 参数将倒序遍历</i>
     * 
     * @param key 键
     * @param values 字符串数组
     */
    public abstract void prependToList(String key, String... values);

    /**
     * 存储字符串序列（后入式）<br />
     * <i>values 参数将顺序遍历</i>
     * 
     * @param key 键
     * @param values 字符串列表
     */
    public abstract void appendToList(String key, List<String> values);

    /**
     * 存储字符串序列（后入式）<br />
     * <i>values 参数将顺序遍历</i>
     * 
     * @param key 键
     * @param values 字符串数组
     */
    public abstract void appendToList(String key, String... values);

    /**
     * 获取列表，指定起始结束位置
     * 
     * @param key 键
     * @param start 起始，从 0 开始
     * @param end 结束，如果越界将取最大长度
     * @return 字符串数组
     */
    public abstract List<String> getList(String key, int start, int end);

    /**
     * 获取整个列表
     * 
     * @param key 键
     * @return 字符串数组
     */
    public abstract List<String> getList(String key);

    /**
     * 获取列表，从指定位置截取至最后
     * 
     * @param key 键
     * @param start 起始，从 0 开始
     * @return 字符串数组
     */
    public abstract List<String> getSubList(String key, int start);

    /**
     * 获取列表长度
     * 
     * @param key 键
     * @return 列表长度
     */
    public abstract long getListSize(String key);

    /**
     * 存储对象序列（从前插入）<br />
     * <i>values 参数将倒序遍历</i>
     * 
     * @param key 键
     * @param values 对象列表
     */
    public abstract void prependObjectToList(String key, List<? extends Serializable> values);

    /**
     * 存储对象序列（从前插入）<br />
     * <i>values 参数将倒序遍历</i>
     * 
     * @param key 键
     * @param values 对象数组
     */
    public abstract void prependObjectToList(String key, Serializable... values);

    /**
     * 存储对象序列（后入式）<br />
     * <i>values 参数将顺序遍历</i>
     * 
     * @param key 键
     * @param values 对象列表
     */
    public abstract void appendObjectToList(String key, List<? extends Serializable> values);

    /**
     * 存储对象序列（后入式）<br />
     * <i>values 参数将顺序遍历</i>
     * 
     * @param key 键
     * @param values 对象数组
     */
    public abstract void appendObjectToList(String key, Serializable... values);

    /**
     * 获取列表，指定起始结束位置
     * 
     * @param key 键
     * @param start 起始，从 0 开始
     * @param end 结束，如果越界将取最大长度
     * @param clz 对象的 Class
     * @return 对象列表
     */
    public abstract <T extends Serializable> List<T> getObjectList(String key, int start, int end, Class<T> clz);

    /**
     * 获取整个列表
     * 
     * @param key 键
     * @param clz 对象的 Class
     * @return 对象列表
     */
    public abstract <T extends Serializable> List<T> getObjectList(String key, Class<T> clz);

    /**
     * 获取列表，从指定位置截取至最后
     * 
     * @param key 键
     * @param start 起始，从 0 开始
     * @return 对象数组
     */
    public abstract <T extends Serializable> List<T> getObjectSubList(String key, int start, Class<T> clz);

    /**
     * 存储字符串 Map
     * 
     * @param key 键
     * @param map Map<String, String> 对象
     */
    public abstract void putMap(String key, Map<String, String> map);

    /**
     * 获取 Map 值
     * 
     * @param key 键
     * @param entries Map 的 key
     * @return 值
     */
    public abstract String getMapValue(String key, String entry);

    /**
     * 获取 Map 值列表
     * 
     * @param key 键
     * @param entries Map 的 key 列表
     * @return 值列表
     */
    public abstract List<String> getMapValues(String key, String... entries);

    /**
     * 获取整个字符串 Map
     * 
     * @param key 键
     * @return 整个字符串 Map
     */
    public abstract Map<String, String> getMap(String key);

    /**
     * 删除 Map 中的元素
     * 
     * @param key 键
     * @param entries Map 的 key 列表
     */
    public abstract void deleteMapItem(String key, String... entries);

    /**
     * 存储对象 Map，Map 类型为 Map<String, ? extends Serializable>
     * 
     * @param key 键
     * @param map Map<String, ? extends Serializable> 对象
     */
    public abstract void putObjectMap(String key, Map<String, ? extends Serializable> map);

    /**
     * 获取对象 Map 值列表
     * 
     * @param key 键
     * @param clz 值的 Class
     * @param entries Map 的 key 列表
     * @return 值列表
     */
    public abstract <T extends Serializable> List<T> getObjectMapValues(String key, Class<T> clz, String... entries);

    /**
     * 获取整个对象 Map
     * 
     * @param key 键
     * @param clz 值的 Class
     * @return 整个对象 Map
     */
    public abstract <T extends Serializable> Map<String, T> getObjectMap(String key, Class<T> clz);

    /**
     * 获取对象 Map 值
     * 
     * @param key 键
     * @param clz 值的 Class
     * @param entries Map 的 key
     * @return 值
     */
    public abstract <T extends Serializable> T getObjectMapValue(String key, Class<T> clz, String entry);

    /**
     * 序列值 +1
     * 
     * @param key 键
     * @return +1 后的序列
     */
    public abstract Long incr(String key);

    /**
     * 设置新值，返回旧值
     * 
     * @param key 键
     * @param newValue 新值
     * @return 旧值
     */
    public abstract String getSet(String key, String newValue);

    /**
     * 设置新值，返回旧值
     * 
     * @param key 键
     * @param newValue 新值
     * @return 旧值
     */
    public abstract <T extends Serializable> T getSet(String key, T newValue);

    /**
     * 推入队列
     * 
     * @param key 队列名
     * @param value 队列元素，可传多个
     * @return 推了几个
     */
    public abstract long leftPush(String key, String... value);

    /**
     * 推入队列
     * 
     * @param key 队列名
     * @param value 队列元素，可传多个
     * @return 推了几个
     */
    @SuppressWarnings("unchecked")
    public abstract <T extends Serializable> long leftPush(String key, T... value);

    /**
     * 弹出队列
     * 
     * @param key 队列名
     * @param value 队列元素，可传多个
     * @return 弹出的值
     */
    public abstract String leftPop(String key);

    /**
     * 弹出队列
     * 
     * @param key 队列名
     * @param clz 弹出后的对象 Class
     * @return 弹出的对象
     */
    public abstract <T extends Serializable> T leftPop(String key, Class<T> clz);

    /**
     * 设置某个 key 的超时秒数
     * 
     * @param key 键
     * @param seconds 秒数
     * @return expire 设置成功返回 1，如果 key 不存在或版本不支持，返回 0， 如果 key 为null，返回 -1
     */
    public abstract long expire(String key, int seconds);

    public static void main(String[] args) throws IOException
    {
        // class Dog implements Serializable
        // {
        // private static final long serialVersionUID = -3227839738295908954L;
        //
        // private String name;
        //
        // private int age;
        //
        // private boolean male;
        //
        // @SuppressWarnings("unused")
        // private Dog()
        // {
        // }
        //
        // public abstract Dog(String name, int age, boolean male)
        // {
        // this.name = name;
        // this.age = age;
        // this.male = male;
        // }
        //
        // @Override
        // public abstract String toString()
        // {
        // return "Dog [name=" + name + ", age=" + age + ", male=" + male + "]";
        // }
        // }

        // RedisProperties props = new RedisProperties();
        // rediscluster redis = new RedisClient(new ShardedJedisPool(new GenericObjectPoolConfig(), CollectionUtil.newList()));
        //
        // redis.put("A", "1");
        // redis.put("AA", new Dog("拉登", 2, false));
        //
        // System.err.println(redis.getString("A"));
        // redis.delete("A");
        // System.err.println(redis.getString("A"));
        //
        // System.err.println(redis.get("AA", Dog.class));
        // redis.delete("AA");
        // System.err.println(redis.get("AA", Dog.class));
        //
        // redis.prependToList("B", "1", "2", "3");
        // redis.prependObjectToList("BB", CollectionUtil.newList(new Dog("拉登", 1, false), new Dog("拉登", 2, false), new Dog("拉登", 3, true)));
        // redis.prependObjectToList("BBB", new Dog("拉登", 1, false), new Dog("拉登", 2, false), new Dog("拉登", 3, true));
        //
        // System.err.println(redis.getList("B"));
        // redis.delete("B");
        // System.err.println(redis.getList("B"));
        //
        // System.err.println(redis.getObjectList("BB", Dog.class));
        // redis.delete("BB");
        // System.err.println(redis.getObjectList("BB", Dog.class));
        //
        // System.err.println(redis.getObjectList("BBB", Dog.class));
        // redis.delete("BBB");
        // System.err.println(redis.getObjectList("BBB", Dog.class));
        //
        // redis.appendToList("C", "1", "2", "3");
        // redis.appendObjectToList("CC", CollectionUtil.newList(new Dog("拉登", 1, false), new Dog("拉登", 2, false), new Dog("拉登", 3, true)));
        // redis.appendObjectToList("CCC", new Dog("拉登", 1, false), new Dog("拉登", 2, false), new Dog("拉登", 3, true));
        //
        // System.err.println(redis.getListSize("C"));
        // System.err.println(redis.getListSize("CC"));
        // System.err.println(redis.getListSize("CCC"));
        //
        // System.err.println(redis.getList("C"));
        // redis.delete("C");
        // System.err.println(redis.getList("C"));
        //
        // System.err.println(redis.getObjectList("CC", Dog.class));
        // redis.delete("CC");
        // System.err.println(redis.getObjectList("CC", Dog.class));
        //
        // System.err.println(redis.getObjectList("CCC", Dog.class));
        // redis.delete("CCC");
        // System.err.println(redis.getObjectList("CCC", Dog.class));
        //
        // redis.putMap("D", CollectionUtil.newMap("a", "b"));
        // redis.putObjectMap("DD", CollectionUtil.newMap(ArrayUtil.make("A", "B", "C"), ArrayUtil.make(new Dog("拉登", 1, false), new Dog("拉登", 2, false), new Dog("拉登", 3, true))));
        //
        // System.err.println(redis.getMapValues("D", "A", "B"));
        // System.err.println(redis.getMapValue("D", "C"));
        // System.err.println(redis.getMap("D"));
        // redis.delete("D");
        // System.err.println(redis.getMapValues("D", "A", "B"));
        // System.err.println(redis.getMapValues("D", "C"));
        // System.err.println(redis.getMap("D"));
        //
        // System.err.println(redis.getObjectMapValues("DD", Dog.class, "A", "B"));
        // System.err.println(redis.getObjectMapValue("DD", Dog.class, "C"));
        // System.err.println(redis.getObjectMap("DD", Dog.class));
        // redis.delete("DD");
        // System.err.println(redis.getObjectMapValues("DD", Dog.class, "A", "B"));
        // System.err.println(redis.getObjectMapValue("DD", Dog.class, "C"));
        // System.err.println(redis.getObjectMap("DD", Dog.class));
        //
        // System.err.println(redis.incr("E"));
        // System.err.println(redis.incr("E"));
        // redis.delete("E");
        // System.err.println(redis.getString("E"));
    }
}
