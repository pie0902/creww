-- MySQL dump 10.13  Distrib 8.0.36, for macos14 (arm64)
--
-- Host: localhost    Database: creww
-- ------------------------------------------------------
-- Server version	8.0.36

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
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board`
--

LOCK TABLES `board` WRITE;
/*!40000 ALTER TABLE `board` DISABLE KEYS */;
INSERT INTO `board` VALUES (1,'2024-05-22 13:37:37.446662','2024-05-22 13:37:37.446662','this is testBoard2','Example Board',1),(2,'2024-05-22 13:37:40.032768','2024-05-22 13:37:40.032768','this is testBoard2','Example Board2',1),(3,'2024-05-26 12:34:20.881865','2024-05-26 12:34:20.881865','asdf','asdf',1);
/*!40000 ALTER TABLE `board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `post_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,'test comment',1,1,'teste'),(2,'test comment2',1,1,'teste'),(4,'test comment',1,1,'teste'),(5,'test comment',1,1,'teste'),(6,'test comment',1,1,'teste'),(7,'test comment',1,1,'teste'),(8,'test comment',1,1,'teste'),(9,'test comment',1,1,'teste'),(10,'test comment',1,1,'teste'),(11,'test comment',1,1,'teste'),(12,'test comment',1,1,'teste'),(13,'test comment',1,1,'teste'),(14,'test comment',1,1,'teste'),(15,'test comment',1,1,'teste'),(16,'zz',1,1,'teste'),(17,'zzz',1,1,'teste'),(18,'zzz',4,1,'teste'),(19,'zzzzzzz',1,1,'teste');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,'2024-06-06 17:48:55.232000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',1),(2,'2024-06-06 17:48:55.237000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',2),(3,'2024-06-06 17:48:55.238000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',3),(4,'2024-06-06 17:48:55.239000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',4),(5,'2024-06-06 17:48:55.241000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',5),(6,'2024-06-06 17:48:55.242000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',6),(7,'2024-06-06 17:48:55.243000',_binary '\0','teste님이 알림 을 작성 하셨습니다.',7);
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `board_id` bigint DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `views` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (1,'2024-05-22 14:32:31.989315','2024-05-29 20:01:19.800000',1,'content3','test3',1,82),(2,'2024-05-22 14:32:38.370372','2024-05-26 11:24:27.025046',1,'content1','test1',1,8),(3,'2024-05-22 14:32:46.729826','2024-05-29 20:01:16.850000',1,'content','test',1,2),(4,'2024-05-26 13:58:37.680110','2024-05-26 13:58:40.618567',1,'aaa','aa',1,1),(5,'2024-05-26 13:58:51.340005','2024-05-26 14:00:07.597116',3,'zzz','zz',1,3),(6,'2024-05-26 13:59:05.432254','2024-05-26 13:59:05.432254',2,'zzz','zzz',1,0),(7,'2024-06-05 15:18:53.632000','2024-06-05 15:19:22.852000',1,'test comment update','hi(update)',1,0),(8,'2024-06-06 17:48:55.107000','2024-06-06 17:48:55.107000',1,'알림','알림',1,0);
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'test@test.com','$2a$10$W6Q4F8SOHwVOTjCSYiBPrOo222.w411cjg3UH29mE5yKsw.q9dd12','teste'),(2,'test2@test.com','$2a$10$OW00BztwIQaO7vsF6meXYupfgjT6y3EmMb6nkKq7IVooVFhZIYYvq','teste2'),(3,'test3@test.com','$2a$10$7eRQCcwsMlA7Xg3tSRc7KeVgPfjKfQT1OOComxg/fWp4KAyKTAPZi','teste3'),(4,'test5@test.com','$2a$10$TurESIsz3N8Z28b/7POcn.lYccGXrq2RP8yzXwuV99Pe3hs7ewUc.','tester5'),(5,'test6@test.com','$2a$10$QromWFDhAJpmfcZ9oyq5Te750W9XumL0ggTN3OxEnhLILukHr8Mwq','tester6'),(6,'aa@123','$2a$10$A1DP4wNWCTikAYK8tj/NJecb16byPbzr37gQ2ul9hMKwzshKfZREW','aaa'),(7,'adsf@123','$2a$10$Rah4CZIJ3pKNr2W5hYYGj.DXwnRxtok9iz.mpupb6PmaZYfN.SfoS','aaa'),(8,'v1@test.com','$2a$10$ozycZR0/OA4WH5D4.f0pNubPaF6E0efgm50c/TahYn4SygPprCM6m','v1');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_board`
--

DROP TABLE IF EXISTS `user_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_board` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `is_exited` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_board`
--

LOCK TABLES `user_board` WRITE;
/*!40000 ALTER TABLE `user_board` DISABLE KEYS */;
INSERT INTO `user_board` VALUES (1,1,1,_binary '\0'),(2,1,2,_binary '\0'),(3,1,3,_binary '\0'),(4,2,1,_binary '\0'),(5,2,2,_binary '\0'),(6,2,3,_binary '\0'),(7,3,2,_binary '\0'),(8,3,1,_binary '\0'),(9,1,4,_binary '\0'),(10,1,5,_binary '\0'),(11,1,6,_binary '\0'),(12,1,7,_binary '\0');
/*!40000 ALTER TABLE `user_board` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-21 14:53:25
