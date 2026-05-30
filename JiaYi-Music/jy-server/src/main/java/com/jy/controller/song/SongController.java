package com.jy.controller.song;

import com.jy.domain.ApiResult;
import com.jy.entity.SongEntity;
import com.jy.entity.SongListEntity;
import com.jy.service.SongService;
import com.jy.vo.requestVo.SongInputBody;
import com.jy.vo.responseVo.SongListInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/song")
public class SongController {
    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping("/upload")
    public ApiResult<Object> uploadSong(@RequestBody SongInputBody songInputBody) {
        Long id = songService.uploadSong(songInputBody);
        return ApiResult.success(id.toString());
    }

    @PutMapping("/update/{songId}")
    public ApiResult<Object> updateSong(@RequestBody SongInputBody songInputBody, @PathVariable Long songId) {
        boolean res = songService.updateSong(songInputBody, songId);
        if (!res) return ApiResult.error();
        return ApiResult.success();
    }

    @PutMapping("/updatePlayCount/{songId}")
    public ApiResult<Object> updatePlayCount(@PathVariable Long songId) {
        songService.updatePlayCountCache(songId);
        return ApiResult.success();
    }

    @DeleteMapping("/delete/{songId}")
    public ApiResult<Object> deleteSong(@PathVariable Long songId) {
        boolean res = songService.deleteSong(songId);
        if (!res) return ApiResult.error();
        return ApiResult.success();
    }

    @GetMapping("/songList")
    public ApiResult<List<SongListInfo>> getSongList() {
        List<SongListInfo> songList = songService.getSongList();
        return ApiResult.success(songList);
    }

    @PostMapping("/addSongToList/{songId}/{listId}")
    public ApiResult<Object> addSongToList(@PathVariable Long songId, @PathVariable Long listId) {
        songService.addSongToList(listId, songId);
        return ApiResult.success();
    }

    @PostMapping("/removeSongFromList/{songId}/{listId}")
    public ApiResult<Object> removeSongFromList(@PathVariable Long songId, @PathVariable Long listId) {
        songService.removeSongFromList(listId, songId);
        return ApiResult.success();
    }

    @PostMapping("/createSongList/{name}")
    public ApiResult<Object> createSongList(@PathVariable String name) {
        Long id = songService.createSongList(name);
        return ApiResult.success(id.toString());
    }

    @PostMapping("/deleteSongList/{listId}")
    public ApiResult<Object> deleteSongList(@PathVariable Long listId) {
        songService.deleteSongList(listId);
        return ApiResult.success();
    }

    @PostMapping("/updateSongList")
    public ApiResult<Object> updateSongList(@RequestBody SongListEntity songListEntity) {
        songService.updateSongList(songListEntity);
        return ApiResult.success();
    }

    @GetMapping("/getSongs")
    public ApiResult<List<SongEntity>> getUserSongs() {
        List<SongEntity> songs = songService.getUserSongs();
        return ApiResult.success(songs);
    }

    @GetMapping("/getRankSongs")
    public ApiResult<List<SongEntity>> getRankSongs() {
        List<SongEntity> songs = songService.getRankSongs();
        return ApiResult.success(songs);
    }

    @GetMapping("/download/{songId}")
    public void downloadSong(
            @PathVariable Long songId,
            // 浏览器断点续传请求头
            @RequestHeader(value = "Range", required = false)
            String rangeHeader,
            HttpServletResponse response
    ) throws IOException {

        File file = songService.getSongFileById(songId);

        // 文件不存在
        if (file == null || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 文件总大小
        long fileSize = file.length();

        // 文件名
        String fileName = file.getName();
        // ETag
        // 用于浏览器缓存校验
        String eTag = "\"" + file.lastModified() + "-" + fileSize + "\"";
        // 告诉浏览器支持断点续传
        response.setHeader("Accept-Ranges", "bytes");
        // ETag
//        response.setHeader("ETag", eTag);
//        // 最后修改时间
//        response.setDateHeader("Last-Modified", file.lastModified());
        response.setHeader(
                "Access-Control-Expose-Headers",
                "Content-Disposition,Content-Length,Content-Range"
        );
        response.setHeader(
                "Access-Control-Allow-Origin",
                "http://localhost:5173"
        );
        // Content-Disposition
        // 告诉浏览器：
        // 这是附件，需要下载
        String encodeFileName = URLEncoder.encode(
                fileName,
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodeFileName + "\"");
        // mp3
//        response.setContentType("audio/mpeg");

        response.setContentType("application/octet-stream");
        // 默认下载整个文件
        long start = 0;
        long end = fileSize - 1;

        // 是否是部分下载（断点续传）
        boolean isPartial = false;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            isPartial = true;
            String range = rangeHeader.substring(6);
            String[] parts = range.split("-");

            try {
                if (!parts[0].isEmpty()) {
                    start = Long.parseLong(parts[0]);
                }
                if (parts.length > 1 &&
                        !parts[1].isEmpty()) {

                    end = Long.parseLong(parts[1]);
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (start > end || start >= fileSize) {
                response.setHeader("Content-Range", "bytes */" + fileSize);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
            // end 超出文件大小
            if (end >= fileSize) {
                end = fileSize - 1;
            }
        }
        // 实际下载长度
        long contentLength = end - start + 1;
        if (isPartial) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            // Content-Range
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        // Content-Length
        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 输出文件流
        try (
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())
        ) {

            // 跳到开始位置
            raf.seek(start);
            // 1MB 缓冲区
            byte[] buffer = new byte[1024 * 1024];
            // 剩余未读取长度
            long remaining = contentLength;
            int len;
            // 循环读取
            while (remaining > 0 && (len = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                // 输出到浏览器
                out.write(buffer, 0, len);
                // 减少剩余长度
                remaining -= len;
            }
            // 强制刷新
            out.flush();
        } catch (IOException e) {
            // 用户取消下载
            // 浏览器中断连接
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("/getPlayRank")
    public ApiResult<Map<String, Double>> getPlayRank() {
        Map<String, Double> playRank = songService.getPlayRank();
        return ApiResult.success(playRank);
    }

    @GetMapping("/getCollectRank")
    public ApiResult<Map<String, Double>> getCollectRank() {
        Map<String, Double> collectRank = songService.getCollectRank();
        return ApiResult.success(collectRank);
    }

    @GetMapping("/getHotRank")
    public ApiResult<Map<String, Double>> getHotRank() {
        Map<String, Double> hotRank = songService.getHotRank();
        return ApiResult.success(hotRank);
    }
}
