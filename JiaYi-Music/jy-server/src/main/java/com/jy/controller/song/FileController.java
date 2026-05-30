package com.jy.controller.song;

import com.jy.domain.ApiResult;
import com.jy.securityUtils.SecurityUtils;
import com.jy.service.commonService.IOService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("song/file")
public class FileController {
    private final String songUrl;
    private final String lyrics;
    private final String coverImg;
    private final IOService ioService;
    private final Integer maxUpload;


    public FileController(IOService ioService,
                          @Value("${file.song.path.song-url}") String songUrl,
                          @Value("${file.user.max-upload}") Integer maxUpload,
                          @Value("${file.song.path.lyrics}") String lyrics,
                          @Value("${file.song.path.cover-img}") String coverImg
    ) {
        this.ioService = ioService;
        this.songUrl = songUrl;
        this.coverImg = coverImg;
        this.lyrics = lyrics;
        this.maxUpload = maxUpload;
    }

    @GetMapping("/getChunks")
    public ApiResult<List<Integer>> getChunks(@RequestParam("fileHash") String fileHash) {
        List<Integer> chunks = ioService.getChunks(SecurityUtils.getUserId(), fileHash);
        return ApiResult.success(chunks);
    }

    //写一个isUpload查询范围更大，分开后能缩小查询范围
    @GetMapping("/isUpload")
    public ApiResult<Map<String, Object>> isUpload(@RequestParam("filename") String filename) {
        String url = ioService.fileExist(songUrl, filename);
        Map<String, Object> map = new HashMap<>();
        map.put("isUpload", true);
        map.put("url", url);
        if (url == null) {
            map.put("isUpload", false);
            map.put("url", null);
        }
        return ApiResult.success(map);
    }

    @GetMapping("/lyricsIsUpload")
    public ApiResult<Map<String, Object>> lyricsIsUpload(@RequestParam("filename") String filename) {
        String url = ioService.fileExist(lyrics, filename);
        Map<String, Object> map = new HashMap<>();
        map.put("isUpload", true);
        map.put("url", url);
        if (url == null) {
            map.put("isUpload", false);
            map.put("url", null);
        }
        return ApiResult.success(map);
    }

    @PostMapping("/uploadChunk")
    public ApiResult<Object> uploadChunk(@RequestParam("chunk") MultipartFile file) {
        ioService.clearHashTemp(SecurityUtils.getUserId(), maxUpload);
        ioService.uploadToTemp(file, SecurityUtils.getUserId());
        return ApiResult.success();
    }

    @PostMapping("/mergeChunks")
    public ApiResult<Object> mergeChunks(@RequestParam("filename") String filename) {
        String localPath = ioService.merge(filename, SecurityUtils.getUserId(), songUrl);
        String song = ioService.getUrl(ioService.getRelPath(localPath));
        return ApiResult.success(song);
    }

    @PostMapping("/mergeLyricsChunks")
    public ApiResult<Object> mergeLyricsChunks(@RequestParam("filename") String filename) {
        String localPath = ioService.merge(filename, SecurityUtils.getUserId(), lyrics);
        String lyrics = ioService.getUrl(ioService.getRelPath(localPath));
        return ApiResult.success(lyrics);
    }

    @PostMapping("/cancel")
    public ApiResult<Object> cancel(@RequestParam("fileHash") String fileHash) {
        ioService.cancel(fileHash, SecurityUtils.getUserId());
        return ApiResult.success();
    }

    @PostMapping("/coverImg")
    public ApiResult<Map<String, String>> upload(@RequestParam("coverImg") MultipartFile file) {
        String localPath = ioService.uploadToLocal(file, coverImg);
        String coverUrl = ioService.getUrl(ioService.getRelPath(localPath));
        Map<String, String> map = new HashMap<>();
        map.put("coverImg", coverUrl);
        return ApiResult.success(map);
    }

    @PostMapping("/listCoverImg")
    public ApiResult<Object> listCoverImg(@RequestParam("coverImg") MultipartFile file) {
        String localPath = ioService.uploadToLocal(file, coverImg);
        String coverUrl = ioService.getUrl(ioService.getRelPath(localPath));
        return ApiResult.success(coverUrl);
    }
}



