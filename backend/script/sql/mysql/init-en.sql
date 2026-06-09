-- Business IDs are stored as BIGINT; velox.id.snowflake.enabled=true uses Snowflake and false uses database sequences.
-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: 127.0.0.1    Database: velox
-- ------------------------------------------------------
-- Server version	8.0.45

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
-- Database: `velox`
--

DROP DATABASE IF EXISTS `velox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE DATABASE IF NOT EXISTS `velox` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Use the database `velox`
--

USE `velox`;

--
-- Character set and collation for the database
--

SET NAMES utf8mb4;

--
-- Table structure for table `sys_id_sequence`
--

DROP TABLE IF EXISTS `sys_id_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_id_sequence` (
  `business_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Business Type',
  `current_value` bigint NOT NULL DEFAULT '0' COMMENT 'Current Allocated Sequence',
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated Time',
  PRIMARY KEY (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Business ID Sequence Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_id_sequence`
--

/*!40000 ALTER TABLE `sys_id_sequence` DISABLE KEYS */;
INSERT INTO `sys_id_sequence` (`business_type`, `current_value`) VALUES
  ('default',0),
  ('sys_account',0),
  ('sys_role',0),
  ('sys_menu',0),
  ('sys_profile',0),
  ('sys_account_role',0),
  ('sys_account_session',0),
  ('sys_account_security',0),
  ('sys_role_menu_permission',0),
  ('sys_file_config',0),
  ('sys_file',0),
  ('sys_file_content',0),
  ('sys_mail_group',0),
  ('sys_mail_channel',0),
  ('sys_mail_account',0),
  ('sys_mail_template',0);
/*!40000 ALTER TABLE `sys_id_sequence` ENABLE KEYS */;

--
-- Table structure for table `sys_file_config`
--

DROP TABLE IF EXISTS `sys_file_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_config` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Config Name',
  `storage` int NOT NULL COMMENT 'Storage Type (1-Database 10-Local Disk 11-FTP 12-SFTP 20-S3 Cloud Storage)',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `master` tinyint DEFAULT '0' COMMENT 'Is Master Config (0-No 1-Yes)',
  `config` json DEFAULT NULL COMMENT 'Storage Config (JSON)',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled Status (0-Disabled 1-Enabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_storage` (`storage`),
  KEY `idx_sys_file_config_master` (`master`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Config Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_config`
--

/*!40000 ALTER TABLE `sys_file_config` DISABLE KEYS */;
INSERT INTO `sys_file_config` VALUES
  ('1900000000000000090','Database Storage',1,'Database-stored files',0,'{\"domain\": \"http://127.0.0.1:3000\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000040','Local Storage',10,'Local disk storage',1,'{\"domain\": \"http://127.0.0.1:3000\", \"basePath\": \"/tmp/uploads\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000041','S3 Cloud Storage',20,'S3/OSS cloud storage (currently tested only with Alibaba Cloud; in theory all S3-compatible providers are supported. If you encounter issues, please submit an issue to help us improve and optimize.)',0,'{\"bucket\": \"bucket\", \"endpoint\": \"oss-cn-beijing.aliyuncs.com\", \"accessKey\": \"Your Access Key\", \"accessSecret\": \"Your Access Secret\", \"enablePublicAccess\": true, \"enablePathStyleAccess\": false}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000011','FTP Storage',11,'FTP file storage example',0,'{\"host\": \"127.0.0.1\", \"port\": 21, \"username\": \"ftp-user\", \"password\": \"ftp-password\", \"mode\": \"Passive\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3000\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000072','SFTP Storage',12,'SFTP file storage example',0,'{\"host\": \"127.0.0.1\", \"port\": 22, \"username\": \"sftp-user\", \"password\": \"sftp-password\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3000\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_file_config` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_group`
--

DROP TABLE IF EXISTS `sys_mail_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_group` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Group name',
  `active` tinyint DEFAULT '1' COMMENT 'Active (0-no 1-yes)',
  `sort` int DEFAULT '1' COMMENT 'Sort',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Create by',
  `update_by` bigint DEFAULT NULL COMMENT 'Update by',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-no 1-yes)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mail group table';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `sys_mail_group` DISABLE KEYS */;
INSERT INTO `sys_mail_group` VALUES
  ('1900000000000000210','Default Group',1,1,'System default mail group','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_group` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_channel`
--

DROP TABLE IF EXISTS `sys_mail_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_channel` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Channel name',
  `protocol` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Protocol (SMTP/SMTPS)',
  `active` tinyint DEFAULT '1' COMMENT 'Active (0-no 1-yes)',
  `sort` int DEFAULT '1' COMMENT 'Sort',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Create by',
  `update_by` bigint DEFAULT NULL COMMENT 'Update by',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-no 1-yes)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mail channel table';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `sys_mail_channel` DISABLE KEYS */;
INSERT INTO `sys_mail_channel` VALUES
  ('1900000000000000211','SMTP','SMTP',1,1,'SMTP plaintext/STARTTLS protocol','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000212','SMTPS','SMTPS',1,2,'SMTPS encrypted protocol','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_channel` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_account`
--

DROP TABLE IF EXISTS `sys_mail_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_account` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Name/Alias',
  `group_id` bigint DEFAULT NULL COMMENT 'Group ID',
  `channel_id` bigint DEFAULT NULL COMMENT 'Channel ID',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SMTP username',
  `password` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SMTP auth code/password',
  `from_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'From address (blank uses username)',
  `from_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'From name',
  `host` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SMTP host (blank=auto detect)',
  `port` int DEFAULT NULL COMMENT 'SMTP port (blank=auto detect)',
  `ssl_enabled` tinyint DEFAULT NULL COMMENT 'SSL (0-no 1-yes null-auto)',
  `starttls` tinyint DEFAULT NULL COMMENT 'STARTTLS (0-no 1-yes null-auto)',
  `weight` int DEFAULT '100' COMMENT 'Weight (lower=higher priority)',
  `fail_threshold` int DEFAULT '3' COMMENT 'Consecutive failures to mark abnormal',
  `retry_interval` int DEFAULT '300' COMMENT 'Retry interval (seconds)',
  `max_unavailable` int DEFAULT '5' COMMENT 'Dead after this many abnormal marks',
  `health_status` tinyint DEFAULT '0' COMMENT 'Health (0-normal 1-abnormal 2-dead)',
  `usage_count` bigint DEFAULT '0' COMMENT 'Usage count',
  `fail_count` int DEFAULT '0' COMMENT 'Current consecutive failures',
  `unavailable_count` int DEFAULT '0' COMMENT 'Total abnormal marks',
  `next_retry_time` datetime DEFAULT NULL COMMENT 'Next retry time',
  `last_used_time` datetime DEFAULT NULL COMMENT 'Last used time',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled (0-disabled 1-enabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Create by',
  `update_by` bigint DEFAULT NULL COMMENT 'Update by',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-no 1-yes)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_mail_account_group_id` (`group_id`),
  KEY `idx_sys_mail_account_channel_id` (`channel_id`),
  KEY `idx_sys_mail_account_enabled` (`enabled`),
  KEY `idx_sys_mail_account_health_status` (`health_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mail account table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_mail_template`
--

DROP TABLE IF EXISTS `sys_mail_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_template` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Template name (globally unique)',
  `send_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Sending type code (MailTemplateType)',
  `template_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CUSTOM' COMMENT 'Template type (SYSTEM/CUSTOM)',
  `subject` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Subject (may contain {{var}})',
  `content` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'HTML body (may contain {{var}})',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled (0-no 1-yes); one per send type',
  `sort` int DEFAULT '1' COMMENT 'Sort',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Create by',
  `update_by` bigint DEFAULT NULL COMMENT 'Update by',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-no 1-yes)',
  `active_name` varchar(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `name` else NULL end)) STORED COMMENT 'Active Name',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_mail_template_name` (`active_name`),
  KEY `idx_sys_mail_template_send_type` (`send_type`),
  KEY `idx_sys_mail_template_template_type` (`template_type`),
  KEY `idx_sys_mail_template_send_type_enabled` (`send_type`,`enabled`),
  KEY `idx_sys_mail_template_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mail template table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_mail_template`
--

/*!40000 ALTER TABLE `sys_mail_template` DISABLE KEYS */;
INSERT INTO `sys_mail_template`
  (`id`,`name`,`send_type`,`template_type`,`subject`,`content`,`enabled`,`sort`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
VALUES
  ('1900000000000003110','Password reset code','AUTH_RESET_PASSWORD_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are resetting your password.<br /><br />Your password reset code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please ignore this email.</p></div>',1,1,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003111','Login verification code','AUTH_LOGIN_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are signing in with an email verification code.<br /><br />Your login code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please change your password as soon as possible.</p></div>',1,2,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003112','Login two-factor code','AUTH_LOGIN_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are completing two-factor verification to sign in.<br /><br />Your verification code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please change your password as soon as possible.</p></div>',1,3,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003113','Email unbinding code','ACCOUNT_EMAIL_UNBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are unbinding your email address.<br /><br />Your verification code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please ignore this email and change your password as soon as possible.</p></div>',1,4,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003114','Email change identity verification','ACCOUNT_EMAIL_REBIND_PROOF_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are verifying your current identity to change your email address.<br /><br />Your verification code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please ignore this email and change your password as soon as possible.</p></div>',1,5,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003115','Email change code','ACCOUNT_EMAIL_REBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are changing your email address.<br /><br />Your email change code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please ignore this email and change your password.</p></div>',1,6,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003116','Two-factor code','ACCOUNT_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">Hello {{username}},<br /><br />You are using an email two-factor verification code.<br /><br />Your verification code is: {{code}}<br /><br />The code is valid for {{validityMinutes}} minutes. Please do not share it with anyone.<br /><br />If this was not you, please change your password as soon as possible.</p></div>',1,7,'System built-in template','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_template` ENABLE KEYS */;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `config_id` bigint NOT NULL COMMENT 'File Config ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Name',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Path',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'File URL',
  `type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'File Type',
  `size` bigint DEFAULT '0' COMMENT 'File Size (Bytes)',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Upload Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_id` (`config_id`),
  KEY `idx_sys_file_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Main Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_file_content`
--

DROP TABLE IF EXISTS `sys_file_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_content` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `config_id` bigint NOT NULL COMMENT 'File Config ID',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Path',
  `content` mediumblob NOT NULL COMMENT 'File Content (Binary)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_content_config_id` (`config_id`),
  KEY `idx_sys_file_content_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Content Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Name',
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Code',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Role Description',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT 'Role Type: 0-System Role, 1-Custom Role',
  `role_level` int NOT NULL DEFAULT '0' COMMENT 'Role Level, higher value means higher privileges',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled Status (0-Disabled 1-Enabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_role_code` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `role_code` else NULL end)) STORED COMMENT 'Active Role Code',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`active_role_code`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Role Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `type`, `role_level`, `enabled`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000023','Super Administrator','R_SUPER','Has all system permissions',0,3,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000073','Administrator','R_ADMIN','Has system administration permissions',0,2,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000032','Regular User','R_USER','Has regular system permissions',0,1,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;

