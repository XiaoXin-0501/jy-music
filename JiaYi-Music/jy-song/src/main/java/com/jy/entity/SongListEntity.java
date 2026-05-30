package com.jy.entity;

import com.jy.vo.responseVo.SongListInfo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SongListEntity {
    /**
     * 列表id
     */
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

    public void setSongListInfo(SongListInfo songListInfo) {
        this.id = songListInfo.getId();
        this.name = songListInfo.getName();
        this.coverImg = songListInfo.getCoverImg();
        this.createTime = songListInfo.getCreateTime();
        this.updateTime = songListInfo.getUpdateTime();
    }
}
