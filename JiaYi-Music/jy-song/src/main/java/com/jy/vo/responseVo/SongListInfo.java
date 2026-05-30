package com.jy.vo.responseVo;

import com.jy.entity.SongListEntity;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SongListInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 列表id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 列表名称
     */
    private String name;
    /**
     * 列表封面
     */
    private String coverImg;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 歌曲
     */
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> songs;

    public void setSongListEntity(SongListEntity songListEntity) {
        this.id = songListEntity.getId();
        this.name = songListEntity.getName();
        this.coverImg = songListEntity.getCoverImg();
        this.createTime = songListEntity.getCreateTime();
        this.updateTime = songListEntity.getUpdateTime();
    }
}
