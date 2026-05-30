package com.jy.service.songService;

import com.jy.bo.SongStateBo;
import com.jy.cache.RedisService;
import com.jy.cache.RedissonService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.RedisConstants;
import com.jy.constant.RedisKey;
import com.jy.entity.SongEntity;
import com.jy.entity.SongListEntity;
import com.jy.exception.SongException;
import com.jy.mapper.SongMapper;
import com.jy.securityUtils.SecurityUtils;
import com.jy.service.SongService;
import com.jy.service.commonService.IOService;
import com.jy.utils.IdUtils;
import com.jy.vo.requestVo.SongInputBody;
import com.jy.vo.responseVo.SongListInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SongServiceImpl implements SongService {
    private final SongMapper songMapper;
    private final RedisService redisService;
    private final RedissonService redissonService;
    private final IOService ioService;

    public SongServiceImpl(SongMapper songMapper,
                           RedisService redisService,
                           RedissonService redissonService,
                           IOService ioService
    ) {
        this.songMapper = songMapper;
        this.redisService = redisService;
        this.redissonService = redissonService;
        this.ioService = ioService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long uploadSong(SongInputBody songInputBody) {
        SongEntity songEntity = new SongEntity();
        songEntity.setBySongInputBody(songInputBody);
        Long songId = IdUtils.snowflake();
        Long listId = getUploadListId();
        Long userId = SecurityUtils.getUserId();
        songEntity.setId(songId);
        songEntity.setCreateTime(LocalDateTime.now());
        boolean bool1 = songMapper.insertSong(songEntity);
        boolean bool2 = songMapper.insertSongCategoryRelation(songId, songInputBody.getCategories());
        boolean bool3 = songMapper.insertSongListRelation(listId, songId);
        if (!bool1 || !bool2 || !bool3) {
            throw new SongException(ErrorMessage.SONG_UPLOAD_FAIL);
        }
        redisService.set(RedisKey.SONG.getKey(songId.toString()), songEntity);
        addSongToListCache(songId, listId);
        redisService.lPush(RedisKey.USER_SONGS.getKey(userId.toString()), songId);
        redissonService.songIdFilterAdd(songId.toString());
        return songId;
    }

    //需要优化，加锁，暂未处理
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateSong(SongInputBody songInputBody, Long songId) {
        boolean exist = redissonService.getSongIdFilter().exists(songId.toString());
        if (!exist) return false;
        SongEntity songEntity = redisService.get(RedisKey.SONG.getKey(songId.toString()), SongEntity.class);
        if (songEntity == null) {
            songEntity = songMapper.getSongById(songId);
            if (songEntity == null) {
                return false;
            }
        }
        songEntity.setBySongInputBody(songInputBody);
        songEntity.setId(songId);
        int res1 = songMapper.updateSong(songEntity, songId);
        int res2 = songMapper.deleteCategoriesById(songId);
        boolean res3 = songMapper.insertSongCategoryRelation(songId, songInputBody.getCategories());
        if (res1 <= 0 || res2 <= 0 || !res3) throw new SongException(ErrorMessage.SONG_UPLOAD_FAIL);
        redisService.set(RedisKey.SONG.getKey(songId.toString()), songEntity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteSong(Long songId) {
        boolean exist = redissonService.getSongIdFilter().exists(songId.toString());
        if (!exist) return false;
        boolean res1 = songMapper.deleteSongById(songId);
        boolean res2 = songMapper.deleteSong(songId);
        boolean res3 = songMapper.deleteCategoriesById(songId) > 0;
        if (!res1 || !res2 || !res3) throw new SongException(ErrorMessage.SONG_REMOVE_FAIL);
        Long userId = SecurityUtils.getUserId();
        redissonService.getSongIdFilter().remove(songId.toString());
        redisService.delete(
                RedisKey.SONG.getKey(songId.toString()),
                RedisKey.SONG_COLLECT_COUNT.getKey(songId.toString()),
                RedisKey.SONG_PLAY_COUNT.getKey(songId.toString())
        );
        redisService.sRem(RedisKey.SONG_PENDING.getKey(), songId);
        redisService.zRem(RedisKey.SONG_PLAY_TOP.getKey(), songId);
        redisService.zRem(RedisKey.SONG_COLLECT_TOP.getKey(), songId);
        redisService.zRem(RedisKey.SONG_HOT_TOP.getKey(), songId);
        redisService.lRemove(RedisKey.USER_SONGS.getKey(userId.toString()), songId, 0);
        return true;
    }

    @Override
    public List<SongListInfo> getSongList() {
        Long userId = SecurityUtils.getUserId();
        List<SongListInfo> songListInfos = new ArrayList<>();

        List<Long> listIds = redisService.lRangeAll(RedisKey.USER_SONG_LIST.getKey(userId.toString()), Long.class);
        if (listIds.isEmpty()) {
            songListInfos = songMapper.getSongListByUserId(userId);
            listIds = songListInfos.stream()
                    .map(SongListInfo::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (listIds.isEmpty()) return songListInfos;
            redisService.lPushAll(RedisKey.USER_SONG_LIST.getKey(userId.toString()), listIds);
        }
        Set<Long> set = new HashSet<>(listIds);
        Iterator<Long> iterator = set.iterator();
        boolean needUpadte = false;
        while (iterator.hasNext()) {
            Long listId = iterator.next();
            SongListInfo songListInfo = redisService.get(RedisKey.LIST.getKey(listId.toString()), SongListInfo.class);
            if (songListInfo == null) {
                songListInfo = songMapper.getSongListByListId(listId);
                if (songListInfo == null) {
                    needUpadte = true;
                    listIds.remove(listId);
                } else {
                    songListInfos.add(songListInfo);
                    redisService.set(RedisKey.LIST.getKey(listId.toString()), songListInfo);
                }
            } else {
                List<Long> songIdsByList = songListInfo.getSongs() == null ? new ArrayList<>() : songListInfo.getSongs();
                if (songIdsByList.isEmpty()) {
                    songIdsByList = songMapper.getSongListByListId(listId).getSongs();
                    if (songIdsByList.isEmpty()) {
                        songListInfos.add(songListInfo);
                        continue;
                    }
                }
                // 获取用户所有的歌曲ID
                List<Long> songIdsByUser = redisService.lRangeAll(RedisKey.USER_SONGS.getKey(userId.toString()), Long.class);
                Set<Long> userSongSet = new HashSet<>(songIdsByUser);

                // 找出在歌单中但不在用户歌曲列表中的ID（需要移除的）
                List<Long> toRemove = songIdsByList.stream()
                        .filter(songId -> !userSongSet.contains(songId))
                        .toList();

                if (!toRemove.isEmpty()) {
                    // 创建一个新的列表，只保留有效的歌曲ID，避免直接修改原引用可能带来的副作用，并确保内容确实发生了变化
                    List<Long> updatedSongs = songIdsByList.stream()
                            .filter(userSongSet::contains)
                            .collect(Collectors.toList());

                    // 只有当过滤后的列表与原列表不一致时才更新缓存
                    // 这里通过比较大小或内容来确认是否真的“更改”了
                    if (updatedSongs.size() != songIdsByList.size()) {
                        songListInfo.setSongs(updatedSongs);
                        redisService.set(RedisKey.LIST.getKey(listId.toString()), songListInfo);
                    }
                }
                songListInfos.add(songListInfo);
            }
        }
        if (needUpadte) {
            redisService.delete(RedisKey.USER_SONG_LIST.getKey(userId.toString()));
            redisService.lPushAll(RedisKey.USER_SONG_LIST.getKey(userId.toString()), listIds);
        }
        return songListInfos;
    }

    @Override
    public void addSongToList(Long toListId, Long songId) {
        boolean exist = redissonService.getSongIdFilter().exists(songId.toString());
        if (!exist) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
        boolean res = songMapper.insertSongToList(toListId, songId);
        if (!res) throw new SongException(ErrorMessage.SONG_ADD_TO_LIST_FAIL);
        Long userId = SecurityUtils.getUserId();
        addSongToListCache(songId, toListId);
        redisService.lPush(RedisKey.USER_SONGS.getKey(userId.toString()), songId);
    }

    @Override
    public void removeSongFromList(Long listId, Long songId) {
        boolean exist = redissonService.getSongIdFilter().exists(songId.toString());
        if (!exist) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
        boolean res = songMapper.deleteSongFromList(listId, songId);
        if (!res) throw new SongException(ErrorMessage.SONG_REMOVE_FROM_LIST_FAIL);
        delSongFromListCache(songId, listId);
        Long userId = SecurityUtils.getUserId();
        redisService.lRemove(RedisKey.USER_SONGS.getKey(userId.toString()), songId);
    }

    @Override
    public Long createSongList(String name) {
        SongListEntity songListEntity = new SongListEntity();
        Long id = IdUtils.snowflake();
        Long userId = SecurityUtils.getUserId();
        songListEntity.setId(id);
        songListEntity.setName(name);
        songListEntity.setCreateTime(LocalDateTime.now());
        boolean res1 = songMapper.insertSongList(songListEntity);
        boolean res2 = songMapper.insertSongUserRelation(userId, id);
        if (!res1 || !res2) throw new SongException(ErrorMessage.SONG_LIST_CREATE_FAIL);
        SongListInfo songListInfo = new SongListInfo();
        songListInfo.setSongListEntity(songListEntity);
        songListInfo.setSongs(new ArrayList<>());
        redisService.set(RedisKey.LIST.getKey(id.toString()), songListInfo);
        redisService.lPush(RedisKey.USER_SONG_LIST.getKey(userId.toString()), id);
        return id;
    }

    @Override
    public List<SongEntity> getUserSongs() {
        List<SongEntity> songs = new ArrayList<>();
        Long userId = SecurityUtils.getUserId();
        List<Long> songIds = redisService.lRangeAll(RedisKey.USER_SONGS.getKey(userId.toString()), Long.class);
        if (songIds.isEmpty()) {
            songIds = songMapper.getSongIdsByUserId(userId);
            if (songIds.isEmpty()) return songs;
            redisService.lPushAll(RedisKey.USER_SONGS.getKey(userId.toString()), songIds);
        }
        Set<Long> set = new HashSet<>(songIds);
        for (Long songId : set) {
            SongEntity songEntity = redisService.get(RedisKey.SONG.getKey(songId.toString()), SongEntity.class);
            if (songEntity == null) {
                songEntity = songMapper.getSongById(songId);
                if (songEntity == null) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
                redisService.set(RedisKey.SONG.getKey(songId.toString()), songEntity);
            }
            Integer playCount = redisService.get(RedisKey.SONG_PLAY_COUNT.getKey(songId.toString()), Integer.class);
            Integer collectCount = redisService.get(RedisKey.SONG_COLLECT_COUNT.getKey(songId.toString()), Integer.class);
            if (playCount != null) songEntity.setPlayCount(playCount);
            if (collectCount != null) songEntity.setCollectCount(collectCount);
            songs.add(songEntity);
        }
        return songs;
    }

    @Override
    public List<SongEntity> getRankSongs() {
        List<SongEntity> songs = new ArrayList<>();

        Set<String> songIds = new HashSet<>();
        Map<String, Double> plankRank = getPlayRank();
        Map<String, Double> collectRank = getCollectRank();
        Map<String, Double> hotRank = getHotRank();

        songIds.addAll(plankRank.keySet());
        songIds.addAll(collectRank.keySet());
        songIds.addAll(hotRank.keySet());

        if (songIds.isEmpty()) return songs;

        for (String songId : songIds) {
            SongEntity songEntity = redisService.get(RedisKey.SONG.getKey(songId), SongEntity.class);
            if (songEntity == null) {
                songEntity = songMapper.getSongById(Long.parseLong(songId));
                if (songEntity == null) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
                redisService.set(RedisKey.SONG.getKey(songId), songEntity);
            }
            Integer playCount = redisService.get(RedisKey.SONG_PLAY_COUNT.getKey(songId), Integer.class);
            Integer collectCount = redisService.get(RedisKey.SONG_COLLECT_COUNT.getKey(songId), Integer.class);
            if (playCount != null) songEntity.setPlayCount(playCount);
            if (collectCount != null) songEntity.setCollectCount(collectCount);
            songs.add(songEntity);
        }
        return songs;
    }

    @Override
    public void deleteSongList(Long listId) {
        Long userId = SecurityUtils.getUserId();
        boolean res1 = songMapper.deleteSongListById(listId);
        boolean res2 = songMapper.deleteSongListUserRelation(listId, userId);
        if (!res1 || !res2) throw new SongException(ErrorMessage.SONG_LIST_DELETE_FAIL);
        redisService.delete(RedisKey.LIST.getKey(listId.toString()));
        redisService.lRemove(RedisKey.USER_SONG_LIST.getKey(userId.toString()), listId);
    }

    @Override
    public void updateSongList(SongListEntity songListEntity) {
        songListEntity.setUpdateTime(LocalDateTime.now());
        boolean res = songMapper.updateSongList(songListEntity);
        if (!res) throw new SongException(ErrorMessage.SONG_LIST_UPDATE_FAIL);
        SongListInfo songListInfo = redisService.get(RedisKey.LIST.getKey(songListEntity.getId().toString()), SongListInfo.class);
        songListInfo.setSongListEntity(songListEntity);
        redisService.set(RedisKey.LIST.getKey(songListEntity.getId().toString()), songListInfo);
    }

    @Override
    public File getSongFileById(Long songId) {
        boolean exist = redissonService.getSongIdFilter().exists(songId.toString());
        if (!exist) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
        SongEntity songEntity = redisService.get(RedisKey.SONG.getKey(songId.toString()), SongEntity.class);
        if (songEntity == null) {
            songEntity = songMapper.getSongById(songId);
            if (songEntity == null) throw new SongException(ErrorMessage.SONG_NOT_EXIST);
            redisService.set(RedisKey.SONG.getKey(songId.toString()), songEntity);
        }
        return new File(ioService.getLocalPath(songEntity.getSongUrl()));
    }

    public boolean addSongToListCache(Long songId, Long listId) {
        SongListInfo songListInfo = redisService.get(RedisKey.LIST.getKey(listId.toString()), SongListInfo.class);
        if (songListInfo == null) return false;
        List<Long> songs = songListInfo.getSongs();
        songs.add(songId);
        songListInfo.setSongs(songs);
        redisService.set(RedisKey.LIST.getKey(listId.toString()), songListInfo);
        if (listId.equals(getFavoriteListId())) {
            redisService.incr(RedisKey.SONG_COLLECT_COUNT.getKey(songId.toString()));
            redisService.zIncrby(RedisKey.SONG_COLLECT_TOP.getKey(), songId.toString(), 1);
            redisService.zIncrby(RedisKey.SONG_HOT_TOP.getKey(), songId.toString(), RedisConstants.COLLECT_WEIGHT);
            redisService.sAdd(RedisKey.SONG_PENDING.getKey(), songId);
        }
        return true;
    }

    public boolean delSongFromListCache(Long songId, Long listId) {
        SongListInfo songListInfo = redisService.get(RedisKey.LIST.getKey(listId.toString()), SongListInfo.class);
        if (songListInfo == null) return false;
        List<Long> songs = songListInfo.getSongs();
        if (songs == null || !songs.contains(songId)) return false;
        songs.remove(songId);
        songListInfo.setSongs(songs);
        redisService.set(RedisKey.LIST.getKey(listId.toString()), songListInfo);
        if (listId.equals(getFavoriteListId())) {
            redisService.decr(RedisKey.SONG_COLLECT_COUNT.getKey(songId.toString()));
            redisService.zIncrby(RedisKey.SONG_COLLECT_TOP.getKey(), songId.toString(), -1);
            redisService.zIncrby(RedisKey.SONG_HOT_TOP.getKey(), songId.toString(), -RedisConstants.COLLECT_WEIGHT);
            redisService.sAdd(RedisKey.SONG_PENDING.getKey(), songId);
        }
        return true;
    }

    @Override
    public boolean updateSongState(List<SongStateBo> songStateBos) {
        boolean res = songMapper.updateSongState(songStateBos);
        if (!res) throw new SongException(ErrorMessage.SONG_UPLOAD_FAIL);
        return true;
    }

    @Override
    public Map<String, Double> getPlayRank() {
        return redisService.zRange(RedisKey.SONG_PLAY_TOP.getKey(), 0, 14);
    }

    @Override
    public Map<String, Double> getCollectRank() {
        return redisService.zRange(RedisKey.SONG_COLLECT_TOP.getKey(), 0, 14);
    }

    @Override
    public Map<String, Double> getHotRank() {
        return redisService.zRange(RedisKey.SONG_HOT_TOP.getKey(), 0, 14);
    }

    @Override
    public void updatePlayCountCache(Long songId) {
        redisService.incr(RedisKey.SONG_PLAY_COUNT.getKey(songId.toString()));
        redisService.zIncrby(RedisKey.SONG_PLAY_TOP.getKey(), songId.toString(), 1);
        redisService.zIncrby(RedisKey.SONG_HOT_TOP.getKey(), songId.toString(), RedisConstants.PLAY_WEIGHT);
        redisService.sAdd(RedisKey.SONG_PENDING.getKey(), songId);
    }


    public Long getUploadListId() {
        Long userId = SecurityUtils.getUserId();
        return userId * 100 + 1;
    }

    public Long getFavoriteListId() {
        Long userId = SecurityUtils.getUserId();
        return userId * 100;
    }
}
