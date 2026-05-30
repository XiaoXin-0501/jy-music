package com.jy.vo.responseVo;

import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CategoryInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 分类id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 分类名称
     */
    private String name;
}
