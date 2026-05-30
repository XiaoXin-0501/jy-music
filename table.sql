-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: jy_music
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `jy_category`
--

DROP TABLE IF EXISTS `jy_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jy_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT '分类名称不能重复'
) ENGINE=InnoDB AUTO_INCREMENT=4305 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌曲分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jy_category`
--

LOCK TABLES `jy_category` WRITE;
/*!40000 ALTER TABLE `jy_category` DISABLE KEYS */;
INSERT INTO `jy_category` VALUES (4104,'乡村乐'),(4201,'伤感'),(4205,'励志'),(4103,'古典乐'),(4001,'国语'),(4302,'学习'),(4202,'安静'),(4102,'摇滚乐'),(4005,'日语'),(4203,'欢快'),(4204,'治愈'),(4006,'法语'),(4101,'流行乐'),(4105,'爵士乐'),(4206,'甜蜜'),(4301,'睡前'),(4003,'粤语'),(4304,'约会'),(4002,'英语'),(4106,'轻音乐'),(4303,'运动'),(4004,'韩语');
/*!40000 ALTER TABLE `jy_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jy_role`
--

DROP TABLE IF EXISTS `jy_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jy_role` (
  `id` bigint NOT NULL,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jy_role`
--

LOCK TABLES `jy_role` WRITE;
/*!40000 ALTER TABLE `jy_role` DISABLE KEYS */;
INSERT INTO `jy_role` VALUES (1003,'vip用户'),(1002,'普通用户'),(1001,'管理员');
/*!40000 ALTER TABLE `jy_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jy_song`
--

DROP TABLE IF EXISTS `jy_song`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jy_song` (
  `id` bigint NOT NULL COMMENT '歌曲id',
  `account` varchar(100) DEFAULT NULL COMMENT '发布账号',
  `singer` varchar(100) DEFAULT NULL COMMENT '歌手',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面',
  `name` varchar(200) NOT NULL COMMENT '歌名',
  `lyrics` varchar(200) DEFAULT NULL COMMENT '歌词',
  `lyricist` varchar(100) DEFAULT NULL COMMENT '作词人',
  `composer` varchar(100) DEFAULT NULL COMMENT '作曲人',
  `duration` varchar(50) DEFAULT NULL COMMENT '播放时长',
  `play_count` int DEFAULT '0' COMMENT '播放次数',
  `collect_count` int DEFAULT '0' COMMENT '收藏数',
  `status` tinyint DEFAULT '1' COMMENT '状态 0不可播放 1正常',
  `sort` int DEFAULT NULL COMMENT '排序权重',
  `song_url` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌曲表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jy_song`
--

LOCK TABLES `jy_song` WRITE;
/*!40000 ALTER TABLE `jy_song` DISABLE KEYS */;
/*!40000 ALTER TABLE `jy_song` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jy_song_list`
--

DROP TABLE IF EXISTS `jy_song_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jy_song_list` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '列表id',
  `name` varchar(100) NOT NULL COMMENT '列表名称',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '列表封面',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2058712905305690113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户歌单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jy_song_list`
--

LOCK TABLES `jy_song_list` WRITE;
/*!40000 ALTER TABLE `jy_song_list` DISABLE KEYS */;
INSERT INTO `jy_song_list` VALUES (100100,'我的喜欢',NULL,NULL,NULL),(100101,'我的上传',NULL,NULL,NULL),(100110,'我的下载',NULL,NULL,NULL);
/*!40000 ALTER TABLE `jy_song_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jy_user`
--

DROP TABLE IF EXISTS `jy_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jy_user` (
  `id` bigint NOT NULL,
  `nick_name` varchar(255) NOT NULL,
  `account` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `sex` enum('0','1','2') DEFAULT '2',
  `status` enum('0','1') DEFAULT '0',
  `login_ip` varchar(45) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `login_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `birthday` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jy_user`
--

LOCK TABLES `jy_user` WRITE;
/*!40000 ALTER TABLE `jy_user` DISABLE KEYS */;
INSERT INTO `jy_user` VALUES (1001,'小新','小新','$2a$10$WC32ytDY8DaO0.1gpTvoMO4aCwpRm/e0lQgMRJMf3GVZZW0imz.MS','@qq.com','2','0','IpUtils.getIpAddr()','/jy_upload/user/avatar/2026/05/20_696ffa4a3ab04acabccff88873a56786.png','2026-05-29 14:31:00','2025-11-12 00:00:00','2026-05-29 14:30:59','2005-01-08');
/*!40000 ALTER TABLE `jy_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `song_category`
--

DROP TABLE IF EXISTS `song_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `song_category` (
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`song_id`,`category_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌曲-分类关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `song_category`
--

LOCK TABLES `song_category` WRITE;
/*!40000 ALTER TABLE `song_category` DISABLE KEYS */;
INSERT INTO `song_category` VALUES (2057397728761221120,4001),(2057397728761221120,4006),(2057395443473715200,4101),(2057395443473715200,4105);
/*!40000 ALTER TABLE `song_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `song_list_song`
--

DROP TABLE IF EXISTS `song_list_song`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `song_list_song` (
  `list_id` bigint NOT NULL COMMENT '歌单ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  PRIMARY KEY (`list_id`,`song_id`),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌单-歌曲关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `song_list_song`
--

LOCK TABLES `song_list_song` WRITE;
/*!40000 ALTER TABLE `song_list_song` DISABLE KEYS */;
/*!40000 ALTER TABLE `song_list_song` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `song_list_user`
--

DROP TABLE IF EXISTS `song_list_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `song_list_user` (
  `list_id` bigint NOT NULL COMMENT '歌单ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌单用户关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `song_list_user`
--

LOCK TABLES `song_list_user` WRITE;
/*!40000 ALTER TABLE `song_list_user` DISABLE KEYS */;
INSERT INTO `song_list_user` VALUES (100100,1001),(100101,1001),(100110,1001);
/*!40000 ALTER TABLE `song_list_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1001,1001,'管理员');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-29 14:48:57
