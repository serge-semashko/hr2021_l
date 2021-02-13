copyAccStory.cfg

[parameters]
title=*ЛОГ=>архив
debug=off

[end]

[report]
$INCLUDE dat/common.dat[head]
<style type="text/css">
$INCLUDE free/main_css_noDB.cfg[report]
</style>


$GET_DATA [getArchTables]
$GET_DATA [move to achive] ??cop=Y&!TABLE_EXISTS

<body bgcolor=F4FFFA style="padding:20px;">
<table border=0 cellpadding=0 cellspacing=0 width=98%>
<tr><td width=90%><h3>СЭД - лог запросов - сброс в архив</h3></td>
<td align=right nowrap=true>
</td><td align=right nowrap=true>
<a class=info href="/adb/adb">
Главная</a>
</td></tr></table>
<form name="theForm" method="POST" enctype="multipart/form-data">
<input type=hidden name="c" value="#c#">
<input type=hidden name="cop" value="Y">

<table border=0 cellpadding=8 style="border:solid 1px gray; margin:10px 0 0 200px; background-color:white;">
<tr><td style="text-align:right;">Записей в a_req_log: </td><td>#TOT_NUM_RECS#</td></tr>
<tr><td style="text-align:right;">Таблицы:</td><td>#TNAMES#</select></td></tr>
<tr><td colspan=2 align=center>
<input type="submit" value=" Сбросить в архив #ARCH_TABLE#"
disabled ??TABLE_EXISTS
></td></tr>
</table>
#ERROR#
</body>
</html>
[end]




****************************************************************************
****************************************************************************
****************************************************************************

[getArchTables]
select concat(table_name,'<br>') as TNAMES
from information_schema.tables
where table_schema='dms'
and table_name like 'a_req_log%'
order by table_schema
;
select count(*) as TOT_NUM_RECS from a_req_log
;
select concat('a_req_log_', DATE_FORMAT(now(),'%Y%m%d')) as ARCH_TABLE from dual
;
select table_name as TABLE_EXISTS
from information_schema.tables
where table_schema='dms' and table_name='#ARCH_TABLE#'
;
[end]

[move to achive]
RENAME TABLE `a_req_log` TO `#ARCH_TABLE#`
;
create table a_req_log as select * from #ARCH_TABLE# where 1=2
;
CREATE INDEX IDX_a_req_log_DAT ON a_req_log (DAT)
;
CREATE INDEX IDX_a_req_log_USER ON a_req_log (USER_ID)
[end]

create table #ARCH_TABLE# as select * from a_req_log
;
truncate table a_req_log ??!ERROR
;
CREATE INDEX IDX_#ARCH_TABLE#_DAT ON #ARCH_TABLE# (DAT)
;
CREATE INDEX IDX_#ARCH_TABLE#_USER ON #ARCH_TABLE# (USER_ID)
;
[end]

RENAME TABLE a_req_log TO a_req_log_20201001

-- --------------------------------------------------------
-- Хост:                         159.93.33.215
-- Версия сервера:               8.0.16 - MySQL Community Server - GPL
-- Операционная система:         Linux
-- HeidiSQL Версия:              10.2.0.5599
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Дамп структуры для таблица dms. a_req_log_20201001
CREATE TABLE IF NOT EXISTS a_req_log (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT 'ID пользователя',
  `real_user_id` int(11) DEFAULT NULL COMMENT 'реальный ID пользователя',
  `request_type` varchar(255) DEFAULT NULL COMMENT 'Тип запроса: http, AJAX, внутренний',
  `queryLabel` varchar(31) DEFAULT NULL COMMENT 'ID запроса',
  `c` varchar(63) NOT NULL DEFAULT '?' COMMENT 'параметр запроса "c"',
  `request_name` varchar(255) DEFAULT NULL COMMENT 'название запроса',
  `query` text COMMENT 'http запрос',
  `doc_id` int(11) NOT NULL DEFAULT '0',
  `cookies` varchar(1024) DEFAULT NULL COMMENT 'cookies клиента',
  `err` varchar(2048) DEFAULT NULL COMMENT 'зарегистрированная ошибка',
  `dat` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'дата-время запроса',
  `IP` varchar(255) DEFAULT NULL COMMENT 'IP клиента',
  `user_agent` varchar(512) DEFAULT NULL COMMENT 'браузер пользователя',
  `ref` varchar(255) DEFAULT NULL COMMENT 'referer',
  `sess_id` varchar(255) DEFAULT NULL COMMENT 'ID сессии клиента',
  `sess` varchar(32) DEFAULT NULL COMMENT 'hash сессии клиента',
  `did` varchar(255) DEFAULT NULL,
  `tim` int(11) unsigned DEFAULT NULL COMMENT 'время выполнения запроса (мс)',
  `fixed` tinyint(1) unsigned DEFAULT NULL COMMENT 'флаг "ошибка исправлена"',
  `fix_dat` datetime DEFAULT NULL COMMENT 'дата-время исправления ошибки',
  `comments` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `c` (`c`),
  KEY `dat` (`dat`),
  KEY `doc_id` (`doc_id`),
  KEY `fixed` (`fixed`),
  KEY `ix_req_log_user_id` (`user_id`),
  KEY `tim` (`tim`),
  KEY `err` (`err`(255))
) ENGINE=MyISAM AUTO_INCREMENT=348537 DEFAULT CHARSET=utf8 COMMENT='Лог http-запросов';

-- Экспортируемые данные не выделены.

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

