package com.whz;

import com.whz.entity.Employeer;
import com.whz.mapperinterface.IEmployeerMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisTest {
	private static SqlSessionFactory sqlSessionFactory;
	private static Reader reader;
	private static InputStream inputStream;
	static {
		try {

			/*
			mybatis初始化要经过简单的以下几步：
			1. 调用SqlSessionFactoryBuilder对象的build(inputStream)方法；
			2. SqlSessionFactoryBuilder会根据输入流inputStream等信息创建XMLConfigBuilder对象;
			3. SqlSessionFactoryBuilder调用XMLConfigBuilder对象的parse()方法；
			4. XMLConfigBuilder对象返回Configuration对象；
			5. SqlSessionFactoryBuilder根据Configuration对象创建一个DefaultSessionFactory对象；
			6. SqlSessionFactoryBuilder返回 DefaultSessionFactory对象给Client，供Client使用。
			 */

			inputStream = Resources.getResourceAsStream("mybatis-config.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

			//reader = Resources.getResourceAsReader("mybatis-config.xml");
			//sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEmployeerById(){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Employeer employeer = (Employeer) session.selectOne("com.whz.mapperinterface.IEmployeerMapper.findEmployeerByID", 5);
			//Employeer employeer = session.getMapper(com.whz.mapperinterface.IEmployeerMapper.class).findEmployeerByID(id);
		} finally {
			session.close();
		}
	}

	@Test
	public void testFindEmployeerByDepartmentAndWorktype() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			List<Employeer> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByDepartmentAndWorktype("产品一部","开发工程师");
			System.out.println(employeerList);
		} finally {
			session.close();
		}
	}

	@Test
	public void testFindEmployeerByCondition() {
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			Map condition = new HashMap<String,String>();
			condition.put("age",30);
			List<Map> employeerList = session.getMapper(IEmployeerMapper.class)
					.findEmployeerByCondition(condition);
			System.out.println(employeerList);
		} finally {
			session.close();
		}
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
	@Test
	public void updateTest(){
		Employeer employeer2 = new Employeer();
		employeer2.setEmployeer_id(2);
		employeer2.setEmployeer_age(21);
		employeer2.setEmployeer_department("产品三部");

		updateEmployeer(employeer2);
	}
	@Test
	public void deleteTest(){
		deleteEmployeer(1);
	}




	public static void addEmployeer(Employeer employeer){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			 //返回值是记录条数  
            int resultCount = session.insert("com.whz.mapperinterface.IEmployeerMapper.addEmployeer", employeer );
            System.out.printf("当前插入的employeer_id :%d    当前插入数据库中条数:%d " , employeer.getEmployeer_id() ,resultCount);  //获取插入对象的id
            System.out.println("");
            session.commit() ;  		
		} finally {
			session.close();
		}
		
	}

	public static void deleteEmployeer(int id){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession();
			 //返回值是记录条数  
			 int resultCount=session.delete("com.whz.mapperinterface.IEmployeerMapper.deleteEmployeer",id);
			  System.out.println("当前删除数据库中条数: "+resultCount);  //获取插入对象的id  
            session.commit() ;  		
		} finally {
			session.close();
		}
	}

	public static void updateEmployeer(Employeer employeer){
		SqlSession session = null;
		try {
			session = sqlSessionFactory.openSession(); 
            session.update("com.whz.mapperinterface.IEmployeerMapper.updateEmployeer",employeer);
            session.commit() ;  		
		} finally {
			session.close();
		}
		
	}

	public static void main(String[] args) {



		//查找
		//findEmployeerById(1);

		//删除
		//	deleteEmployeer(1);

		//更改
/*		employeer2.setEid(2);
		employeer2.setEmployeer_age(21);
		employeer2.setEmployeer_department("产品三部");
		updateEmployeer(employeer2);*/

	}
}