--
-- Table structure for table `sys_account`
--

DROP TABLE IF EXISTS `sys_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Account Remark',
  `status` tinyint DEFAULT '1' COMMENT 'Account status (1-Enabled 2-Disabled 3-Abnormal 4-Cancelled)',
  `login_fail_count` int DEFAULT '0' COMMENT 'Login Failure Count',
  `login_fail_time` datetime DEFAULT NULL COMMENT 'Login Failure Time',
  `deletion_requested_at` datetime DEFAULT NULL COMMENT 'Deletion Requested At',
  `deletion_expires_at` datetime DEFAULT NULL COMMENT 'Deletion Cooling-Off Expires At',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_username` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `username` else NULL end)) STORED COMMENT 'Active Username',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`active_username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Account Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account`
--

/*!40000 ALTER TABLE `sys_account` DISABLE KEYS */;
INSERT INTO `sys_account` (`id`, `username`, `password`, `remark`, `status`, `login_fail_count`, `login_fail_time`, `deletion_requested_at`, `deletion_expires_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000027','Super','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Super administrator account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000087','Admin','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Administrator account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000048','User','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Default member account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account` ENABLE KEYS */;

--
-- Table structure for table `sys_profile`
--

DROP TABLE IF EXISTS `sys_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_profile` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nickname',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Avatar',
  `gender` tinyint DEFAULT '0' COMMENT 'Gender (0-Unknown 1-Male 2-Female 3-Other)',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Full Name',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Display Email',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Display Mobile',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Address',
  `introduction` text COLLATE utf8mb4_unicode_ci COMMENT 'Bio',
  `signature` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Signature',
  `position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Position',
  `company` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Company',
  `tags` json DEFAULT NULL COMMENT 'Tags',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_id` (`account_id`),
  CONSTRAINT `fk_sys_profile_account_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Profile Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_profile`
--

/*!40000 ALTER TABLE `sys_profile` DISABLE KEYS */;
INSERT INTO `sys_profile` VALUES
  ('1900000000000000075','1900000000000000027','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Super',2,'Prefer not to disclose','super.public@velox.web','15800158001','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000057','1900000000000000087','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Admin',1,'Prefer not to disclose','admin.public@velox.web','15800158002','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000014','1900000000000000048','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=User',1,'Prefer not to disclose','user.public@velox.web','15800158003','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_profile` ENABLE KEYS */;

--
-- Table structure for table `sys_account_role`
--

DROP TABLE IF EXISTS `sys_account_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_role` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_account_role` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT 'Active Relation Marker',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`,`role_id`,`active_account_role`),
  KEY `idx_account_role_account_id` (`account_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_account_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_account_role_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Role Relation Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_account_session`
--

DROP TABLE IF EXISTS `sys_account_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_session` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token Digest',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'Session Status (1-online 2-logged-out)',
  `login_time` datetime DEFAULT NULL COMMENT 'Login Time',
  `last_active_time` datetime DEFAULT NULL COMMENT 'Last Active Time',
  `logout_time` datetime DEFAULT NULL COMMENT 'Logout Time',
  `presence_expire_time` datetime DEFAULT NULL COMMENT 'Presence Expire Time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT 'Logical Delete (0-active 1-deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_session_token_hash` (`token_hash`),
  KEY `idx_account_session_account_id` (`account_id`),
  KEY `idx_user_session_presence_expire_time` (`presence_expire_time`),
  CONSTRAINT `fk_sys_account_session_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Login Session Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account_role`
--

/*!40000 ALTER TABLE `sys_account_role` DISABLE KEYS */;
INSERT INTO `sys_account_role` (`id`, `account_id`, `role_id`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000021','1900000000000000027','1900000000000000023','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000012','1900000000000000087','1900000000000000073','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000024','1900000000000000048','1900000000000000032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account_role` ENABLE KEYS */;

--
-- Table structure for table `sys_account_security`
--

DROP TABLE IF EXISTS `sys_account_security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_security` (
  `id` bigint NOT NULL COMMENT 'Primary key',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Bound email',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT 'password' COMMENT 'Allowed login methods (comma-separated: password,email_code)',
  `disabled_login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Admin-disabled login methods (comma-separated); user cannot enable or use',
  `disabled_oauth_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Admin-disabled third-party channels (comma-separated); user cannot enable or use',
  `mfa_email_enabled` tinyint DEFAULT '0' COMMENT 'Email MFA enabled',
  `mfa_totp_enabled` tinyint DEFAULT '0' COMMENT 'TOTP MFA enabled (placeholder)',
  `mfa_totp_secret` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP secret (placeholder)',
  `mfa_totp_recovery_codes` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP recovery code hash list (comma-separated)',
  `email_verified_at` datetime DEFAULT NULL COMMENT 'Email last verification time',
  `last_password_change_at` datetime DEFAULT NULL COMMENT 'Last password change time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Creator',
  `update_by` bigint DEFAULT NULL COMMENT 'Updater',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-active 1-deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_security_account_id` (`account_id`),
  CONSTRAINT `fk_sys_account_security_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Account security configuration';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account_security`
--

/*!40000 ALTER TABLE `sys_account_security` DISABLE KEYS */;
INSERT INTO `sys_account_security` (`id`, `account_id`, `email`, `login_methods`, `mfa_email_enabled`, `mfa_totp_enabled`, `mfa_totp_secret`, `mfa_totp_recovery_codes`, `email_verified_at`, `last_password_change_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000126','1900000000000000027','super@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000127','1900000000000000087','admin@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000128','1900000000000000048','user@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account_security` ENABLE KEYS */;

--
--
-- Table structure for table `sys_access_control`
--

DROP TABLE IF EXISTS `sys_access_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_access_control` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `general_register_enabled` tinyint DEFAULT '1' COMMENT 'General Registration Switch (0-Off 1-On)',
  `forgot_password_enabled` tinyint DEFAULT '1' COMMENT 'Forgot Password Switch (0-Off 1-On)',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled normal login methods (comma-separated)',
  `third_party_login_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled third-party login channels (comma-separated)',
  `third_party_register_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled third-party registration channels (comma-separated)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Access Control Config Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_access_control`
--

/*!40000 ALTER TABLE `sys_access_control` DISABLE KEYS */;
INSERT INTO `sys_access_control` (`id`, `general_register_enabled`, `forgot_password_enabled`, `login_methods`, `third_party_login_channels`, `third_party_register_channels`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000002010',1,1,'password,email_code','github,linuxdo','github,linuxdo','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_access_control` ENABLE KEYS */;

--
-- Table structure for table `sys_verification_policy`
--

DROP TABLE IF EXISTS `sys_verification_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_verification_policy` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `scene_key` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Scene key (login/verify_code/captcha/send_code/mfa)',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled (0-Off 1-On)',
  `max_attempts` int DEFAULT NULL COMMENT 'Max retry attempts',
  `recovery_seconds` int DEFAULT NULL COMMENT 'Recovery time (seconds)',
  `limit_by_account` tinyint DEFAULT '1' COMMENT 'Limit by account/email dimension (0/1)',
  `limit_by_ip` tinyint DEFAULT '0' COMMENT 'Limit by IP dimension (0/1)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_scene_key` varchar(32) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `scene_key` else NULL end)) STORED COMMENT 'Active scene key',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verification_policy_scene` (`active_scene_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Verification Policy Config Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_verification_policy`
--

/*!40000 ALTER TABLE `sys_verification_policy` DISABLE KEYS */;
INSERT INTO `sys_verification_policy` (`id`, `scene_key`, `enabled`, `max_attempts`, `recovery_seconds`, `limit_by_account`, `limit_by_ip`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000019001','login',1,5,1800,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019002','verify_code',1,5,600,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019003','captcha',1,10,60,0,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019004','send_code',1,1,60,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019005','mfa',1,5,300,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_verification_policy` ENABLE KEYS */;

-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `parent_id` bigint DEFAULT NULL COMMENT 'Parent Menu ID',
  `menu_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Menu Type (menu/button)',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Route Name or Permission Name',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Menu Title or Button Title',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Route Path',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Component Path',
  `redirect` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Redirect Path',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Icon',
  `auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Auth Mark',
  `is_enable` tinyint DEFAULT '1' COMMENT 'Is Enabled',
  `sort` int DEFAULT '1' COMMENT 'Sort Order',
  `keep_alive` tinyint DEFAULT '0' COMMENT 'Keep Alive',
  `is_hide` tinyint DEFAULT '0' COMMENT 'Hide Menu',
  `is_hide_tab` tinyint DEFAULT '0' COMMENT 'Hide Tab',
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'External Link',
  `is_iframe` tinyint DEFAULT '0' COMMENT 'Is Iframe',
  `show_badge` tinyint DEFAULT '0' COMMENT 'Show Badge',
  `show_text_badge` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Text Badge',
  `fixed_tab` tinyint DEFAULT '0' COMMENT 'Is Fixed Tab',
  `active_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Active Menu Path',
  `is_full_page` tinyint DEFAULT '0' COMMENT 'Is Fullscreen Page',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `auth_mark` else NULL end)) STORED COMMENT 'Active Auth Mark',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_auth_mark` (`active_auth_mark`),
  KEY `idx_menu_parent_id` (`parent_id`),
  KEY `idx_menu_auth_mark` (`auth_mark`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Menu Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `name`, `title`, `path`, `component`, `redirect`, `icon`, `auth_mark`, `is_enable`, `sort`, `keep_alive`, `is_hide`, `is_hide_tab`, `link`, `is_iframe`, `show_badge`, `show_text_badge`, `fixed_tab`, `active_path`, `is_full_page`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  -- Dashboard
  ('1900000000000000077',NULL,'menu','Dashboard','Dashboard','/dashboard','/index/index',NULL,'ri:pie-chart-line',NULL,1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000089','1900000000000000077','menu','Console','Console','console','/dashboard/console',NULL,NULL,'dashboard:console',1,1,0,0,0,NULL,0,0,NULL,1,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System
  ('1900000000000000080',NULL,'menu','System','System','/system','/index/index',NULL,'ri:user-3-line',NULL,1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Account
  ('1900000000000000016','1900000000000000080','menu','Account','Account Manage','account','/system/account',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000007','1900000000000000016','button','AccountQuery','Query',NULL,NULL,NULL,NULL,'system:account:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000020','1900000000000000016','button','AccountCreate','Create',NULL,NULL,NULL,NULL,'system:account:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000060','1900000000000000016','button','AccountUpdate','Edit',NULL,NULL,NULL,NULL,'system:account:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000030','1900000000000000016','button','AccountDelete','Delete',NULL,NULL,NULL,NULL,'system:account:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Role
  ('1900000000000000046','1900000000000000080','menu','Role','Role Manage','role','/system/role',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000085','1900000000000000046','button','RoleQuery','Query',NULL,NULL,NULL,NULL,'system:role:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000004','1900000000000000046','button','RoleCreate','Create',NULL,NULL,NULL,NULL,'system:role:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000066','1900000000000000046','button','RoleUpdate','Edit',NULL,NULL,NULL,NULL,'system:role:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000092','1900000000000000046','button','RoleDelete','Delete',NULL,NULL,NULL,NULL,'system:role:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000053','1900000000000000046','button','RolePermission','Menu Permission',NULL,NULL,NULL,NULL,'system:role:permission',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > AccountCenter
  ('1900000000000000035','1900000000000000080','menu','AccountCenter','Account Center','account-center','/system/account-center',NULL,NULL,NULL,1,3,1,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Menus
  ('1900000000000000063','1900000000000000080','menu','Menus','Menu Manage','menu','/system/menu',NULL,NULL,NULL,1,4,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000086','1900000000000000063','button','MenusQuery','Query',NULL,NULL,NULL,NULL,'system:menu:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000084','1900000000000000063','button','MenusCreate','Create',NULL,NULL,NULL,NULL,'system:menu:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000042','1900000000000000063','button','MenusUpdate','Edit',NULL,NULL,NULL,NULL,'system:menu:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000071','1900000000000000063','button','MenusDelete','Delete',NULL,NULL,NULL,NULL,'system:menu:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > FileManage
  ('1900000000000000038','1900000000000000080','menu','FileManage','File Manage','file-manage','/system/file-manage',NULL,'',NULL,1,5,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000008','1900000000000000038','button','FileQuery','Query',NULL,NULL,NULL,NULL,'system:file:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000064','1900000000000000038','button','FileCreate','Create',NULL,NULL,NULL,NULL,'system:file:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000093','1900000000000000038','button','FileUpdate','Edit',NULL,NULL,NULL,NULL,'system:file:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000069','1900000000000000038','button','FileDelete','Delete',NULL,NULL,NULL,NULL,'system:file:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000083','1900000000000000038','button','FileDownload','Download',NULL,NULL,NULL,NULL,'system:file:download',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000006','1900000000000000038','button','FileUpload','Upload',NULL,NULL,NULL,NULL,'system:file:upload',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config
  ('1900000000000000029',NULL,'menu','Config','Config','/config','/index/index',NULL,'ri:settings-3-line',NULL,1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > FileConfig
  ('1900000000000000003','1900000000000000029','menu','FileConfig','File Config','file-config','/config/file-config',NULL,'',NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000062','1900000000000000003','button','FileConfigQuery','Query',NULL,NULL,NULL,NULL,'system:file-config:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000039','1900000000000000003','button','FileConfigCreate','Create',NULL,NULL,NULL,NULL,'system:file-config:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000076','1900000000000000003','button','FileConfigUpdate','Edit',NULL,NULL,NULL,NULL,'system:file-config:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000044','1900000000000000003','button','FileConfigDelete','Delete',NULL,NULL,NULL,NULL,'system:file-config:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > MailAccount
  ('1900000000000003001','1900000000000000029','menu','MailAccount','Mail Config','mail-account','/config/mail-account',NULL,'',NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003002','1900000000000003001','button','MailAccountQuery','Query',NULL,NULL,NULL,NULL,'system:mail-account:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003003','1900000000000003001','button','MailAccountCreate','Create',NULL,NULL,NULL,NULL,'system:mail-account:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003004','1900000000000003001','button','MailAccountUpdate','Edit',NULL,NULL,NULL,NULL,'system:mail-account:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003005','1900000000000003001','button','MailAccountDelete','Delete',NULL,NULL,NULL,NULL,'system:mail-account:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003006','1900000000000003001','button','MailAccountGroup','Group',NULL,NULL,NULL,NULL,'system:mail-account:group',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003007','1900000000000003001','button','MailAccountChannel','Sending Channel',NULL,NULL,NULL,NULL,'system:mail-account:channel',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > EmailTemplate
  ('1900000000000003101','1900000000000000029','menu','EmailTemplate','Email Template','mail-template','/config/mail-template',NULL,'',NULL,1,3,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003102','1900000000000003101','button','EmailTemplateQuery','Query',NULL,NULL,NULL,NULL,'system:mail-template:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003103','1900000000000003101','button','EmailTemplateCreate','Create',NULL,NULL,NULL,NULL,'system:mail-template:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003104','1900000000000003101','button','EmailTemplateUpdate','Edit',NULL,NULL,NULL,NULL,'system:mail-template:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003105','1900000000000003101','button','EmailTemplateDelete','Delete',NULL,NULL,NULL,NULL,'system:mail-template:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings
  ('1900000000000002001',NULL,'menu','Settings','Settings','/settings','/index/index',NULL,'ri:settings-4-line',NULL,1,90,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings > AccessControl
  ('1900000000000002002','1900000000000002001','menu','AccessControl','Access Control','access-control','/settings/access-control',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002004','1900000000000002002','button','AccessControlGeneralRegister','General Registration',NULL,NULL,NULL,NULL,'settings:access-control:general-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002005','1900000000000002002','button','AccessControlThirdPartyRegister','Third-party Registration',NULL,NULL,NULL,NULL,'settings:access-control:third-party-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002006','1900000000000002002','button','AccessControlLoginMethod','Login Methods',NULL,NULL,NULL,NULL,'settings:access-control:login-method',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002007','1900000000000002002','button','AccessControlThirdPartyLogin','Third-party Login',NULL,NULL,NULL,NULL,'settings:access-control:third-party-login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002008','1900000000000002002','button','AccessControlForgotPassword','Forgot Password',NULL,NULL,NULL,NULL,'settings:access-control:forgot-password',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019010','1900000000000002001','menu','VerificationSettings','Verification Settings','verification-settings','/settings/verification-settings',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019013','1900000000000019010','button','VerificationSettingsLogin','Login Failure Lock',NULL,NULL,NULL,NULL,'settings:verification-settings:login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019014','1900000000000019010','button','VerificationSettingsVerifyCode','Verification Code Error',NULL,NULL,NULL,NULL,'settings:verification-settings:verify-code',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019015','1900000000000019010','button','VerificationSettingsCaptcha','API Rate Limiting',NULL,NULL,NULL,NULL,'settings:verification-settings:captcha',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019016','1900000000000019010','button','VerificationSettingsSendCode','Code Sending Frequency',NULL,NULL,NULL,NULL,'settings:verification-settings:send-code',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019017','1900000000000019010','button','VerificationSettingsMfa','MFA Verification Failure',NULL,NULL,NULL,NULL,'settings:verification-settings:mfa',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;

--
-- Table structure for table `sys_role_menu_permission`
--

DROP TABLE IF EXISTS `sys_role_menu_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu_permission` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `menu_id` bigint NOT NULL COMMENT 'Menu/Button ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_role_menu_permission` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT 'Active Relation Marker',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu_permission` (`role_id`,`menu_id`,`active_role_menu_permission`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menu_permission_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`),
  CONSTRAINT `fk_sys_role_menu_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Menu Permission Relation Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu_permission`
--

/*!40000 ALTER TABLE `sys_role_menu_permission` DISABLE KEYS */;
INSERT INTO `sys_role_menu_permission` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  -- R_ADMIN -> Dashboard
  ('1900000000000000028','1900000000000000073','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000033','1900000000000000073','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System
  ('1900000000000000022','1900000000000000073','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > Account
  ('1900000000000000015','1900000000000000073','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000091','1900000000000000073','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000078','1900000000000000073','1900000000000000020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000079','1900000000000000073','1900000000000000060','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000051','1900000000000000073','1900000000000000030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > Role
  ('1900000000000002042','1900000000000000073','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002044','1900000000000000073','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002045','1900000000000000073','1900000000000000004','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002046','1900000000000000073','1900000000000000066','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002047','1900000000000000073','1900000000000000092','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002048','1900000000000000073','1900000000000000053','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > FileManage
  ('1900000000000002049','1900000000000000073','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002051','1900000000000000073','1900000000000000008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002052','1900000000000000073','1900000000000000064','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002053','1900000000000000073','1900000000000000093','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002054','1900000000000000073','1900000000000000069','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002055','1900000000000000073','1900000000000000083','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002056','1900000000000000073','1900000000000000006','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> Dashboard
  ('1900000000000002057','1900000000000000032','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002058','1900000000000000032','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System
  ('1900000000000002060','1900000000000000032','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > Account
  ('1900000000000002061','1900000000000000032','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002063','1900000000000000032','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > Role
  ('1900000000000002064','1900000000000000032','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002066','1900000000000000032','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > FileManage
  ('1900000000000002067','1900000000000000032','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002069','1900000000000000032','1900000000000000008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- AccountCenter page direct grant (R_ADMIN / R_USER): page visibility = menu grant (RBAC refactor 4.4)
  ('1900000000000002070','1900000000000000073','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002071','1900000000000000032','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role_menu_permission` ENABLE KEYS */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-10 14:30:14
