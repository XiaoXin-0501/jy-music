package com.jy.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * datacenterId	机房 / 集群标识	有多机房才用
 * workerId 机器ID（节点ID） 必须关心
 */
public class IdUtils {
    /**
     * 机器ID（必须唯一）
     */
    private static final long WORKER_ID = getWorkerId();

    /**
     * 数据中心ID
     */
    private static final long DATACENTER_ID = 1;

    /**
     * 雪花算法实例（单例）
     */
    private static final Snowflake SNOWFLAKE =
            IdUtil.getSnowflake(WORKER_ID, DATACENTER_ID);

    private IdUtils() {
    }

    /**
     * 标准UUID（32位，无'-'）
     * 安全性更高
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 高性能UUID（无加密随机）
     * 适用于高并发、非安全场景
     */
    public static String fastUuid() {
        return new UUID(
                ThreadLocalRandom.current().nextLong(),
                ThreadLocalRandom.current().nextLong()
        ).toString().replace("-", "");
    }

    /**
     * 雪花ID（long）
     * 用于数据库主键 / 分布式ID
     */
    public static long snowflake() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 雪花ID字符串
     */
    public static String snowflakeStr() {
        return SNOWFLAKE.nextIdStr();
    }

    private static long getWorkerId() {
        //写死
        return 1;
        //根据机器信息生成
        // return Math.abs(SystemUtils.getHostName().hashCode()) % 32;

        //Redis / 配置中心分配
    }
}
