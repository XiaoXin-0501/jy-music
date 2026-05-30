package com.jy.mapper;

import com.jy.bo.SongStateBo;
import com.jy.entity.SongEntity;
import com.jy.entity.SongListEntity;
import com.jy.vo.responseVo.SongListInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SongMapper {
    boolean insertSong(SongEntity songEntity);

    boolean insertSongCategoryRelation(@Param("songId") Long songId, @Param("categories") List<Long> categories);

    boolean insertSongListRelation(@Param("listId") Long listId, @Param("songId") Long songId);

    int updateSong(@Param("songEntity") SongEntity songEntity, @Param("songId") Long songId);

    int deleteCategoriesById(@Param("songId") Long songId);

    SongEntity getSongById(@Param("songId") Long songId);

    boolean deleteSongById(@Param("songId") Long songId);

    boolean deleteSong(@Param("songId") Long songId);

    List<SongListInfo> getSongListByUserId(Long userId);

    SongListInfo getSongListByListId(Long listId);

    boolean insertSongToList(@Param("listId") Long listId, @Param("songId") Long songId);

    boolean deleteSongFromList(@Param("listId") Long listId, @Param("songId") Long songId);

    boolean insertSongList(SongListEntity songListEntity);

    boolean insertSongUserRelation(@Param("userId") Long userId, @Param("listId") Long listId);

    boolean deleteSongListById(@Param("listId") Long listId);

    boolean deleteSongListUserRelation(@Param("listId") Long listId, @Param("userId") Long userId);

    List<Long> getSongIdsByUserId(Long userId);

    boolean updateSongList(SongListEntity songListEntity);

    boolean updateSongState(List<SongStateBo> songStateBos);
}
