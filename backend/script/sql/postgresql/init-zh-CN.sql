-- 业务主键统一存储为 BIGINT；velox.id.snowflake.enabled=true 使用雪花 ID，false 使用数据库序列。
DROP TABLE IF EXISTS sys_role_menu_permission CASCADE;
DROP TABLE IF EXISTS sys_menu CASCADE;
DROP TABLE IF EXISTS sys_access_control CASCADE;
DROP TABLE IF EXISTS sys_verification_policy CASCADE;
DROP TABLE IF EXISTS sys_account_security CASCADE;
DROP TABLE IF EXISTS sys_account_session CASCADE;
DROP TABLE IF EXISTS sys_account_role CASCADE;
DROP TABLE IF EXISTS sys_profile CASCADE;
DROP TABLE IF EXISTS sys_account CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_file_content CASCADE;
DROP TABLE IF EXISTS sys_file CASCADE;
DROP TABLE IF EXISTS sys_file_config CASCADE;
DROP TABLE IF EXISTS sys_mail_account CASCADE;
DROP TABLE IF EXISTS sys_mail_template CASCADE;
DROP TABLE IF EXISTS sys_mail_group CASCADE;
DROP TABLE IF EXISTS sys_mail_channel CASCADE;
DROP TABLE IF EXISTS sys_id_sequence CASCADE;

