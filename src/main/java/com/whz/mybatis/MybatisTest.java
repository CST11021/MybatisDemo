package com.whz.mybatis;

import com.whz.mybatis.entity.Employeer;
import com.whz.mybatis.dao.IEmployeerMapper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class MybatisTest {

	private static Reader reader;
	private static InputStream inputStream;
	private static SqlSessionFactory sqlSessionFactory;
	static {
		try {

			/*
			mybatis初始化要经过简单的以下几步：

			1.使用Resources类去加载配置文件，返回一个InputStream流对象；

			2.SqlSessionFactoryBuilder使用build(inputStream)方法创建一个SqlSessionFactory：build()方法内部使用XMLConfigBuilder对象进行解析，
			 然后返回一个Configuration对象，然后，SqlSessionFactoryBuilder再根据Configuration对象创建SqlSessionFactory的对象
			（SqlSessionFactoryBuilder创建时，都是用DefaultSqlSessionFactory实现类，且该实现类创建的SqlSession时，都使用DefaultSqlSession实现类）;

			3.SqlSessionFactory 调用openSession方法创建一个SqlSession（）实例，创建是会根据ExecutorType、TransactionIsolationLevel和autoCommit三个参数进行创建

			4.接下来SqlSession就可以进行一系列的增删改查操作了：SqlSession内部委托给了Executor的doUpdate、doQuery和doQueryCursor进行

			*/

			// 方案一：返回字节流
			inputStream = Resources.getResourceAsStream("mybatis-config.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

			// 方案二：返回字符流
			//reader = Resources.getResourceAsReader("mybatis-config.xml");
			//sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 测试一级会话缓存：如果使用同一个SqlSession对象进行两个相同的查询操作，则第二次会走缓存
	 */
	@Test
	public void testSessionCache() {

		SqlSession sqlSession = sqlSessionFactory.openSession();

		IEmployeerMapper iEmployeerMapper = sqlSession.getMapper(IEmployeerMapper.class);
		List<Employeer> employeers = iEmployeerMapper.findAllEmployeer();

		// SqlSession sqlSession1 = sqlSessionFactory.openSession();
		sqlSession.clearCache();

		IEmployeerMapper iEmployeerMapper1 = sqlSession.getMapper(IEmployeerMapper.class);
		List<Employeer> employeers1 = iEmployeerMapper1.findAllEmployeer();

		System.out.println(employeers);
		System.out.println(employeers1);
	}

	@Test
	public void testFindAllEmployeerByPage() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();

			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class).findAllEmployeerByPage(0,5, "employeer_age");
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}
	@Test
	public void testEmployeerById(){
		SqlSession session = sqlSessionFactory.openSession();
		Employeer employeer = session.selectOne("findEmployeerByID", 5);
		//Employeer employeer = session.selectOne("com.whz.mybatis.IEmployeerMapper.findEmployeerByID", 5);
		//Employeer employeer = session.getMapper(com.whz.mybatis.IEmployeerMapper.class).findEmployeerByID(5);
		session.close();
	}
	@Test
	public void testFindEmployeerByDepartmentAndWorktype() {
		SqlSession session = sqlSessionFactory.openSession();
			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByDepartmentAndWorktype("产品一部","开发工程师");
			System.out.println(employeerList);
			session.close();
	}
	@Test
	public void testFindEmployeerByCondition1() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Map condition = new HashMap<String,String>();
			condition.put("age",30);
			List<Map> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByCondition1(condition);
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}
	@Test
	public void testFindEmployeerByCondition2() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Map condition = new HashMap<String,String>();
			condition.put("age",30);
			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByCondition2(condition);
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}
	@Test
	public void testFindEmployeerByCondition3() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Employeer condition = new Employeer();
			condition.setEmployeer_age(30);
			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByCondition3(condition);
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}
	@Test
	public void testFindEmployeerByCondition4() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Employeer condition = new Employeer();
			condition.setEmployeer_age(30);
			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByCondition4(condition, 0, 3);
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}
	@Test
	public void testSelectMap() {
		Map params = new HashMap();
		params.put("department","产品一部");
		params.put("worktype","开发工程师");

		SqlSession session = sqlSessionFactory.openSession();
		Map<String, Map<String, String>> result = session.selectMap("findEmployeerByDepartmentAndWorktype", params, "employeer_id");
		System.out.println(result);

		session.close();

	}
	@Test
	public void testSelectCursor() throws IOException {
		SqlSession session = sqlSessionFactory.openSession();
		Cursor<Employeer> cursor = session.selectCursor("findAllEmployeer");

		List<Employeer> result = new ArrayList(10);
		Iterator<Employeer> iter = cursor.iterator();
		while (iter.hasNext()) {
			iter.next();
			result.add(iter.next());
		}
		System.out.println(result);

		cursor.close();
	}

	@Test
	public void addTest(){
		Employeer employeer1=new Employeer();
		employeer1.setEmployeer_name("李四");
		employeer1.setEmployeer_age(23);
		employeer1.setEmployeer_department("产品一部");
		employeer1.setEmployeer_worktype("开发工程师");

		Employeer employeer2=new Employeer();
		employeer2.setEmployeer_name("张三");
		employeer2.setEmployeer_age(30);
		employeer2.setEmployeer_department("产品二部");
		employeer2.setEmployeer_worktype("测试工程师");

		Employeer employeer3=new Employeer();
		employeer3.setEmployeer_name("小王");
		employeer3.setEmployeer_age(22);
		employeer3.setEmployeer_department("产品三部");
		employeer3.setEmployeer_worktype("数据分析师");


		Employeer employeer4=new Employeer();
		employeer4.setEmployeer_name("明明");
		employeer4.setEmployeer_age(22);
		employeer4.setEmployeer_department("财会部");
		employeer4.setEmployeer_worktype("财务人员");

		//插入
		addEmployeer(employeer1);
		addEmployeer(employeer2);
		addEmployeer(employeer3);
		addEmployeer(employeer4);
	}
	private void addEmployeer(Employeer employeer){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			//返回值是记录条数
			int resultCount = session.insert("com.whz.mybatis.IEmployeerMapper.addEmployeer", employeer );
			System.out.printf("当前插入的employeer_id :%d    当前插入数据库中条数:%d " , employeer.getEmployeer_id() ,resultCount);  //获取插入对象的id
			System.out.println("");
			session.commit() ;
		} finally {
			session.close();
		}

	}

	@Test
	public void updateTest(){
		Employeer employeer2 = new Employeer();
		employeer2.setEmployeer_id1(2);
		employeer2.setEmployeer_age(21);
		employeer2.setEmployeer_department("产品三部");

		updateEmployeer(employeer2);
	}
	private void updateEmployeer(Employeer employeer){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			session.update("com.whz.mybatis.mapperinterface.IEmployeerMapper.updateEmployeer",employeer);
			session.commit() ;
		} finally {
			session.close();
		}

	}

	@Test
	public void deleteTest(){
		deleteEmployeer(11);
	}
	private void deleteEmployeer(int id){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			//返回值是记录条数
			int resultCount=session.delete("com.whz.mybatis.IEmployeerMapper.deleteEmployeer",id);
			System.out.println("当前删除数据库中条数: "+resultCount);  //获取插入对象的id
			session.commit() ;
		} finally {
			session.close();
		}
	}

	@Test
	public void test() throws Exception {

		Employeer employeer = new Employeer();
		employeer.setEmployeer_age(56);
		employeer.setEmployeer_name("王五");

		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			//返回值是记录条数
			int resultCount = session.insert("com.whz.mybatis.mapperinterface.IEmployeerMapper.addEmployeer", employeer);
			System.out.printf("当前插入的employeer_id :%d    当前插入数据库中条数:%d " , employeer.getEmployeer_id() ,resultCount);  //获取插入对象的id
			session.commit() ;
		} finally {
			session.close();
		}

	}

}
