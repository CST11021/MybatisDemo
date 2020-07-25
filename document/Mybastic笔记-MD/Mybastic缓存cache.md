# Mybastic缓存cache的使用

参考文档：https://ethendev.github.io/2017/02/13/mysql-cache/



​		MyBatis提供了缓存机制减轻数据库压力，提高数据库性能，但在没有配置的默认情况下，它只开启一级缓存。

​	mybatis的缓存分为两级：一级缓存、二级缓存

* 一级缓存是SqlSession级别的缓存，缓存的数据只在SqlSession内有效

* 二级缓存是mapper级别的缓存，同一个namespace公用这一个缓存，所以对SqlSession是共享的

​		在sql和参数完全一样的情况下，使用同一个SqlSession对象调用同一个Mapper的方法，只会执行一次Sql，因为使用SqlSession第一次查询后，MyBatis会将其放在缓存中，以后在查询的时候，如果没有申明需要刷新，并且缓存没有超时的情况下，SqlSession都只会取出当前缓存的数据，而不会再次发送sql到数据库。但如果不是一个SqlSession对象，因为不同SqlSession都是相互隔离的，所以一级缓存失效。

​		二级缓存是在SqlSessionFactory层面上的缓存，默认不开启，开启的话只需要在映射XML文件中配置，开启缓存即可。

 

**Mybatis的一级缓存和二级缓存执行顺序**

1、先判断二级缓存是否开启，如果没开启，再判断一级缓存是否开启，如果没开启，直接查数据库

2、如果一级缓存关闭，即使二级缓存开启也没有数据，因为二级缓存的数据从一级缓存获取

3、一般不会关闭一级缓存

4、二级缓存默认不开启

5、如果二级缓存关闭，直接判断一级缓存是否有数据，如果没有就查数据库

6、如果二级缓存开启，先判断二级缓存有没有数据，如果有就直接返回；如果没有，就查询一级缓存，如果有就返回，没有就查询数据库；

 

**一级缓存：**

　　mybatis的一级缓存是SqlSession级别的缓存，在操作数据库的时候需要先创建SqlSession会话对象，在对象中有一个HashMap用于存储缓存数据，此HashMap是当前会话对象私有的，别的SqlSession会话对象无法访问。

　　![img](https://images2017.cnblogs.com/blog/663108/201709/663108-20170912191237032-1438769206.png)

 

具体流程：

​	1.第一次执行select完毕会将查到的数据写入SqlSession内的HashMap中缓存起来

​	2.第二次执行select会从缓存中查数据，如果select相同切传参数一样，那么就能从缓存中返回数据，不用去数据库了，从而提高了效率。

注意事项：

​	1.如果SqlSession执行了DML操作（insert、update、delete），并commit了，那么mybatis就会清空当前SqlSession缓存中的所有缓存数据，这样可以保证缓存中的存的数据永远和数据库中一致，避免出现脏读

​	2.当一个SqlSession结束后那么他里面的一级缓存也就不存在了，mybatis默认是开启一级缓存，不需要配置

​	3.mybatis的缓存是基于[namespace:sql语句:参数]来进行缓存的，意思就是，SqlSession的HashMap存储缓存数据时，是使用[namespace:sql:参数]作为key，查询返回的语句作为value保存的。例如：-1242243203:1146242777:winclpt.bean.userMapper.getUser:0:2147483647:select * from user where id=?:19

 

**二级缓存：**

　　二级缓存是mapper级别的缓存，也就是同一个namespace的mappe.xml，当多个SqlSession使用同一个Mapper操作数据库的时候，得到的数据会缓存在同一个二级缓存区域

　　二级缓存默认是没有开启的。需要在setting全局参数中配置开启二级缓存

conf.xml：

```xml
<settings>
	<setting name="cacheEnabled" value="true"/>默认是false：关闭二级缓存
<settings>
```

在userMapper.xml中配置：

```xml
<cache eviction="LRU" flushInterval="60000" size="512" readOnly="true"/>
当前mapper下所有语句开启二级缓存
```

讨论一下<cache/>的属性：

- eviction：代表的是缓存收回策略，有一下策略：
  1. LRU， 最近最少使用的，移除最长时间不用的对象。
  2. FIFO，先进先出，按对象进入缓存的顺序来移除他们
  3. SOFT， 软引用，移除基于垃圾回收器状态和软引用规则的对象。
  4. WEAK，若引用，更积极的移除基于垃圾收集器状态和若引用规则的对象
- flushInterval：缓存自动清除的时间间隔，单位为毫秒，默认是当sql执行的时候才回去刷新。
- size：引用数目，一个正整数，代表缓存最多可以存储多少对象，不宜设置过大，过大会造成内存溢出。
- readOnly：只读，意味着缓存数据只能读取，不能修改，这样设置的好处是我们可以快速读取缓存，去诶但是我们没有办法修改缓存。默认值为false，不允许我们修改。

这里配置了一个LRU缓存，并每隔60秒刷新，最大存储512个对象，而却返回的对象是只读的。



**配置指定SQL禁用二级缓存**

若想禁用当前select语句的二级缓存，添加useCache="false"修改如下：

```xml
<select id="getCountByName" parameterType="java.util.Map" useCache="false">
```

具体流程：

​	1.当一个sqlseesion执行了一次select后，在关闭此session的时候，会将查询结果缓存到二级缓存；

​	2.当另一个sqlsession执行select时，首先会查二级缓存，再查一级缓存，再查数据库；即使在一个sqlSession中，也会先查二级缓存；一个namespace中的查询更是如此；

 **所以说，缓存执行顺序是：二级缓存-->一级缓存-->数据库**



**注意事项：**

1.如果SqlSession执行了DML操作（insert、update、delete），并commit了，那么mybatis就会清空当前mapper缓存中的所有缓存数据，这样可以保证缓存中的存的数据永远和数据库中一致，避免出现脏读

2.mybatis的缓存是基于[namespace:sql语句:参数]来进行缓存的，意思就是，SqlSession的HashMap存储缓存数据时，是使用[namespace:sql:参数]作为key，查询返回的语句作为value保存的。例如：-1242243203:1146242777:winclpt.bean.userMapper.getUser:0:2147483647:select * from user where id=?:19





