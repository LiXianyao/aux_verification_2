package cn.edu.bupt.bean.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.util.List;

@Entity
@Table(name = "entity_name_no")
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
@Data
public class EntityReflect {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_tag")
    private String entityTag;

    @Column(name = "color")
    private String color;

    @Column(name = "parent_id", nullable = true)
    private Integer parentId;

    // 只在查询时级联
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "parentId")
    private List<EntityReflect> children;
}
