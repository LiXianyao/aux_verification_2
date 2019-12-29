package cn.edu.bupt.repository;

import cn.edu.bupt.bean.po.EntityReflect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntiReflectRepo extends JpaRepository<EntityReflect, Long> {

    @Override
    @Query("select e from EntityReflect e where e.parentId is null")
    public List<EntityReflect> findAll();

}
