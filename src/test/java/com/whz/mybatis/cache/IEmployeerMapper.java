package com.whz.mybatis.cache;

import com.whz.entity.Employeer;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface IEmployeerMapper {

    List<Employeer> findAllEmployeer();
    Employeer findEmployeerByID(@Param("department") int id);
    Employeer findEmployeerByIDWithoutCache(@Param("department") int id);

    void addEmployeer(Employeer employeer);
    void addEmployeerWithoutFlushCache(Employeer employeer);

    void deleteEmployeer(int id);
    void updateEmployeer(Employeer employeer);

}
