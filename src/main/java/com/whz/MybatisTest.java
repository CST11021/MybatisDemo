package com.whz;

import com.whz.entity.Employeer;
import com.whz.handover.CountPlanDischargeNumberDO;
import com.whz.handover.HandoverPlanDO;
import com.whz.loading.LoadingDO;
import com.whz.mapperinterface.HandoverPlanMapper;
import com.whz.mapperinterface.IEmployeerMapper;
import com.whz.mapperinterface.LoadingMapper;
import com.whz.mapperinterface.TransportStationMapper;
import com.whz.transport.TransportStationDO;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
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



	@Test
	public void testLoadingInsert() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		LoadingMapper mapper = sqlSession.getMapper(LoadingMapper.class);

		Date date = new Date();
		BigDecimal decimal = new BigDecimal(1);

		LoadingDO loadingDO = new LoadingDO();
		loadingDO.setMerchantCode("1");
		loadingDO.setLoadingNo("1");
		loadingDO.setSenderStationCode("1");
		loadingDO.setTransCode("1");
		loadingDO.setCarCode("1");
		loadingDO.setDriverCode("1");
		loadingDO.setLineCode("1");
		loadingDO.setDispatcherCode("1");
		loadingDO.setPlanArrivalTime(date);
		loadingDO.setFactArrivalTime(date);
		loadingDO.setDeadlineTime(date);
		loadingDO.setStartoffTime(date);
		loadingDO.setArrivalTime(date);
		loadingDO.setTemperatureType("1");
		loadingDO.setGenerateMode("1");
		loadingDO.setShiftCode("1");
		loadingDO.setState("1");
		loadingDO.setStatus("1");
		loadingDO.setCloseTime(date);
		loadingDO.setGmtCreate(date);
		loadingDO.setGmtModified(date);
		loadingDO.setCreator("1");
		loadingDO.setModifier("1");
		loadingDO.setRemark("1");
		loadingDO.setPlanStartoffTime(date);
		loadingDO.setPalletNumber(1);
		loadingDO.setRollNumber(1);
		loadingDO.setPayloadRatio(decimal);
		loadingDO.setArrivalWsLongitude("1");
		loadingDO.setArrivalWsLatitude("1");
		loadingDO.setStartoffLongitude("1");
		loadingDO.setStartoffLatitude("1");
		loadingDO.setShiftPeriodTime(date);
		loadingDO.setPublishState("1");
		loadingDO.setAttendanceState("1");
		loadingDO.setBillingState("1");
		loadingDO.setCarModelCode("1");
		loadingDO.setLineName("1");
		loadingDO.setDriverPhone("1");
		loadingDO.setTransPlanType("1");
		loadingDO.setLoadingName("1");
		loadingDO.setEndStationCode("1");
		loadingDO.setStationNum(1);
		loadingDO.setProgressPercent(decimal);
		loadingDO.setItineraryVerifyInfo("1");
		loadingDO.setAuditState("1");
		loadingDO.setQuoteType("1");
		loadingDO.setTemporaryQuoteAmt(decimal);
		loadingDO.setBizNatureCode("1");
		loadingDO.setContractNo("1");
		loadingDO.setContractVersion("1");
		loadingDO.setReceiveState("1");
		loadingDO.setReceiveTime(date);
		loadingDO.setTransName("1");
		loadingDO.setTransShortName("1");
		loadingDO.setFactCarModelCode("1");
		loadingDO.setFactCarModelName("1");
		loadingDO.setPlateNumber("1");
		loadingDO.setDriverName("1");
		loadingDO.setStandardSealNum(1);
		loadingDO.setLoadingState(1);
		loadingDO.setStateRemark("1");
		loadingDO.setVersion(1);
		loadingDO.setPlanCarModelName("1");
		loadingDO.setPlanTemperatureType("1");
		loadingDO.setPlanLength(decimal);
		loadingDO.setPlanWidth(decimal);
		loadingDO.setPlanHeight(decimal);
		loadingDO.setFactTemperatureType("1");
		loadingDO.setFactLength(decimal);
		loadingDO.setFactWidth(decimal);
		loadingDO.setFactHeight(decimal);
		loadingDO.setExternalOrderCode("1");
		loadingDO.setExternalSys("1");
		loadingDO.setTransportType("1");
		loadingDO.setDynamicFields("1");
		loadingDO.setLoadingEndTime(date);
		loadingDO.setCreateFrom("1");
		loadingDO.setDeliveryState("1");
		loadingDO.setPlanPickNumber(1L);
		loadingDO.setActualPickNumber(1L);
		loadingDO.setExpectDischargeNumber(1L);
		loadingDO.setPlanDischargeNumber(1L);
		loadingDO.setActualDischargeNumber(1L);
		loadingDO.setOrderNum(1);

		int count = mapper.insert(loadingDO);
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void testCount() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		LoadingMapper mapper = sqlSession.getMapper(LoadingMapper.class);

		Map<String, Object> map = new HashMap<>();
		map.put("merchantCode", "1");
		map.put("sendStationCode", "1");
		map.put("transCode", "1");
		map.put("loadingNo", "1");
		map.put("state", "1");
		map.put("driverCode", "1");
		map.put("carCode", "1");
		long count = mapper.count(map);
		System.out.println(count);
	}

	@Test
	public void testSelect() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		LoadingMapper mapper = sqlSession.getMapper(LoadingMapper.class);

		Map<String, Object> map = new HashMap<>();
		map.put("merchantCode", "1");
		map.put("sendStationCode", "1");
		map.put("transCode", "1");
		map.put("loadingNo", "1");
		map.put("state", "1");
		map.put("driverCode", "1");
		map.put("carCode", "1");
		List<LoadingDO> dos = mapper.select(map);
		System.out.println(dos);
	}

	@Test
	public void updateByLoadingNoAndVersion() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		LoadingMapper mapper = sqlSession.getMapper(LoadingMapper.class);

		Date date = new Date();
		BigDecimal decimal = new BigDecimal(2);

		LoadingDO loadingDO = new LoadingDO();
		loadingDO.setMerchantCode("1");
		loadingDO.setLoadingNo("1");
		loadingDO.setVersion(1);
		loadingDO.setSenderStationCode("2");
		loadingDO.setTransCode("2");
		loadingDO.setCarCode("2");
		loadingDO.setDriverCode("2");
		loadingDO.setLineCode("2");
		loadingDO.setDispatcherCode("2");
		loadingDO.setPlanArrivalTime(date);
		loadingDO.setFactArrivalTime(date);
		loadingDO.setDeadlineTime(date);
		loadingDO.setStartoffTime(date);
		loadingDO.setArrivalTime(date);
		loadingDO.setTemperatureType("2");
		loadingDO.setGenerateMode("2");
		loadingDO.setShiftCode("2");
		loadingDO.setState("2");
		loadingDO.setStatus("2");
		loadingDO.setCloseTime(date);
		loadingDO.setGmtCreate(date);
		loadingDO.setGmtModified(date);
		loadingDO.setCreator("2");
		loadingDO.setModifier("2");
		loadingDO.setRemark("2");
		loadingDO.setPlanStartoffTime(date);
		loadingDO.setPalletNumber(2);
		loadingDO.setRollNumber(2);
		loadingDO.setPayloadRatio(decimal);
		loadingDO.setArrivalWsLongitude("2");
		loadingDO.setArrivalWsLatitude("2");
		loadingDO.setStartoffLongitude("2");
		loadingDO.setStartoffLatitude("2");
		loadingDO.setShiftPeriodTime(date);
		loadingDO.setPublishState("2");
		loadingDO.setAttendanceState("2");
		loadingDO.setBillingState("2");
		loadingDO.setCarModelCode("2");
		loadingDO.setLineName("2");
		loadingDO.setDriverPhone("2");
		loadingDO.setTransPlanType("2");
		loadingDO.setLoadingName("2");
		loadingDO.setEndStationCode("2");
		loadingDO.setStationNum(2);
		loadingDO.setProgressPercent(decimal);
		loadingDO.setItineraryVerifyInfo("2");
		loadingDO.setAuditState("2");
		loadingDO.setQuoteType("2");
		loadingDO.setTemporaryQuoteAmt(decimal);
		loadingDO.setBizNatureCode("2");
		loadingDO.setContractNo("2");
		loadingDO.setContractVersion("2");
		loadingDO.setReceiveState("2");
		loadingDO.setReceiveTime(date);
		loadingDO.setTransName("2");
		loadingDO.setTransShortName("2");
		loadingDO.setFactCarModelCode("2");
		loadingDO.setFactCarModelName("2");
		loadingDO.setPlateNumber("2");
		loadingDO.setDriverName("2");
		loadingDO.setStandardSealNum(2);
		loadingDO.setLoadingState(2);
		loadingDO.setStateRemark("2");
		loadingDO.setPlanCarModelName("2");
		loadingDO.setPlanTemperatureType("2");
		loadingDO.setPlanLength(decimal);
		loadingDO.setPlanWidth(decimal);
		loadingDO.setPlanHeight(decimal);
		loadingDO.setFactTemperatureType("2");
		loadingDO.setFactLength(decimal);
		loadingDO.setFactWidth(decimal);
		loadingDO.setFactHeight(decimal);
		loadingDO.setExternalOrderCode("2");
		loadingDO.setExternalSys("2");
		loadingDO.setTransportType("2");
		loadingDO.setDynamicFields("2");
		loadingDO.setLoadingEndTime(date);
		loadingDO.setCreateFrom("2");
		loadingDO.setDeliveryState("2");
		loadingDO.setPlanPickNumber(2L);
		loadingDO.setActualPickNumber(2L);
		loadingDO.setExpectDischargeNumber(2L);
		loadingDO.setPlanDischargeNumber(2L);
		loadingDO.setActualDischargeNumber(2L);
		loadingDO.setOrderNum(2);

		long count = mapper.updateByLoadingNoAndVersion(loadingDO);
		sqlSession.commit();
		System.out.println(count);
	}













	@Test
	public void tBatchInsert() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		List<TransportStationDO> doList = new ArrayList<>();
		for (int i = 1; i<=10;i++) {
			TransportStationDO stationDO = new TransportStationDO();
			stationDO.setGmtCreate(new Date());
			stationDO.setGmtModified(new Date());
			stationDO.setWhCode(i+"");
			stationDO.setLoadingNo(i+"");
			stationDO.setSeq(i);
			stationDO.setStartStationCode(i+"");
			stationDO.setEndStationCode(i+"");
			stationDO.setStartStationName(i+"");
			stationDO.setEndStationName(i+"");
			stationDO.setLongitude(i+"");
			stationDO.setLatitude(i+"");
			stationDO.setStartoffLongitude(i+"");
			stationDO.setStartoffLatitude(i+"");
			stationDO.setArrivalTime(new Date());
			stationDO.setStartoffTime(new Date());
			stationDO.setPlanArrivalTime(new Date());
			stationDO.setPlanStartoffTime(new Date());
			stationDO.setAppArrivalTime(new Date());
			stationDO.setAppStartoffTime(new Date());
			stationDO.setManualArrivalTime(new Date());
			stationDO.setManualStartoffTime(new Date());
			stationDO.setStickDuration(i);
			stationDO.setArrivalFence("1");
			stationDO.setStartoffArrivalFence("1");
			stationDO.setStandardServiceTime(i);
			stationDO.setState("1");
			stationDO.setStatus("1");
			stationDO.setCreator(i+"");
			stationDO.setModifier(i+"");
			stationDO.setRemark(i+"");
			stationDO.setVerificationState(i+"");
			stationDO.setHandOverType(i+"");
			stationDO.setPlanPickNumber(Long.valueOf(i+""));
			stationDO.setActualPickNumber(Long.valueOf(i+""));
			stationDO.setExpectDischargeNumber(Long.valueOf(i+""));
			stationDO.setPlanDischargeNumber(Long.valueOf(i+""));
			stationDO.setActualDischargeNumber(Long.valueOf(i+""));
			stationDO.setConnectionTime(i);

			doList.add(stationDO);
		}
		int count = mapper.batchInsert(doList);
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void updateConnectionTime() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		int count = mapper.updateConnectionTime("1", "1", "1", 100);
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void updateState() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		int count = mapper.updateState("1", "1", "1", "a");
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void updateVerificationState() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		int count = mapper.updateVerificationState("1", "1", "1", "a");
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void updateByLoadingNoAndStationCode() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		int i = 0;
		TransportStationDO stationDO = new TransportStationDO();
		stationDO.setGmtCreate(new Date());
		stationDO.setGmtModified(new Date());
		stationDO.setWhCode("2");
		stationDO.setLoadingNo("2");
		stationDO.setSeq(i);
		stationDO.setStartStationCode(i+"");
		stationDO.setEndStationCode("2");
		stationDO.setStartStationName(i+"");
		stationDO.setEndStationName(i+"");
		stationDO.setLongitude(i+"");
		stationDO.setLatitude(i+"");
		stationDO.setStartoffLongitude(i+"");
		stationDO.setStartoffLatitude(i+"");
		stationDO.setArrivalTime(new Date());
		stationDO.setStartoffTime(new Date());
		stationDO.setPlanArrivalTime(new Date());
		stationDO.setPlanStartoffTime(new Date());
		stationDO.setAppArrivalTime(new Date());
		stationDO.setAppStartoffTime(new Date());
		stationDO.setManualArrivalTime(new Date());
		stationDO.setManualStartoffTime(new Date());
		stationDO.setStickDuration(i);
		stationDO.setArrivalFence("0");
		stationDO.setStartoffArrivalFence("0");
		stationDO.setStandardServiceTime(i);
		stationDO.setState("0");
		stationDO.setStatus("0");
		stationDO.setCreator(i+"");
		stationDO.setModifier(i+"");
		stationDO.setRemark(i+"");
		stationDO.setVerificationState(i+"");
		stationDO.setHandOverType(i+"");
		stationDO.setPlanPickNumber(Long.valueOf(i+""));
		stationDO.setActualPickNumber(Long.valueOf(i+""));
		stationDO.setExpectDischargeNumber(Long.valueOf(i+""));
		stationDO.setPlanDischargeNumber(Long.valueOf(i+""));
		stationDO.setActualDischargeNumber(Long.valueOf(i+""));
		stationDO.setConnectionTime(i);
		int count = mapper.updateByLoadingNoAndStationCode(stationDO);
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void getListByLoadingNo() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		List count = mapper.getListByLoadingNo("1", "1");
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void getTransportStation() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		TransportStationMapper mapper = sqlSession.getMapper(TransportStationMapper.class);

		TransportStationDO count = mapper.getTransportStation("1", "1", "1");
		sqlSession.commit();
		System.out.println(count);
	}







	@Test
	public void testBatchInsert() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		HandoverPlanMapper mapper = sqlSession.getMapper(HandoverPlanMapper.class);



		List<HandoverPlanDO> doList = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			mapper.deleteByBizOrderCode(i + "", i + "", i + "");

			HandoverPlanDO planDO = new HandoverPlanDO();
			planDO.setId(Long.valueOf(i + ""));
			planDO.setGmtCreate(new Date());
			planDO.setGmtModified(new Date());
			planDO.setWhCode(i + "");
			planDO.setStationCode(i + "");
			planDO.setBizOrderCode(i + "");
			planDO.setLoadingNo(i + "");
			planDO.setEntityId(i + "");
			planDO.setOutCode(i + "");
			planDO.setCode(i + "");
			planDO.setType(i + "");
			planDO.setName(i + "");
			planDO.setSpec(i + "");
			planDO.setImageUrl(i + "");
			planDO.setPlanPickNumber(Long.valueOf(i+""));
			planDO.setActualPickNumber(Long.valueOf(i+""));
			planDO.setExpectDischargeNumber(Long.valueOf(i+""));
			planDO.setPlanDischargeNumber(Long.valueOf(i+""));
			planDO.setActualDischargeNumber(Long.valueOf(i+""));
			planDO.setStatus("1");
			planDO.setOperationTime(new Date());
			planDO.setAttribute(i + "");

			doList.add(planDO);
		}

		Integer count = mapper.batchInsert(doList);
		sqlSession.commit();
		System.out.println(count);

	}

	@Test
	public void list() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		HandoverPlanMapper mapper = sqlSession.getMapper(HandoverPlanMapper.class);

		Map<String, Object> map = new HashMap<>();
		map.put("whCode", "1");
		map.put("stationCode", "1");
		List<String> stationCodes = new ArrayList<>();
		stationCodes.add("1");
		map.put("stationCodes", stationCodes);
		map.put("bizOrderCode", "1");
		map.put("loadingNo", "1");
		map.put("outCode", "1");
		map.put("code", "1");
		map.put("nameLike", "1");
		List<HandoverPlanDO> doList = mapper.list(map);
		System.out.println(doList);
	}

	@Test
	public void updateSignQuantityByCode() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		HandoverPlanMapper mapper = sqlSession.getMapper(HandoverPlanMapper.class);
		int count = mapper.updateSignQuantityByCode("1", "1", "1", 5L);
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void deleteByBizOrderCode() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		HandoverPlanMapper mapper = sqlSession.getMapper(HandoverPlanMapper.class);
		int count = mapper.deleteByBizOrderCode("10", "10", "10");
		sqlSession.commit();
		System.out.println(count);
	}

	@Test
	public void countPlanDischargeNumberGroupStation() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		HandoverPlanMapper mapper = sqlSession.getMapper(HandoverPlanMapper.class);

		List<String> stationCodes = new ArrayList<>();
		stationCodes.add("1");
		stationCodes.add("2");
		stationCodes.add("3");
		stationCodes.add("4");
		stationCodes.add("5");
		List<CountPlanDischargeNumberDO> numberDOS = mapper.countPlanDischargeNumberGroupStation("1", "1", stationCodes);
		System.out.println(numberDOS);
	}




	// 测试一级会话缓存，如果使用同一个SqlSession对象进行两个相同的查询操作，则第二会走缓存
	@Test
	public void testSessionCache1() {

		SqlSession sqlSession = sqlSessionFactory.openSession();
		IEmployeerMapper iEmployeerMapper = sqlSession.getMapper(IEmployeerMapper.class);
		List<Employeer> employeers = iEmployeerMapper.findAllEmployeer();

//		SqlSession sqlSession1 = sqlSessionFactory.openSession();
		sqlSession.clearCache();
		IEmployeerMapper iEmployeerMapper1 = sqlSession.getMapper(IEmployeerMapper.class);
		List<Employeer> employeers1 = iEmployeerMapper1.findAllEmployeer();

//		System.out.println(employeers);
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
		//Employeer employeer = session.selectOne("com.whz.mapperinterface.IEmployeerMapper.findEmployeerByID", 5);
		//Employeer employeer = session.getMapper(com.whz.mapperinterface.IEmployeerMapper.class).findEmployeerByID(5);
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
			int resultCount = session.insert("com.whz.mapperinterface.IEmployeerMapper.addEmployeer", employeer );
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
			session.update("com.whz.mapperinterface.IEmployeerMapper.updateEmployeer",employeer);
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
			int resultCount=session.delete("com.whz.mapperinterface.IEmployeerMapper.deleteEmployeer",id);
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
			int resultCount = session.insert("com.whz.mapperinterface.IEmployeerMapper.addEmployeer", employeer );
			System.out.printf("当前插入的employeer_id :%d    当前插入数据库中条数:%d " , employeer.getEmployeer_id() ,resultCount);  //获取插入对象的id
			session.commit() ;
		} finally {
			session.close();
		}

	}

}