CREATE TABLE sys_id_sequence (
  business_type varchar(64) PRIMARY KEY,
  current_value bigint NOT NULL DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_id_sequence (business_type, current_value) VALUES
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

CREATE TABLE sys_file_config (
  id bigint PRIMARY KEY,
  name varchar(100) NOT NULL,
  storage integer NOT NULL,
  remark varchar(500),
  master boolean DEFAULT false,
  config text,
  enabled smallint DEFAULT 1,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_sys_file_config_storage ON sys_file_config (storage);
CREATE INDEX idx_sys_file_config_master ON sys_file_config (master);

INSERT INTO sys_file_config (id, name, storage, remark, master, config, enabled, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000090','数据库存储',1,'数据库存储文件',false,'{"domain": "http://127.0.0.1:3000"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000040','本地存储',10,'本地磁盘存储',true,'{"domain": "http://127.0.0.1:3000", "basePath": "/tmp/uploads"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000041','S3云存储',20,'S3/OSS云存储(当前仅测试阿里云，理论上 S3 类型都支持，若遇到问题请提出您宝贵的 issue，这将有利于我们完善和优化)',false,'{"bucket": "bucket", "endpoint": "oss-cn-beijing.aliyuncs.com", "accessKey": "Your Access Key", "accessSecret": "Your Access Secret", "enablePublicAccess": true, "enablePathStyleAccess": false}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000011','FTP存储',11,'FTP 文件存储示例',false,'{"host": "127.0.0.1", "port": 21, "username": "ftp-user", "password": "ftp-password", "mode": "Passive", "basePath": "/uploads", "domain": "http://127.0.0.1:3000"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000072','SFTP存储',12,'SFTP 文件存储示例',false,'{"host": "127.0.0.1", "port": 22, "username": "sftp-user", "password": "sftp-password", "basePath": "/uploads", "domain": "http://127.0.0.1:3000"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_mail_group (
  id bigint PRIMARY KEY,
  name varchar(100) NOT NULL,
  active smallint DEFAULT 1,
  sort integer DEFAULT 1,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

INSERT INTO sys_mail_group (id, name, active, sort, remark, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000210','默认分组',1,1,'系统默认发件分组','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_mail_channel (
  id bigint PRIMARY KEY,
  name varchar(100) NOT NULL,
  protocol varchar(20) NOT NULL,
  active smallint DEFAULT 1,
  sort integer DEFAULT 1,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

INSERT INTO sys_mail_channel (id, name, protocol, active, sort, remark, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000211','SMTP','SMTP',1,1,'SMTP 明文/STARTTLS 发件协议','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000212','SMTPS','SMTPS',1,2,'SMTPS 加密发件协议','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_mail_account (
  id bigint PRIMARY KEY,
  name varchar(100) NOT NULL,
  group_id bigint,
  channel_id bigint,
  username varchar(255) NOT NULL,
  password varchar(500),
  from_address varchar(255),
  from_name varchar(100),
  host varchar(255),
  port integer,
  ssl_enabled smallint,
  starttls smallint,
  weight integer DEFAULT 100,
  fail_threshold integer DEFAULT 3,
  retry_interval integer DEFAULT 300,
  max_unavailable integer DEFAULT 5,
  health_status smallint DEFAULT 0,
  usage_count bigint DEFAULT 0,
  fail_count integer DEFAULT 0,
  unavailable_count integer DEFAULT 0,
  next_retry_time timestamp,
  last_used_time timestamp,
  remark varchar(500),
  enabled smallint DEFAULT 1,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_sys_mail_account_group_id ON sys_mail_account (group_id);
CREATE INDEX idx_sys_mail_account_channel_id ON sys_mail_account (channel_id);
CREATE INDEX idx_sys_mail_account_enabled ON sys_mail_account (enabled);
CREATE INDEX idx_sys_mail_account_health_status ON sys_mail_account (health_status);

CREATE TABLE sys_mail_template (
  id bigint PRIMARY KEY,
  name varchar(100) NOT NULL,
  send_type varchar(64) NOT NULL,
  template_type varchar(16) NOT NULL DEFAULT 'CUSTOM',
  subject varchar(255),
  content text,
  enabled smallint DEFAULT 1,
  sort integer DEFAULT 1,
  remark varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE UNIQUE INDEX uk_sys_mail_template_name_active ON sys_mail_template (name) WHERE deleted = 0 AND name IS NOT NULL;
CREATE INDEX idx_sys_mail_template_send_type ON sys_mail_template (send_type);
CREATE INDEX idx_sys_mail_template_template_type ON sys_mail_template (template_type);
CREATE INDEX idx_sys_mail_template_send_type_enabled ON sys_mail_template (send_type, enabled);
CREATE INDEX idx_sys_mail_template_enabled ON sys_mail_template (enabled);

INSERT INTO sys_mail_template
  (id, name, send_type, template_type, subject, content, enabled, sort, remark, create_time, update_time, create_by, update_by, deleted)
VALUES
  ('1900000000000003110','密码重置验证码','AUTH_RESET_PASSWORD_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行忘记密码操作。<br /><br />本次密码重置验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件。</p></div>',1,1,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003111','登录验证码','AUTH_LOGIN_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在通过邮箱验证码登录。<br /><br />本次登录验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,2,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003112','登录二次验证码','AUTH_LOGIN_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在通过邮箱二次验证完成登录。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,3,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003113','邮箱解绑验证码','ACCOUNT_EMAIL_UNBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行邮箱解绑操作。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并尽快修改密码。</p></div>',1,4,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003114','邮箱换绑身份验证','ACCOUNT_EMAIL_REBIND_PROOF_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在验证当前身份，以继续执行邮箱换绑。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并尽快修改密码。</p></div>',1,5,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003115','邮箱换绑验证码','ACCOUNT_EMAIL_REBIND_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在执行邮箱换绑操作。<br /><br />本次换绑验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请忽略本邮件并修改密码。</p></div>',1,6,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000003116','二次验证码','ACCOUNT_MFA_CODE','SYSTEM','Velox Web Pro','<div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333333;line-height:1.6;"><p style="margin:0;">您好，{{username}}：<br /><br />您正在使用邮箱二次验证码。<br /><br />本次验证码为：{{code}}<br /><br />验证码 {{validityMinutes}} 分钟内有效，请勿泄露给他人。<br /><br />如果这不是您的操作，请尽快修改密码。</p></div>',1,7,'系统内置模板','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_file (
  id bigint PRIMARY KEY,
  config_id bigint NOT NULL,
  name varchar(255) NOT NULL,
  path varchar(500) NOT NULL,
  url varchar(500),
  type varchar(100),
  size bigint DEFAULT 0,
  upload_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_sys_file_config_id ON sys_file (config_id);
CREATE INDEX idx_sys_file_create_by ON sys_file (create_by);

CREATE TABLE sys_file_content (
  id bigint PRIMARY KEY,
  config_id bigint NOT NULL,
  path varchar(500) NOT NULL,
  content bytea NOT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_sys_file_content_config_id ON sys_file_content (config_id);
CREATE INDEX idx_sys_file_content_path ON sys_file_content (path);

CREATE TABLE sys_role (
  id bigint PRIMARY KEY,
  role_name varchar(50) NOT NULL,
  role_code varchar(50) NOT NULL,
  description varchar(255),
  type smallint NOT NULL DEFAULT 1,
  role_level integer NOT NULL DEFAULT 0,
  enabled smallint DEFAULT 1,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_role_code ON sys_role (role_code);
CREATE UNIQUE INDEX uk_role_code_active ON sys_role (role_code) WHERE deleted = 0;

INSERT INTO sys_role (id, role_name, role_code, description, type, role_level, enabled, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000023','超级管理员','R_SUPER','拥有系统全部权限',0,3,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000073','管理员','R_ADMIN','拥有系统管理权限',0,2,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000032','普通用户','R_USER','拥有系统普通权限',0,1,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_account (
  id bigint PRIMARY KEY,
  username varchar(50) NOT NULL,
  password varchar(100) NOT NULL,
  remark varchar(255),
  status smallint DEFAULT 1,
  login_fail_count integer DEFAULT 0,
  login_fail_time timestamp,
  deletion_requested_at timestamp,
  deletion_expires_at timestamp,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_username ON sys_account (username);
CREATE UNIQUE INDEX uk_username_active ON sys_account (username) WHERE deleted = 0;

INSERT INTO sys_account (id, username, password, remark, status, login_fail_count, login_fail_time, deletion_requested_at, deletion_expires_at, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000027','Super','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','超级管理员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000087','Admin','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','管理员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000048','User','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','默认成员账号',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_profile (
  id bigint PRIMARY KEY,
  account_id bigint NOT NULL REFERENCES sys_account (id),
  nickname varchar(50),
  avatar varchar(255),
  gender smallint DEFAULT 0,
  real_name varchar(50),
  email varchar(100),
  mobile varchar(20),
  address varchar(255),
  introduction text,
  signature varchar(255),
  position varchar(50),
  company varchar(100),
  tags text,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE UNIQUE INDEX uk_account_id ON sys_profile (account_id);

INSERT INTO sys_profile (id, account_id, nickname, avatar, gender, real_name, email, mobile, address, introduction, signature, position, company, tags, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000075','1900000000000000027','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=Super',2,'不愿透露','super.public@velox.web','15800158001','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','["可爱", "很可爱", "非常可爱"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000057','1900000000000000087','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=Admin',1,'不愿透露','admin.public@velox.web','15800158002','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','["可爱", "很可爱", "非常可爱"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000014','1900000000000000048','虚拟双子','https://api.dicebear.com/7.x/avataaars/svg?seed=User',1,'不愿透露','user.public@velox.web','15800158003','不愿透露','不愿透露','不愿透露','不愿透露','不愿透露','["可爱", "很可爱", "非常可爱"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_account_role (
  id bigint PRIMARY KEY,
  account_id bigint NOT NULL REFERENCES sys_account (id),
  role_id bigint NOT NULL REFERENCES sys_role (id),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_account_role_account_id ON sys_account_role (account_id);
CREATE INDEX idx_user_role_role_id ON sys_account_role (role_id);
CREATE UNIQUE INDEX uk_account_role_active ON sys_account_role (account_id, role_id) WHERE deleted = 0;

INSERT INTO sys_account_role (id, account_id, role_id, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000021','1900000000000000027','1900000000000000023','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000012','1900000000000000087','1900000000000000073','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000024','1900000000000000048','1900000000000000032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_account_session (
  id bigint PRIMARY KEY,
  account_id bigint NOT NULL REFERENCES sys_account (id),
  token_hash varchar(64) NOT NULL,
  status smallint NOT NULL DEFAULT 1,
  login_time timestamp,
  last_active_time timestamp,
  logout_time timestamp,
  presence_expire_time timestamp,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_account_session_token_hash ON sys_account_session (token_hash);
CREATE INDEX idx_account_session_account_id ON sys_account_session (account_id);
CREATE INDEX idx_user_session_presence_expire_time ON sys_account_session (presence_expire_time);

CREATE TABLE sys_account_security (
  id bigint PRIMARY KEY,
  account_id bigint NOT NULL REFERENCES sys_account (id),
  email varchar(100),
  login_methods varchar(128) DEFAULT 'password',
  disabled_login_methods varchar(128),
  disabled_oauth_channels varchar(255),
  mfa_email_enabled smallint DEFAULT 0,
  mfa_totp_enabled smallint DEFAULT 0,
  mfa_totp_secret varchar(64),
  mfa_totp_recovery_codes varchar(1024),
  email_verified_at timestamp,
  last_password_change_at timestamp,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE UNIQUE INDEX uk_account_security_account_id ON sys_account_security (account_id);

INSERT INTO sys_account_security (id, account_id, email, login_methods, mfa_email_enabled, mfa_totp_enabled, mfa_totp_secret, mfa_totp_recovery_codes, email_verified_at, last_password_change_at, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000000126','1900000000000000027','super@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000127','1900000000000000087','admin@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000128','1900000000000000048','user@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_access_control (
  id bigint PRIMARY KEY,
  general_register_enabled smallint DEFAULT 1,
  forgot_password_enabled smallint DEFAULT 1,
  login_methods varchar(128),
  third_party_login_channels varchar(255),
  third_party_register_channels varchar(255),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

INSERT INTO sys_access_control (id, general_register_enabled, forgot_password_enabled, login_methods, third_party_login_channels, third_party_register_channels, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000002010',1,1,'password,email_code','github,linuxdo','github,linuxdo','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_verification_policy (
  id bigint PRIMARY KEY,
  scene_key varchar(32) NOT NULL,
  enabled smallint DEFAULT 1,
  max_attempts integer,
  recovery_seconds integer,
  limit_by_account smallint DEFAULT 1,
  limit_by_ip smallint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE UNIQUE INDEX uk_verification_policy_scene ON sys_verification_policy (scene_key) WHERE deleted = 0;

INSERT INTO sys_verification_policy (id, scene_key, enabled, max_attempts, recovery_seconds, limit_by_account, limit_by_ip, create_time, update_time, create_by, update_by, deleted) VALUES
  ('1900000000000019001','login',1,5,1800,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019002','verify_code',1,5,600,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019003','captcha',1,10,60,0,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019004','send_code',1,1,60,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019005','mfa',1,5,300,1,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_menu (
  id bigint PRIMARY KEY,
  parent_id bigint,
  menu_type varchar(20) NOT NULL,
  name varchar(100) NOT NULL,
  title varchar(100) NOT NULL,
  path varchar(255),
  component varchar(255),
  redirect varchar(255),
  icon varchar(100),
  auth_mark varchar(100),
  is_enable smallint DEFAULT 1,
  sort integer DEFAULT 1,
  keep_alive smallint DEFAULT 0,
  is_hide smallint DEFAULT 0,
  is_hide_tab smallint DEFAULT 0,
  link varchar(255),
  is_iframe smallint DEFAULT 0,
  show_badge smallint DEFAULT 0,
  show_text_badge varchar(50),
  fixed_tab smallint DEFAULT 0,
  active_path varchar(255),
  is_full_page smallint DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_menu_parent_id ON sys_menu (parent_id);
CREATE INDEX idx_menu_auth_mark ON sys_menu (auth_mark);
CREATE UNIQUE INDEX uk_menu_auth_mark_active ON sys_menu (auth_mark) WHERE deleted = 0 AND auth_mark IS NOT NULL;

INSERT INTO sys_menu (id, parent_id, menu_type, name, title, path, component, redirect, icon, auth_mark, is_enable, sort, keep_alive, is_hide, is_hide_tab, link, is_iframe, show_badge, show_text_badge, fixed_tab, active_path, is_full_page, create_time, update_time, create_by, update_by, deleted) VALUES
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
    -- Settings
  ('1900000000000002001',NULL,'menu','Settings','设置','/settings','/index/index',NULL,'ri:settings-4-line',NULL,1,90,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
    -- Settings > AccessControl
  ('1900000000000002002','1900000000000002001','menu','AccessControl','访问控制','access-control','/settings/access-control',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002004','1900000000000002002','button','AccessControlGeneralRegister','通用注册管理',NULL,NULL,NULL,NULL,'settings:access-control:general-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002005','1900000000000002002','button','AccessControlThirdPartyRegister','第三方注册管理',NULL,NULL,NULL,NULL,'settings:access-control:third-party-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002006','1900000000000002002','button','AccessControlLoginMethod','普通登录方式管理',NULL,NULL,NULL,NULL,'settings:access-control:login-method',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002007','1900000000000002002','button','AccessControlThirdPartyLogin','第三方登录方式管理',NULL,NULL,NULL,NULL,'settings:access-control:third-party-login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002008','1900000000000002002','button','AccessControlForgotPassword','忘记密码管理',NULL,NULL,NULL,NULL,'settings:access-control:forgot-password',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
    -- Settings > VerificationSettings
  ('1900000000000019010','1900000000000002001','menu','VerificationSettings','验证设置','verification-settings','/settings/verification-settings',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019013','1900000000000019010','button','VerificationSettingsLogin','登陆失败锁定管理',NULL,NULL,NULL,NULL,'settings:verification-settings:login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019014','1900000000000019010','button','VerificationSettingsVerifyCode','验证码错误管理',NULL,NULL,NULL,NULL,'settings:verification-settings:verify-code',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019015','1900000000000019010','button','VerificationSettingsCaptcha','接口限流管理',NULL,NULL,NULL,NULL,'settings:verification-settings:captcha',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019016','1900000000000019010','button','VerificationSettingsSendCode','发码频率管理',NULL,NULL,NULL,NULL,'settings:verification-settings:send-code',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000019017','1900000000000019010','button','VerificationSettingsMfa','MFA 验证失败管理',NULL,NULL,NULL,NULL,'settings:verification-settings:mfa',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);

CREATE TABLE sys_role_menu_permission (
  id bigint PRIMARY KEY,
  role_id bigint NOT NULL REFERENCES sys_role (id),
  menu_id bigint NOT NULL REFERENCES sys_menu (id),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_by bigint,
  update_by bigint,
  deleted smallint DEFAULT 0
);

CREATE INDEX idx_role_id ON sys_role_menu_permission (role_id);
CREATE INDEX idx_menu_id ON sys_role_menu_permission (menu_id);
CREATE UNIQUE INDEX uk_role_menu_permission_active ON sys_role_menu_permission (role_id, menu_id) WHERE deleted = 0;

INSERT INTO sys_role_menu_permission (id, role_id, menu_id, create_time, update_time, create_by, update_by, deleted) VALUES
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

