SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_employeer`
-- ----------------------------
DROP TABLE IF EXISTS `t_employeer`;
CREATE TABLE `t_employeer` (
  `eid` int(11) NOT NULL AUTO_INCREMENT,
  `employeer_name` varchar(255) DEFAULT NULL,
  `employeer_age` int(11) DEFAULT NULL,
  `employeer_department` varchar(255) DEFAULT NULL,
  `employeer_worktype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`eid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `uid` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
-- Records of t_user
INSERT INTO `t_user` VALUES ('1', 'zhangsan');
INSERT INTO `t_user` VALUES ('2', 'lisi');


-- ----------------------------
-- Table structure for `t_address`
-- ----------------------------
DROP TABLE IF EXISTS `t_address`;
CREATE TABLE `t_address` (
  `aid` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `postCode` varchar(6) NOT NULL,
  `ownerid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`aid`),
  KEY `ownerid` (`ownerid`),
  CONSTRAINT `t_address_ibfk_1` FOREIGN KEY (`ownerid`) REFERENCES `t_user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
-- Records of t_address
INSERT INTO `t_address` VALUES ('1', 'baoding beishiqu', '071000', '1');
INSERT INTO `t_address` VALUES ('2', 'baoding xinshiqu', '071051', '1');
INSERT INTO `t_address` VALUES ('3', 'beijing', '100001', '2');

