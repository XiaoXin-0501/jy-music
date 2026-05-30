package com.jy.vo.requestVo;

import lombok.Data;

import java.util.List;

@Data
public class SongInputBody {
    /**
     * 发布账号
     */
    private String account;
    /**
     * 歌手
     */
    private String singer;
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
    private List<Long> categories;
}
