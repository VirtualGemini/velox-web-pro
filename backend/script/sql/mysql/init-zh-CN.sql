-- 业务主键统一存储为 BIGINT；velox.id.snowflake.enabled=true 使用雪花 ID，false 使用数据库序列。
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
  `business_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型',
  `current_value` bigint NOT NULL DEFAULT '0' COMMENT '当前已分配序号',
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务主键序列表';
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
  ('sys_mail_template',0),
  ('sys_login_log',0),
  ('sys_operation_log',0),
  ('sys_api_log',0);
/*!40000 ALTER TABLE `sys_id_sequence` ENABLE KEYS */;

--
-- Table structure for table `sys_file_config`
--

DROP TABLE IF EXISTS `sys_file_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `storage` int NOT NULL COMMENT '存储器(1-数据库 10-本地磁盘 11-FTP 12-SFTP 20-S3云存储)',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `master` tinyint DEFAULT '0' COMMENT '是否主配置(0-否 1-是)',
  `config` json DEFAULT NULL COMMENT '存储器配置(JSON)',
  `enabled` tinyint DEFAULT '1' COMMENT '启用状态(0-禁用 1-启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_storage` (`storage`),
  KEY `idx_sys_file_config_master` (`master`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_config`
--

/*!40000 ALTER TABLE `sys_file_config` DISABLE KEYS */;
INSERT INTO `sys_file_config` VALUES
  ('1900000000000000090','数据库存储',1,'数据库存储文件',0,'{\"domain\": \"http://127.0.0.1:3000\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000040','本地存储',10,'本地磁盘存储',1,'{\"domain\": \"http://127.0.0.1:3000\", \"basePath\": \"/tmp/uploads\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000041','S3云存储',20,'S3/OSS云存储(当前仅测试阿里云，理论上 S3 类型都支持，若遇到问题请提出您宝贵的 issue，这将有利于我们完善和优化)',0,'{\"bucket\": \"bucket\", \"endpoint\": \"oss-cn-beijing.aliyuncs.com\", \"accessKey\": \"Your Access Key\", \"accessSecret\": \"Your Access Secret\", \"enablePublicAccess\": true, \"enablePathStyleAccess\": false}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000011','FTP存储',11,'FTP 文件存储示例',0,'{\"host\": \"127.0.0.1\", \"port\": 21, \"username\": \"ftp-user\", \"password\": \"ftp-password\", \"mode\": \"Passive\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3000\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000072','SFTP存储',12,'SFTP 文件存储示例',0,'{\"host\": \"127.0.0.1\", \"port\": 22, \"username\": \"sftp-user\", \"password\": \"sftp-password\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3000\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_file_config` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_group`
--

DROP TABLE IF EXISTS `sys_mail_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_group` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称',
  `active` tinyint DEFAULT '1' COMMENT '是否生效(0-否 1-是)',
  `sort` int DEFAULT '1' COMMENT '排序',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发件分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `sys_mail_group` DISABLE KEYS */;
INSERT INTO `sys_mail_group` VALUES
  ('1900000000000000210','默认分组',1,1,'系统默认发件分组','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_group` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_channel`
--

DROP TABLE IF EXISTS `sys_mail_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_channel` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `protocol` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '协议(SMTP/SMTPS)',
  `active` tinyint DEFAULT '1' COMMENT '是否生效(0-否 1-是)',
  `sort` int DEFAULT '1' COMMENT '排序',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发件渠道表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 ALTER TABLE `sys_mail_channel` DISABLE KEYS */;
INSERT INTO `sys_mail_channel` VALUES
  ('1900000000000000211','SMTP','SMTP',1,1,'SMTP 明文/STARTTLS 发件协议','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000212','SMTPS','SMTPS',1,2,'SMTPS 加密发件协议','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_channel` ENABLE KEYS */;

--
-- Table structure for table `sys_mail_account`
--

DROP TABLE IF EXISTS `sys_mail_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mail_account` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称/别名',
  `group_id` bigint DEFAULT NULL COMMENT '所属分组ID',
  `channel_id` bigint DEFAULT NULL COMMENT '所属渠道ID',
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SMTP用户名',
  `password` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SMTP授权码/密码',
  `from_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发件地址(留空用username)',
  `from_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发件人名称',
  `host` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SMTP主机(留空自动识别)',
  `port` int DEFAULT NULL COMMENT 'SMTP端口(留空自动识别)',
  `ssl_enabled` tinyint DEFAULT NULL COMMENT '是否SSL(0-否 1-是 null-自动)',
  `starttls` tinyint DEFAULT NULL COMMENT '是否STARTTLS(0-否 1-是 null-自动)',
  `weight` int DEFAULT '100' COMMENT '权重(越低越优先)',
  `fail_threshold` int DEFAULT '3' COMMENT '触发异常的连续失败次数',
  `retry_interval` int DEFAULT '300' COMMENT '重试间隔(秒)',
  `max_unavailable` int DEFAULT '5' COMMENT '累计异常达到该次数则死亡',
  `health_status` tinyint DEFAULT '0' COMMENT '健康状态(0-正常 1-异常 2-死亡)',
  `usage_count` bigint DEFAULT '0' COMMENT '已使用次数',
  `fail_count` int DEFAULT '0' COMMENT '当前连续失败次数',
  `unavailable_count` int DEFAULT '0' COMMENT '累计被标记异常次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次可重试时间',
  `last_used_time` datetime DEFAULT NULL COMMENT '上次使用时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `enabled` tinyint DEFAULT '1' COMMENT '启用状态(0-禁用 1-启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_mail_account_group_id` (`group_id`),
  KEY `idx_sys_mail_account_channel_id` (`channel_id`),
  KEY `idx_sys_mail_account_enabled` (`enabled`),
  KEY `idx_sys_mail_account_health_status` (`health_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发件邮箱表';
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
  ('1900000000000003110','密码重置验证码','AUTH_RESET_PASSWORD_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行忘记密码操作。<br /><br />本次密码重置验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件。</p></div>',1,1,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003111','登录验证码','AUTH_LOGIN_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在通过邮箱验证码登录。<br /><br />本次登录验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,2,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003112','登录二次验证码','AUTH_LOGIN_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在通过邮箱二次验证完成登录。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,3,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003113','邮箱解绑验证码','ACCOUNT_EMAIL_UNBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行邮箱解绑操作。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并尽快修改密码。</p></div>',1,4,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003114','邮箱换绑身份验证','ACCOUNT_EMAIL_REBIND_PROOF_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在验证当前身份，以继续执行邮箱换绑。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并尽快修改密码。</p></div>',1,5,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003115','邮箱换绑验证码','ACCOUNT_EMAIL_REBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行邮箱换绑操作。<br /><br />本次换绑验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并修改密码。</p></div>',1,6,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003116','二次验证码','ACCOUNT_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在使用邮箱二次验证码。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,7,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_mail_template` ENABLE KEYS */;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '文件配置ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件URL',
  `type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
  `size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_id` (`config_id`),
  KEY `idx_sys_file_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_file_content`
--

DROP TABLE IF EXISTS `sys_file_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_content` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '文件配置ID',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
  `content` mediumblob NOT NULL COMMENT '文件内容(二进制)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_content_config_id` (`config_id`),
  KEY `idx_sys_file_content_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '角色类型：0系统角色，1自定义角色',
  `role_level` int NOT NULL DEFAULT '0' COMMENT '角色等级，值越大权限越高',
  `enabled` tinyint DEFAULT '1' COMMENT '启用状态(0-禁用 1-启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_role_code` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `role_code` else NULL end)) STORED COMMENT '未删除角色编码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`active_role_code`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `type`, `role_level`, `enabled`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000023','超级管理员','R_SUPER','拥有系统全部权限',0,3,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000073','管理员','R_ADMIN','拥有系统管理权限',0,2,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000032','普通用户','R_USER','拥有系统普通权限',0,1,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;

--
-- Table structure for table `sys_account`
--

DROP TABLE IF EXISTS `sys_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账号备注',
  `status` tinyint DEFAULT '1' COMMENT '账号状态(1-启用 2-禁用 3-异常 4-注销)',
  `login_fail_count` int DEFAULT '0' COMMENT '登录失败次数',
  `login_fail_time` datetime DEFAULT NULL COMMENT '登录失败时间',
  `deletion_requested_at` datetime DEFAULT NULL COMMENT '申请删除时间',
  `deletion_expires_at` datetime DEFAULT NULL COMMENT '删除冷静期到期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_username` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `username` else NULL end)) STORED COMMENT '未删除用户名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`active_username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统账号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account`
