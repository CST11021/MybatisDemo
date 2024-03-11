package com.whz.mybatis.dao;

import com.whz.mybatis.entity.Employeer;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IEmployeerMapper {


    List<Employeer> findAllEmployeer();
    List<Employeer> findAllEmployeerByPage(@Param("index") int index, @Param("pageSize") int pageSize, @Param("orderByField") String orderByField);
    // 根据id查找Employeer
    Employeer findEmployeerByID(@Param("department") int id);
    // 根据部分和工作类型查找
    List<Employeer> findEmployeerByDepartmentAndWorktype(@Param("department") String department, @Param("worktype")String worktype);

    List<Map> findEmployeerByCondition1(Map condition);
    List<Employeer> findEmployeerByCondition2(Map condition);
    List<Employeer> findEmployeerByCondition3(Employeer condition);
    List<Employeer> findEmployeerByCondition4(Employeer condition, @Param("index") int index, @Param("pageSize") int pageSize);

    int addEmployeer(Employeer employeer);
    void deleteEmployeer(int id);
    void updateEmployeer(Employeer employeer);

}
