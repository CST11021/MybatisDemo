package com.whz.mapperinterface;

import com.whz.entity.Employeer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


public interface IEmployeerMapper {


    List<Employeer> findAllEmployeer();

    // 根据id查找Employeer
    Employeer findEmployeerByID(@Param("department") int id);
    // 根据部分和工作类型查找
    List<Employeer> findEmployeerByDepartmentAndWorktype(@Param("department") String department, @Param("worktype")String worktype);

    List<Map> findEmployeerByCondition(Map condition);

    void addEmployeer(Employeer employeer);
    void deleteEmployeer(int id);
    void updateEmployeer(Employeer employeer);

}
