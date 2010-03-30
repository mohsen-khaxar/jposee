CREATE TABLE  `syslog` (
  `id` bigint(20) NOT NULL auto_increment,
  `date` datetime default NULL,
  `deleted` tinyint(1) default NULL,
  `source` varchar(32) default NULL,
  `type` varchar(32) default NULL,
  `severity` int(11) default NULL,
  `summary` varchar(255) default NULL,
  `detail` text,
  `trace` text,
  `status` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKCB1C555729C01CEC` (`status`),
  CONSTRAINT `FKCB1C555729C01CEC` FOREIGN KEY (`status`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;