--

/*!40000 ALTER TABLE `sys_account` DISABLE KEYS */;
INSERT INTO `sys_account` (`id`, `username`, `password`, `remark`, `status`, `login_fail_count`, `login_fail_time`, `deletion_requested_at`, `deletion_expires_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000027','Super','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','超级管理员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000087','Admin','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','管理员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000048','User','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','默认成员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account` ENABLE KEYS */;

--
-- Table structure for table `sys_profile`
--

DROP TABLE IF EXISTS `sys_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_profile` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '账号ID',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `gender` tinyint DEFAULT '0' COMMENT '性别(0-未知 1-男 2-女 3-其它)',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '展示邮箱',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '展示手机号',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
  `introduction` text COLLATE utf8mb4_unicode_ci COMMENT '个人介绍',
  `signature` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人签名',
  `position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '岗位',
  `company` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '在职企业',
  `tags` json DEFAULT NULL COMMENT '标签',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_id` (`account_id`),
  CONSTRAINT `fk_sys_profile_account_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_profile`
--

/*!40000 ALTER TABLE `sys_profile` DISABLE KEYS */;
INSERT INTO `sys_profile` VALUES
  ('1900000000000000075','1900000000000000027','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=Super',2,'不愿透露','super.public@velox.web','15800158001','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','[\"可爱\", \"很可爱\", \"非常可爱\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000057','1900000000000000087','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=Admin',1,'不愿透露','admin.public@velox.web','15800158002','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','[\"可爱\", \"很可爱\", \"非常可爱\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000014','1900000000000000048','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=User',1,'不愿透露','user.public@velox.web','15800158003','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','[\"可爱\", \"很可爱\", \"非常可爱\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_profile` ENABLE KEYS */;

--
-- Table structure for table `sys_account_role`
--

DROP TABLE IF EXISTS `sys_account_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '账号ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_account_role` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT '未删除关联标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`,`role_id`,`active_account_role`),
  KEY `idx_account_role_account_id` (`account_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_account_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_account_role_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_account_session`
--

DROP TABLE IF EXISTS `sys_account_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_session` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '账号ID',
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token摘要',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '会话状态(1-在线 2-已退出)',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活动时间',
  `logout_time` datetime DEFAULT NULL COMMENT '退出时间',
  `presence_expire_time` datetime DEFAULT NULL COMMENT '在线状态过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_session_token_hash` (`token_hash`),
  KEY `idx_account_session_account_id` (`account_id`),
  KEY `idx_user_session_presence_expire_time` (`presence_expire_time`),
  CONSTRAINT `fk_sys_account_session_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号登录会话表';
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
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '账号ID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绑定邮箱',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT 'password' COMMENT '允许的登录方式（逗号分隔: password,email_code）',
  `disabled_login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员禁用的登录方式（逗号分隔），用户不可开启或使用',
  `disabled_oauth_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员禁用的第三方登录渠道（逗号分隔），用户不可开启或使用',
  `mfa_email_enabled` tinyint DEFAULT '0' COMMENT '邮箱二段验证是否启用',
  `mfa_totp_enabled` tinyint DEFAULT '0' COMMENT 'TOTP 二段验证是否启用（占位）',
  `mfa_totp_secret` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP 密钥（占位）',
  `mfa_totp_recovery_codes` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP 恢复码哈希列表（逗号分隔）',
  `email_verified_at` datetime DEFAULT NULL COMMENT '邮箱最近验证时间',
  `last_password_change_at` datetime DEFAULT NULL COMMENT '最近一次修改密码时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_security_account_id` (`account_id`),
  CONSTRAINT `fk_sys_account_security_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号安全配置表';
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
-- Table structure for table `sys_login_log`
--

DROP TABLE IF EXISTS `sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint DEFAULT NULL COMMENT '账号ID',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录事件类型',
  `login_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录方式',
  `mfa_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二次验证类型',
  `result` tinyint DEFAULT NULL COMMENT '结果(1-成功 0-失败)',
  `failure_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败编码',
  `failure_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败信息',
  `session_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话ID',
  `logout_time` datetime DEFAULT NULL COMMENT '登出会话时间',
  `first_login` tinyint DEFAULT NULL COMMENT '是否首次登录(1-是 0-否)',
  `risk_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录风险类型',
  `trace_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链路ID',
  `client_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端IP',
  `ip_version` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP版本',
  `country_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家编码',
  `country_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家名称',
  `province_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份名称',
  `city_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市名称',
  `district_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县名称',
  `ip_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `isp` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运营商',
  `location_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置来源',
  `location_parsed_at` datetime DEFAULT NULL COMMENT '位置解析时间',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT 'User-Agent',
  `browser` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器',
  `os` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作系统',
  `device_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_login_log_event_time` (`event_time`),
  KEY `idx_login_log_account_id` (`account_id`),
  KEY `idx_login_log_username` (`username`),
  KEY `idx_login_log_client_ip` (`client_ip`),
  KEY `idx_login_log_trace_id` (`trace_id`),
  KEY `idx_login_log_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_operation_log`
--

DROP TABLE IF EXISTS `sys_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `module_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模块名称',
  `action_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作名称',
  `operation_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型',
  `target_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标类型',
  `target_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标ID',
  `account_id` bigint DEFAULT NULL COMMENT '账号ID',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `operator_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作者类型',
  `request_method` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `request_uri` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求地址',
  `java_method` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Java方法',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `before_data` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '修改前数据',
  `after_data` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '修改后数据',
  `response_summary` text COLLATE utf8mb4_unicode_ci COMMENT '响应摘要',
  `result` tinyint DEFAULT NULL COMMENT '结果(1-成功 0-失败)',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `cost_time_ms` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `trace_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链路ID',
  `client_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端IP',
  `ip_version` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP版本',
  `country_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家编码',
  `country_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家名称',
  `province_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份名称',
  `city_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市名称',
  `district_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县名称',
  `ip_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `isp` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运营商',
  `location_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置来源',
  `location_parsed_at` datetime DEFAULT NULL COMMENT '位置解析时间',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT 'User-Agent',
  `browser` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器',
  `os` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作系统',
  `device_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `operation_time` datetime DEFAULT NULL COMMENT '操作时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operation_time` (`operation_time`),
  KEY `idx_operation_log_account_id` (`account_id`),
  KEY `idx_operation_log_module` (`module_name`),
  KEY `idx_operation_log_client_ip` (`client_ip`),
  KEY `idx_operation_log_trace_id` (`trace_id`),
  KEY `idx_operation_log_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_api_log`
--

DROP TABLE IF EXISTS `sys_api_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_api_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `account_id` bigint DEFAULT NULL COMMENT '账号ID',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `caller_app` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '调用方应用',
  `request_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接口完整地址',
  `request_method` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `request_uri` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求地址',
  `matched_pattern` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配路径',
  `java_method` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Java方法',
  `http_status` int DEFAULT NULL COMMENT 'HTTP状态码',
  `business_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务编码',
  `business_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务信息',
  `result` tinyint DEFAULT NULL COMMENT '结果(1-成功 0-失败)',
  `request_query` text COLLATE utf8mb4_unicode_ci COMMENT '请求查询参数',
  `request_headers` text COLLATE utf8mb4_unicode_ci COMMENT '请求头',
  `request_body` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '请求体',
  `response_body` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '响应体',
  `request_size` bigint DEFAULT NULL COMMENT '请求大小',
  `response_size` bigint DEFAULT NULL COMMENT '响应大小',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `exception_stack` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '异常堆栈',
  `server_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务器IP',
  `server_node` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务节点',
  `request_time` datetime DEFAULT NULL COMMENT '请求进入时间',
  `response_time` datetime DEFAULT NULL COMMENT '响应返回时间',
  `cost_time_ms` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `trace_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '链路ID',
  `client_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端IP',
  `ip_version` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP版本',
  `country_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家编码',
  `country_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家名称',
  `province_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份名称',
  `city_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市名称',
  `district_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县名称',
  `ip_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `isp` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运营商',
  `location_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置来源',
  `location_parsed_at` datetime DEFAULT NULL COMMENT '位置解析时间',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT 'User-Agent',
  `browser` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器',
  `os` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作系统',
  `device_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型',
  `api_time` datetime DEFAULT NULL COMMENT 'API时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_api_log_api_time` (`api_time`),
  KEY `idx_api_log_account_id` (`account_id`),
  KEY `idx_api_log_request_uri` (`request_uri`),
  KEY `idx_api_log_client_ip` (`client_ip`),
  KEY `idx_api_log_trace_id` (`trace_id`),
  KEY `idx_api_log_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
--
-- Table structure for table `sys_access_control`
--

DROP TABLE IF EXISTS `sys_access_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_access_control` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `general_register_enabled` tinyint DEFAULT '1' COMMENT '通用注册开关(0-关闭 1-开启)',
  `forgot_password_enabled` tinyint DEFAULT '1' COMMENT '忘记密码开关(0-关闭 1-开启)',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '已启用的普通登录方式(逗号分隔)',
  `third_party_login_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '已启用的第三方登录渠道(逗号分隔)',
  `third_party_register_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '已启用的第三方注册渠道(逗号分隔)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问控制配置表';
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
  `id` bigint NOT NULL COMMENT '主键ID',
  `scene_key` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景键(login/verify_code/captcha/send_code/mfa)',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用(0-关闭 1-开启)',
  `max_attempts` int DEFAULT NULL COMMENT '最大可重试次数',
  `recovery_seconds` int DEFAULT NULL COMMENT '恢复时间(秒)',
  `limit_by_account` tinyint DEFAULT '1' COMMENT '按账号/邮箱维度限制(0/1)',
  `limit_by_ip` tinyint DEFAULT '0' COMMENT '按IP维度限制(0/1)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_scene_key` varchar(32) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `scene_key` else NULL end)) STORED COMMENT '未删除场景键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_verification_policy_scene` (`active_scene_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证策略配置表';
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
  `id` bigint NOT NULL COMMENT '主键ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父级菜单ID',
  `menu_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单类型(menu/button)',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由名称或权限名称',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单标题或按钮标题',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `redirect` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '重定向地址',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识',
  `is_enable` tinyint DEFAULT '1' COMMENT '是否启用',
  `sort` int DEFAULT '1' COMMENT '排序',
  `keep_alive` tinyint DEFAULT '0' COMMENT '是否缓存',
  `is_hide` tinyint DEFAULT '0' COMMENT '是否隐藏菜单',
  `is_hide_tab` tinyint DEFAULT '0' COMMENT '是否隐藏标签页',
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部链接',
  `is_iframe` tinyint DEFAULT '0' COMMENT '是否内嵌',
  `show_badge` tinyint DEFAULT '0' COMMENT '是否显示徽章',
  `show_text_badge` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文本徽章',
  `fixed_tab` tinyint DEFAULT '0' COMMENT '是否固定标签页',
  `active_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '激活菜单路径',
  `is_full_page` tinyint DEFAULT '0' COMMENT '是否全屏页面',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `auth_mark` else NULL end)) STORED COMMENT '未删除权限标识',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_auth_mark` (`active_auth_mark`),
  KEY `idx_menu_parent_id` (`parent_id`),
  KEY `idx_menu_auth_mark` (`auth_mark`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `name`, `title`, `path`, `component`, `redirect`, `icon`, `auth_mark`, `is_enable`, `sort`, `keep_alive`, `is_hide`, `is_hide_tab`, `link`, `is_iframe`, `show_badge`, `show_text_badge`, `fixed_tab`, `active_path`, `is_full_page`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  -- Dashboard
  ('1900000000000000077',NULL,'menu','Dashboard','仪表盘','/dashboard','/index/index',NULL,'ri:pie-chart-line',NULL,1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000089','1900000000000000077','menu','Console','工作台','console','/dashboard/console',NULL,NULL,'dashboard:console',1,1,0,0,0,NULL,0,0,NULL,1,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System
  ('1900000000000000080',NULL,'menu','System','系统管理','/system','/index/index',NULL,'ri:user-3-line',NULL,1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Account
  ('1900000000000000016','1900000000000000080','menu','Account','账号管理','account','/system/account',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000007','1900000000000000016','button','AccountQuery','查询',NULL,NULL,NULL,NULL,'system:account:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000020','1900000000000000016','button','AccountCreate','新增',NULL,NULL,NULL,NULL,'system:account:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000060','1900000000000000016','button','AccountUpdate','编辑',NULL,NULL,NULL,NULL,'system:account:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000030','1900000000000000016','button','AccountDelete','删除',NULL,NULL,NULL,NULL,'system:account:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Role
  ('1900000000000000046','1900000000000000080','menu','Role','角色管理','role','/system/role',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000085','1900000000000000046','button','RoleQuery','查询',NULL,NULL,NULL,NULL,'system:role:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000004','1900000000000000046','button','RoleCreate','新增',NULL,NULL,NULL,NULL,'system:role:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000066','1900000000000000046','button','RoleUpdate','编辑',NULL,NULL,NULL,NULL,'system:role:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000092','1900000000000000046','button','RoleDelete','删除',NULL,NULL,NULL,NULL,'system:role:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000053','1900000000000000046','button','RolePermission','菜单权限',NULL,NULL,NULL,NULL,'system:role:permission',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > AccountCenter
  ('1900000000000000035','1900000000000000080','menu','AccountCenter','账号中心','account-center','/system/account-center',NULL,NULL,NULL,1,3,1,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Menus
  ('1900000000000000063','1900000000000000080','menu','Menus','菜单管理','menu','/system/menu',NULL,NULL,NULL,1,4,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000086','1900000000000000063','button','MenusQuery','查询',NULL,NULL,NULL,NULL,'system:menu:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000084','1900000000000000063','button','MenusCreate','新增',NULL,NULL,NULL,NULL,'system:menu:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000042','1900000000000000063','button','MenusUpdate','编辑',NULL,NULL,NULL,NULL,'system:menu:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000071','1900000000000000063','button','MenusDelete','删除',NULL,NULL,NULL,NULL,'system:menu:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > FileManage
  ('1900000000000000038','1900000000000000080','menu','FileManage','文件管理','file-manage','/system/file-manage',NULL,'',NULL,1,5,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000008','1900000000000000038','button','FileQuery','查询',NULL,NULL,NULL,NULL,'system:file:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000064','1900000000000000038','button','FileCreate','新增',NULL,NULL,NULL,NULL,'system:file:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000093','1900000000000000038','button','FileUpdate','编辑',NULL,NULL,NULL,NULL,'system:file:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000069','1900000000000000038','button','FileDelete','删除',NULL,NULL,NULL,NULL,'system:file:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000083','1900000000000000038','button','FileDownload','下载',NULL,NULL,NULL,NULL,'system:file:download',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000006','1900000000000000038','button','FileUpload','上传',NULL,NULL,NULL,NULL,'system:file:upload',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config
  ('1900000000000000029',NULL,'menu','Config','配置管理','/config','/index/index',NULL,'ri:settings-3-line',NULL,1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > FileConfig
  ('1900000000000000003','1900000000000000029','menu','FileConfig','文件配置','file-config','/config/file-config',NULL,'',NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000062','1900000000000000003','button','FileConfigQuery','查询',NULL,NULL,NULL,NULL,'system:file-config:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000039','1900000000000000003','button','FileConfigCreate','新增',NULL,NULL,NULL,NULL,'system:file-config:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000076','1900000000000000003','button','FileConfigUpdate','编辑',NULL,NULL,NULL,NULL,'system:file-config:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000044','1900000000000000003','button','FileConfigDelete','删除',NULL,NULL,NULL,NULL,'system:file-config:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > MailAccount
  ('1900000000000003001','1900000000000000029','menu','MailAccount','邮件配置','mail-account','/config/mail-account',NULL,'',NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003002','1900000000000003001','button','MailAccountQuery','查询',NULL,NULL,NULL,NULL,'system:mail-account:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003003','1900000000000003001','button','MailAccountCreate','新增',NULL,NULL,NULL,NULL,'system:mail-account:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003004','1900000000000003001','button','MailAccountUpdate','编辑',NULL,NULL,NULL,NULL,'system:mail-account:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003005','1900000000000003001','button','MailAccountDelete','删除',NULL,NULL,NULL,NULL,'system:mail-account:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003006','1900000000000003001','button','MailAccountGroup','分组管理',NULL,NULL,NULL,NULL,'system:mail-account:group',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003007','1900000000000003001','button','MailAccountChannel','发件渠道',NULL,NULL,NULL,NULL,'system:mail-account:channel',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > EmailTemplate
  ('1900000000000003101','1900000000000000029','menu','EmailTemplate','邮件模板','mail-template','/config/mail-template',NULL,'',NULL,1,3,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003102','1900000000000003101','button','EmailTemplateQuery','查询',NULL,NULL,NULL,NULL,'system:mail-template:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003103','1900000000000003101','button','EmailTemplateCreate','新增',NULL,NULL,NULL,NULL,'system:mail-template:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003104','1900000000000003101','button','EmailTemplateUpdate','编辑',NULL,NULL,NULL,NULL,'system:mail-template:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003105','1900000000000003101','button','EmailTemplateDelete','删除',NULL,NULL,NULL,NULL,'system:mail-template:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Log Management
  ('1900000000000004000',NULL,'menu','LogManagement','日志管理','/log','/index/index',NULL,'ri:file-list-3-line',NULL,1,80,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Log Management > LoginLog
  ('1900000000000004001','1900000000000004000','menu','LoginLog','登录日志','login','/system/log/login',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004010','1900000000000004001','button','LoginLogQuery','查询',NULL,NULL,NULL,NULL,'system:log:login:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004011','1900000000000004001','button','LoginLogDelete','删除',NULL,NULL,NULL,NULL,'system:log:login:delete',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004012','1900000000000004001','button','LoginLogClean','清空',NULL,NULL,NULL,NULL,'system:log:login:clean',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Log Management > OperationLog
  ('1900000000000004002','1900000000000004000','menu','OperationLog','操作日志','operation','/system/log/operation',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004020','1900000000000004002','button','OperationLogQuery','查询',NULL,NULL,NULL,NULL,'system:log:operation:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004021','1900000000000004002','button','OperationLogDelete','删除',NULL,NULL,NULL,NULL,'system:log:operation:delete',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004022','1900000000000004002','button','OperationLogClean','清空',NULL,NULL,NULL,NULL,'system:log:operation:clean',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Log Management > ApiLog
  ('1900000000000004003','1900000000000004000','menu','ApiLog','API 日志','api','/system/log/api',NULL,NULL,NULL,1,3,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004030','1900000000000004003','button','ApiLogQuery','查询',NULL,NULL,NULL,NULL,'system:log:api:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004031','1900000000000004003','button','ApiLogDelete','删除',NULL,NULL,NULL,NULL,'system:log:api:delete',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004032','1900000000000004003','button','ApiLogClean','清空',NULL,NULL,NULL,NULL,'system:log:api:clean',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings
  ('1900000000000002001',NULL,'menu','Settings','设置','/settings','/index/index',NULL,'ri:settings-4-line',NULL,1,90,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings > AccessControl
  ('1900000000000002002','1900000000000002001','menu','AccessControl','访问控制','access-control','/settings/access-control',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002004','1900000000000002002','button','AccessControlGeneralRegister','通用注册管理',NULL,NULL,NULL,NULL,'settings:access-control:general-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002005','1900000000000002002','button','AccessControlThirdPartyRegister','第三方注册管理',NULL,NULL,NULL,NULL,'settings:access-control:third-party-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002006','1900000000000002002','button','AccessControlLoginMethod','普通登录方式管理',NULL,NULL,NULL,NULL,'settings:access-control:login-method',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002007','1900000000000002002','button','AccessControlThirdPartyLogin','第三方登录方式管理',NULL,NULL,NULL,NULL,'settings:access-control:third-party-login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002008','1900000000000002002','button','AccessControlForgotPassword','忘记密码管理',NULL,NULL,NULL,NULL,'settings:access-control:forgot-password',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019010','1900000000000002001','menu','VerificationSettings','验证设置','verification-settings','/settings/verification-settings',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019013','1900000000000019010','button','VerificationSettingsLogin','登陆失败锁定管理',NULL,NULL,NULL,NULL,'settings:verification-settings:login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019014','1900000000000019010','button','VerificationSettingsVerifyCode','验证码错误管理',NULL,NULL,NULL,NULL,'settings:verification-settings:verify-code',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019015','1900000000000019010','button','VerificationSettingsCaptcha','接口限流管理',NULL,NULL,NULL,NULL,'settings:verification-settings:captcha',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019016','1900000000000019010','button','VerificationSettingsSendCode','发码频率管理',NULL,NULL,NULL,NULL,'settings:verification-settings:send-code',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019017','1900000000000019010','button','VerificationSettingsMfa','MFA 验证失败管理',NULL,NULL,NULL,NULL,'settings:verification-settings:mfa',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;

--
-- Table structure for table `sys_role_menu_permission`
--

DROP TABLE IF EXISTS `sys_role_menu_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu_permission` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单/按钮ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0-未删除 1-已删除)',
  `active_role_menu_permission` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT '未删除关联标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu_permission` (`role_id`,`menu_id`,`active_role_menu_permission`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menu_permission_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`),
  CONSTRAINT `fk_sys_role_menu_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单权限关联表';
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
  ('1900000000000002071','1900000000000000032','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> Log Management
  ('1900000000000004100','1900000000000000073','1900000000000004000','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004101','1900000000000000073','1900000000000004001','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004102','1900000000000000073','1900000000000004010','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004103','1900000000000000073','1900000000000004011','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004104','1900000000000000073','1900000000000004012','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004107','1900000000000000073','1900000000000004002','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004108','1900000000000000073','1900000000000004020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004109','1900000000000000073','1900000000000004021','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004110','1900000000000000073','1900000000000004022','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004112','1900000000000000073','1900000000000004003','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004113','1900000000000000073','1900000000000004030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004114','1900000000000000073','1900000000000004031','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004115','1900000000000000073','1900000000000004032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> Log Management
  ('1900000000000004200','1900000000000000023','1900000000000004000','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004201','1900000000000000023','1900000000000004001','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004202','1900000000000000023','1900000000000004010','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004203','1900000000000000023','1900000000000004011','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004204','1900000000000000023','1900000000000004012','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004207','1900000000000000023','1900000000000004002','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004208','1900000000000000023','1900000000000004020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004209','1900000000000000023','1900000000000004021','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004210','1900000000000000023','1900000000000004022','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004212','1900000000000000023','1900000000000004003','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004213','1900000000000000023','1900000000000004030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004214','1900000000000000023','1900000000000004031','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000004215','1900000000000000023','1900000000000004032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
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
