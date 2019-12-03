# Mybatis之useGeneratedKeys和selectKey的用法



##useGeneratedKeys

在MyBatis中，允许设置名称为“useGeneratedKeys”参数存在3个位置：

1. 在settings元素中设置useGeneratedKeys参数
2. 在xml映射器中设置useGeneratedKeys参数
3. 在接口映射器中设置useGeneratedKeys参数

在不同位置设置的useGeneratedKeys参数，最终结果相同，但是影响范围不同。

### 在settings元素中设置useGeneratedKeys参数

官方的说法是该参数的作用是：“允许JDBC支持自动生成主键，需要驱动兼容”，如何理解这句话的意思？
其本意是说：对于支持自动生成记录主键的数据库，如：MySQL，SQL Server，此时设置useGeneratedKeys参数值为true，在执行添加记录之后可以获取到数据库自动生成的主键ID。
实际上，在settings元素中设置useGeneratedKeys是一个全局参数，但是只会对接口映射器产生影响，对xml映射器不起效。

```xml
<settings>
	<!-- 允许JDBC支持自动生成主键，需要驱动兼容。 
			如果设置为true则这个设置强制使用自动生成主键，尽管一些驱动不能兼容但仍可正常工作（比如 Derby）。 
	-->
	<setting name="useGeneratedKeys" value="true" />
</settings>
```

此时，在接口映射中添加记录之后将返回主键ID。

```java
public interface TestMapper {
    // 受全局useGeneratedKeys参数控制，添加记录之后将返回主键id
    @Insert("insert into test(name,descr,url,create_time,update_time) values(#{name},#{descr},#{url},now(),now())")
    Integer insertOneTest(Test test);
}
```

但是，请注意如果此时在接口映射器中又明确设置了useGeneratedKeys参数，那么注解映射器中的useGeneratedKeys参数值将覆盖settings元素中设置的全局useGeneratedKeys参数值。
举个例子：先在settings元素中设置全局useGeneratedKeys参数值为true，再在接口映射器中设置useGeneratedKeys参数值为false，添加记录之后将不能返回注解ID。

```java
// 在接口映射器中设置的useGeneratedKeys参数值将会覆盖在settings元素中设置全局useGeneratedKeys参数值
@Options(useGeneratedKeys = false, keyProperty = "id", keyColumn = "id")
@Insert("insert into test(name,descr,url,create_time,update_time) values(#{name},#{descr},#{url},now(),now())")
Integer insertOneTest(Test test);
```

另外，在settings元素中设置的全局useGeneratedKeys参数对于xml映射器无效。如果希望在xml映射器中执行添加记录之后返回主键ID，则必须在xml映射器中明确设置useGeneratedKeys参数值为true。

### 在xml映射器中配置useGeneratedKeys参数

```java
<!-- 插入数据:返回记录的id值 -->
<insert id="insertOneTest" parameterType="org.chench.test.mybatis.model.Test" useGeneratedKeys="true" keyProperty="id" keyColumn="id">
    insert into test(name,descr,url,create_time,update_time) 
    values(#{name},#{descr},#{url},now(),now())
</insert>
```

xml映射器中配置的useGeneratedKeys参数只会对xml映射器产生影响，且在settings元素中设置的全局useGeneratedKeys参数值对于xml映射器不产生任何作用。

### 在接口映射器中设置useGeneratedKeys参数

```java
// 设置useGeneratedKeys为true，返回数据库自动生成的记录主键id
@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
@Insert("insert into test(name,descr,url,create_time,update_time) values(#{name},#{descr},#{url},now(),now())")
Integer insertOneTest(Test test);
```

**注意：** 在接口映射器中设置的useGeneratedKeys参数会覆盖在`<settings>`元素中设置的对应参数值。









##SelectKey

SelectKey在Mybatis中是为了解决Insert数据时不支持主键自动生成的问题，他可以很随意的设置生成主键的方式。

不管SelectKey有多好，尽量不要遇到这种情况吧，毕竟很麻烦。

| 属性            | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| `keyProperty`   | selectKey 语句结果应该被设置的目标属性。                     |
| `resultType`    | 结果的类型。MyBatis 通常可以算出来,但是写上也没有问题。MyBatis 允许任何简单类型用作主键的类型,包括字符串。 |
| `order`         | 这可以被设置为 BEFORE 或 AFTER。如果设置为 BEFORE，那么它会首先选择主键，设置 keyProperty 然后执行插入语句。如果设置为 AFTER，那么先执行插入语句，然后是 selectKey 元素-这和如 Oracle 数据库相似，可以在插入语句中嵌入序列调用。 |
| `statementType` | 和前面的相同，MyBatis 支持 STATEMENT，PREPARED 和CALLABLE 语句的映射类型，分别代表 PreparedStatement 和CallableStatement 类型。 |

SelectKey需要注意order属性，像Mysql一类支持自动增长类型的数据库中，order需要设置为after才会取到正确的值。

像Oracle这样取序列的情况，需要设置为before，否则会报错。

另外在用Spring管理事务时，SelectKey和插入在同一事务当中，因而Mysql这样的情况由于数据未插入到数据库中，所以是得不到自动增长的Key。取消事务管理就不会有问题。

下面是一个xml和注解的例子，SelectKey很简单，两个例子就够了：

```xml
<insert id="insert" parameterType="map">  
    insert into table1 (name) values (#{name})  
    <selectKey resultType="java.lang.Integer" keyProperty="id">  
      CALL IDENTITY()  
    </selectKey>  
</insert>
```

上面xml的传入参数是map，selectKey会将结果放到入参数map中。用POJO的情况一样，但是有一点需要注意的是，keyProperty对应的字段在POJO中必须有相应的setter方法，setter的参数类型还要一致，否则会报错。

```java
@Insert("insert into table2 (name) values(#{name})")  
@SelectKey(statement="call identity()", keyProperty="nameId", before=false, resultType=int.class)  
int insertTable2(Name name); 
```

上面是注解的形式。



在使用ibatis插入数据进数据库的时候，会用到一些sequence的数据，有些情况下，在插入完成之后还需要将sequence的值返回，然后才能进行下一步的操作。 使用ibatis的selectKey就可以得到sequence的值，同时也会将值返回。不过对于不同的数据库有不同的操作方式。 
      

###对于oracle

```xml
<insert id="insertUser" parameterClass="ibatis.User"> 
		<selectKey resultClass="long" keyProperty="id"> 
    		select SEQ_USER_ID.nextval as id from dual 
    </selectKey> 
    insert into user(id,name,password) values (#id#,#name#,#password#) 
</insert> 
```

该句话执行完之后，传进来的参数User对象DO里的id字段就会被赋值成sequence的值。





###对于mysql

```xml
<insert id="insertUser" parameterClass="ibatis.User"> 
		insert into user(name,password) values(#name#,#password#) 
		<selectKey resultClass="long" keyProperty="id">  
				SELECT LAST_INSERT_ID() AS ID  
    </selectKey>  
</insert> 
```

将selectKey放在insert之后，通过LAST_INSERT_ID() 获得刚插入的自动增长的id的值，这种方式可能存在并发问题。



**注意：SelectKey需要注意order属性，像Mysql一类支持自动增长类型的数据库中，order需要设置为after才会取到正确的值。像Oracle这样取序列的情况，需要设置为before，否则会报错。**









