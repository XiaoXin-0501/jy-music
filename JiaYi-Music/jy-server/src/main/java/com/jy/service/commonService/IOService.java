package com.jy.service.commonService;

import cn.hutool.core.io.FileUtil;
import com.jy.cache.RedisService;
import com.jy.cache.RedissonService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.RedisKey;
import com.jy.exception.MyIOException;
import com.jy.securityUtils.SecurityUtils;
import com.jy.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Component
public class IOService {
    private final String baseDir;
    private final String server;
    private final String hashTemp;
    private final RedisService redisService;

    // 构造器注入
    public IOService(@Value("${file.base-dir}") String baseDir,
                     @Value("${file.server}") String server,
                     @Value("${file.temp-hash}") String hashTemp,
                     RedisService redisService
    ) {
        this.baseDir = baseDir;
        this.server = server;
        this.hashTemp = hashTemp;
        this.redisService = redisService;
    }

    /**
     * 上传文件到本地
     *
     * @param file 前端上传的文件
     * @param path 中间路径
     * @return 本地绝对路径
     */
    public String uploadToLocal(MultipartFile file, String path) {
        String fileName = FileUtils.createFilename(file, true);
        String filePath = FileUtils.createTimePath(fileName, baseDir + File.separator + path);
        FileUtils.writeFile(file, filePath);
        return filePath;
    }

    /**
     * 上传文件到临时目录（分片上传版）
     *
     * @param file 前端上传的文件
     * @param id   用户id
     */
    public void uploadToTemp(MultipartFile file, Long id) {
        String filename = file.getOriginalFilename();
        String filePath = FileUtils.createTempHashPath(filename, baseDir + File.separator + hashTemp, id);
        FileUtils.writeFile(file, filePath);
    }

    /**
     * 清理临时目录，一个用户只能同时上传三个文件，多余的会被清理（配合分片上传）
     * D:/jy_music/jy_upload/temp/hash/id_hash/hash_index.ext
     *
     * @param id
     */
    public void clearHashTemp(Long id, Integer maxUpload) {
        try {
            List<File> myTempFiles = getUserHashTempFile(id);
            if (myTempFiles.size() <= maxUpload) return;
            //把文件按时间顺序排序
            myTempFiles.sort(Comparator.comparingLong(file -> {
                try {
                    // 获取文件创建时间
                    BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    return attrs.creationTime().toMillis();
                } catch (IOException e) {
                    throw new MyIOException(ErrorMessage.FILE_CLEAR_TEMP_FAIL);
                }
            }));
            //删除超出的临时文件
            for (int i = 0; i < myTempFiles.size() - maxUpload; i++) {
                File oldFile = myTempFiles.get(i);
                FileUtils.delete(oldFile);
            }
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_CLEAR_TEMP_FAIL);
        }
    }

    /**
     *
     * @param filename 文件hash
     * @param id       用户id
     * @param path     中间路径
     * @return 本地路径
     */
    public String merge(String filename, Long id, String path) {
        int lastDotIndex = filename.lastIndexOf('.');
        String fileHash = filename.substring(0, lastDotIndex);
        File dir = getFileByHash(id, fileHash);
        try {
            if (dir == null || !dir.exists())
                throw new MyIOException(ErrorMessage.FILE_MERGE_FAIL);
            //创建路径
            String mergePath = FileUtils.createTimePath(filename, baseDir + File.separator + path);
            //合并然后删除临时文件
            FileUtils.merge(dir, mergePath);
            String url = getUrl(getRelPath(mergePath));
            redisService.set(RedisKey.SONG_IS_UPLOAD.getKey(filename), url);
            return mergePath;
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_MERGE_FAIL);
        } finally {
            FileUtils.delete(dir);
        }
    }

    /**
     * 获取已上传分片数组
     * fileName格式:{hash}_{index}.{ext}
     *
     * @param id
     * @param fileHash
     * @return
     */
    public List<Integer> getChunks(Long id, String fileHash) {
        try {
            List<Integer> chunkIndexes = new ArrayList<>();
            File file = getFileByHash(id, fileHash);
            if (file == null) return chunkIndexes;
            File[] files = file.listFiles();
            if (files == null || files.length == 0) return chunkIndexes;
            String prefix = fileHash + "_";
            //收集分片index
            for (File f : files) {
                if (!f.isFile()) continue;
                String fileName = f.getName();
                // 只处理以 hash_ 开头的分片文件
                if (fileName.startsWith(prefix)) {
                    String temp = fileName.substring(prefix.length());
                    int dotIndex = temp.lastIndexOf(".");
                    if (dotIndex > 0) {
                        String indexStr = temp.substring(0, dotIndex);
                        chunkIndexes.add(Integer.parseInt(indexStr));
                    }
                }
            }
            return chunkIndexes;
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_GET_CHUNKS_FAIL);
        }
    }

    public void cancel(String fileHash, Long id) {
        File file = getFileByHash(id, fileHash);
        FileUtils.delete(file);
    }

    public String fileExist(String path, String filename) {
        String url = redisService.get(
                RedisKey.SONG_IS_UPLOAD.getKey(filename),
                String.class
        );
        if (url != null) {
            return url;
        }
        Path filePath = FileUtils.fileExist(baseDir + File.separator + path, file ->
                file.getFileName().toString().endsWith(filename)
        );
        if (filePath == null) {
            return null;
        }
        url = getUrl(getRelPath(filePath.toAbsolutePath().toString()));
        redisService.set(RedisKey.SONG_IS_UPLOAD.getKey(filename), url);
        return url;

    }

    /**
     * 通过filehash获取hash/temp目录下用户的filehash文件
     *
     * @param id
     * @param fileHash
     * @return
     */
    public File getFileByHash(Long id, String fileHash) {
        List<File> myTempFiles = getUserHashTempFile(id);
        if (myTempFiles.isEmpty()) return null;
        for (File file : myTempFiles) {
            String fileName = file.getName();
            String prefix = id + "_";
            if (fileName.startsWith(prefix)) {
                String hash = fileName.substring(prefix.length());
                if (fileHash.equals(hash)) return file;
            }
        }
        return null;
    }

    public List<File> getUserHashTempFile(Long id) {
        List<File> myTempFiles = new ArrayList<>();
        File[] files = new File(baseDir + File.separator + hashTemp).listFiles();
        if (files == null || files.length == 0) return myTempFiles;
        String prefix = id + "_";
        for (File file : files) {
            if (file.getName().startsWith(prefix)) {
                myTempFiles.add(file);
            }
        }
        return myTempFiles;
    }

    /**
     * 通过本地路径，拿到相对路径（用于存储到数据库）
     *
     * @param localPath 本地路径
     * @return 相对路径
     */
    public String getRelPath(String localPath) {
        localPath = localPath.replace("\\", "/");
        return localPath.replace(baseDir, "");
    }

    /**
     * 通过相对路径拿到网络地址（用于用户访问）
     *
     * @param realPath 相对路径
     * @return 网络地址
     */
    public String getUrl(String realPath) {
        return (server + realPath).replace("\\", "/");
    }

    public String getLocalPath(String url) {
        return baseDir + url.replace(server, "").replace("/", "\\");
    }
}
