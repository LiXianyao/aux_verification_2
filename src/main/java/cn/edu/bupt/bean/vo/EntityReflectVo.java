package cn.edu.bupt.bean.vo;

import cn.edu.bupt.bean.po.EntityReflect;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class EntityReflectVo {

    private List<EntityVo> entityList = new ArrayList<>();

    @Data
    public static class EntityVo {
        private int id;
        private String name;
        private String value;
        private String color;
        private List<EntityVo> children;
    }

    public List<EntityVo> getEntityVos(List<EntityReflect> entityReflects){
        List<EntityVo> lists = new ArrayList<>();
        for (EntityReflect entityReflect: entityReflects){
            EntityVo vo = new EntityVo();
            vo.setId(entityReflect.getId());
            vo.setName(entityReflect.getEntityName());
            vo.setValue(entityReflect.getEntityTag());
            vo.setColor(entityReflect.getColor());
            vo.setChildren(getEntityVos(entityReflect.getChildren()));
            lists.add(vo);
        }

        return lists;
    }

    public void addEntities(List<EntityReflect> originEntities){
        entityList.addAll(getEntityVos(originEntities));
//        for (EntityReflect originEntitie: originEntities) {
//            EntityVo vo = new EntityVo();
//            vo.setId(originEntitie.getId());
//            vo.setName(originEntitie.getEntityName());
//            vo.setValue(originEntitie.getEntityTag());
//            vo.setColor(originEntitie.getColor());
//            entityList.add(vo);
//        }
    }

}
