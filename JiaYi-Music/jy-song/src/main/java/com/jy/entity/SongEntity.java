package com.jy.entity;

import com.jy.vo.requestVo.SongInputBody;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SongEntity {
    /**
     * 歌曲id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 发布账号
     */
    private String account;
    /**
     * 歌手
     */
    private String singer;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 封面
     */
    private String coverImg;
    /**
     * 歌名
     */
    private String name;
    /**
     * 歌词
     */
    private String lyrics;
    /**
     * 歌曲
     */
    private String songUrl;
    /**
     * 做词人
     */
    private String lyricist;
    /**
     * 作曲人
     */
    private String composer;
    /**
     * 播放时长
     */
    private String duration;
    /**
     * 分类
     */
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> categories;
    /**
     * 播放次数
     */
    private Integer playCount;
    /**
     * 收藏数
     */
    private Integer collectCount;
    /**
     * 排序权重
     */
    private Integer sort;
    /**
     * 状态（0：不可播放，1：正常）
     */
    private Integer status;

    public void setBySongInputBody(SongInputBody songInputBody) {
        this.account = songInputBody.getAccount();
        this.singer = songInputBody.getSinger();
        this.coverImg = songInputBody.getCoverImg();
        this.name = songInputBody.getName();
        this.lyrics = songInputBody.getLyrics();
        this.songUrl = songInputBody.getSongUrl();
        this.lyricist = songInputBody.getLyricist();
        this.composer = songInputBody.getComposer();
        this.duration = songInputBody.getDuration();
        this.categories = songInputBody.getCategories();
        this.playCount = 0;
        this.collectCount = 0;
        this.sort = 0;
        this.status = 1;
    }
}
