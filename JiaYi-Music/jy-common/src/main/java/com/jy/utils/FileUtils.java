package com.jy.utils;

import com.jy.constant.ContentType;
import com.jy.constant.ErrorMessage;
import com.jy.exception.MyIOException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileUtils {
    /**
     *
     * @param file     前端上传的文件
     * @param filePath 完整路径
     * @return
     */
    public static void writeFile(MultipartFile file, String filePath) {
        File dest = new File(filePath);
        File parent = dest.getParentFile();
        if (!parent.exists()) {
            throw new MyIOException(ErrorMessage.FILE_PATH_NOT_EXIST);
        }
        try {
            file.transferTo(dest);

        } catch (IOException e) {
            throw new MyIOException(ErrorMessage.FILE_WRITE_FAIL);
        }
    }

    /**
     * 创建完整临时存储路径,用于配合前端分片上传
     * fileName格式:{hash}_{index}.{ext}
     *
     * @param filename 文件名(带后缀)
     * @param basePath 基目录:D:/jy_music/jy_upload/temp
     * @return 完整路径D:/jy_music/jy_upload/temp/hash/id_hash/hash_index.ext
     */
    public static String createTempHashPath(String filename, String basePath, Long id) {
        try {
            int lastDotIndex = filename.lastIndexOf('.');
            //去后缀部分
            String nameWithoutExt = filename.substring(0, lastDotIndex);
            int underLineIndex = nameWithoutExt.lastIndexOf('_');
            String hash = nameWithoutExt.substring(0, underLineIndex);
            String dirPath = basePath + File.separator + File.separator + id + "_" + hash;
            File directory = new File(dirPath);
            directory.mkdirs();
            return dirPath + File.separator + filename;
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_CREATE_FAIL);
        }
    }

    /**
     * 创建本地父级存储路径,用于普通存储
     *
     * @param filename
     * @return D:/jy_music/jy_upload/{other}/year/month/day_uuid.ext
     */
    public static String createTimePath(String filename, String basePath) {
        try {
            LocalDate now = LocalDate.now();
            String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
            String month = now.format(DateTimeFormatter.ofPattern("MM"));
            String day = now.format(DateTimeFormatter.ofPattern("dd"));

            String dirPath = basePath + File.separator + year + File.separator + month;
            File directory = new File(dirPath);
            directory.mkdirs();
            return dirPath + File.separator + day + "_" + filename;
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_CREATE_FAIL);
        }
    }

    public static String createFilename(MultipartFile file, boolean useUuid) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String ext = "";
        String name = "";
        //文件名为空，直接使用uuid作为文件名，自动判断后缀
        if (filename == null || filename.isEmpty()) {
            ext = getFileSuffix(contentType);
            name = IdUtils.uuid();
            return name + ext;
        }
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex > 0) {
            ext = filename.substring(lastDotIndex);
            name = filename.substring(0, lastDotIndex);
        } else {
            ext = getFileSuffix(contentType);
            name = filename;
        }
        if (useUuid) {
            return IdUtils.uuid() + ext;
        } else {
            return name + ext;
        }
    }

    /**
     * 递归删除file以及file下的所有文件
     *
     * @param file 可以是文件夹和文件，会删除自身
     */
    public static void delete(File file) {
        if (file == null) return;
        if (!file.exists()) {
            throw new MyIOException(ErrorMessage.FILE_PATH_NOT_EXIST);
        }
        try {
            if (file.isDirectory()) {

                File[] files = file.listFiles();

                if (files != null) {
                    for (File f : files) {
                        delete(f);
                    }
                }

            }
            if (!file.delete()) {
                throw new MyIOException(ErrorMessage.FILE_DELETE_FAIL);
            }
        } catch (Exception e) {
            throw new MyIOException(ErrorMessage.FILE_DELETE_FAIL);
        }
    }

    /**
     * 合并文件夹下所有文件，放入目标地址，用于配合前端分片上传
     *
     * @param file       需要合并的文件的文件夹
     * @param targetPath 目标地址，包含文件名
     */
    public static void merge(File file, String targetPath) {
        // 1. 校验
        if (file == null || !file.exists() || !file.isDirectory()) {
            throw new MyIOException(ErrorMessage.FILE_PATH_NOT_EXIST);
        }

        // 2. 获取所有分片并按数字排序（0,1,2,3...）
        File[] parts = file.listFiles();
        if (parts == null || parts.length == 0) {
            throw new MyIOException(ErrorMessage.FILE_IS_EMPTY);
        }

        // 按文件名数字排序
        Arrays.sort(parts, Comparator.comparingInt(f -> getChunkIndex(f.getName())));

        // 3. 合并流
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetPath))) {
            for (File part : parts) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(part))) {
                    bis.transferTo(bos);
                }
            }
        } catch (IOException e) {
            throw new MyIOException(ErrorMessage.FILE_MERGE_FAIL);
        }
    }

    private static int getChunkIndex(String fileName) {

        int underLine =
                fileName.lastIndexOf('_');

        int dot =
                fileName.lastIndexOf('.');

        return Integer.parseInt(
                fileName.substring(
                        underLine + 1,
                        dot
                )
        );
    }


    public static Path fileExist(String rootDir, Predicate<Path> matcher) {
        Path rootPath = Paths.get(rootDir);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            return null;
        }
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(matcher)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new MyIOException(ErrorMessage.FILE_FIND_FAIL);
        }
    }

    /**
     * 通过文件名，查询本地根目录下是否存在该文件
     *
     * @param rootDir        根目录
     * @param targetFileName 文件名
     * @return 是否存在
     */
    public static Path fileExist(String rootDir, String targetFileName) {
        return fileExist(rootDir, file ->
                file.getFileName().toString().equals(targetFileName)
        );
    }

    /**
     * 获取后缀方法
     *
     * @param contentType 类型
     * @return 后缀
     */
    private static String getFileSuffix(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        for (ContentType ct : ContentType.values()) {
            if (ct.getContentType().equalsIgnoreCase(contentType)) {
                return ct.getSuffix();
            }
        }
        return "";
    }
}
