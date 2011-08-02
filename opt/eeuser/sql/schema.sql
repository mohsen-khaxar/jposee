CREATE TABLE `eeuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nick` varchar(32) NOT NULL,
  `password` varchar(40) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `active` char(1) DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nick` (`nick`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
