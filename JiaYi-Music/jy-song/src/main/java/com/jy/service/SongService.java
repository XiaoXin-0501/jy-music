package com.jy.service;

import com.jy.bo.SongStateBo;
import com.jy.entity.SongEntity;
import com.jy.entity.SongListEntity;
import com.jy.vo.requestVo.SongInputBody;
import com.jy.vo.responseVo.SongListInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface SongService {
    Long uploadSong(SongInputBody songInputBody);

    boolean updateSong(SongInputBody songInputBody, Long songId);

    boolean deleteSong(Long songId);

    List<SongListInfo> getSongList();

    void addSongToList(Long toListId, Long songId);

    void removeSongFromList(Long listId, Long songId);

    Long createSongList(String name);

    List<SongEntity> getUserSongs();

    List<SongEntity> getRankSongs();

    void deleteSongList(Long listId);

    void updateSongList(SongListEntity songListEntity);

    File getSongFileById(Long songId);

    Long getUploadListId();

    void updatePlayCountCache(Long songId);

    boolean updateSongState(List<SongStateBo> songStateBos);

    Map<String, Double> getPlayRank();

    Map<String, Double> getCollectRank();

    Map<String, Double> getHotRank();
}
