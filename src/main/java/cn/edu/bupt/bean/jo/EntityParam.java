package cn.edu.bupt.bean.jo;

import lombok.Data;

@Data
public class EntityParam {

    private long id;

    private String content;

    private boolean passed;

    private long statId;

}