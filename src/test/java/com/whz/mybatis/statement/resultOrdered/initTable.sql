DROP TABLE IF EXISTS `composite_id_test_table`;
CREATE TABLE `composite_id_test_table` (
  `key1` varchar(32) NOT NULL,
  `key2` varchar(32) NOT NULL,
  `key3` varchar(32) NOT NULL,
  `data` varchar(32) default '',
  PRIMARY KEY (`key1`, `key2`, key3)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

insert composite_id_test_table values ('1', '1', '1', 'test1');
insert composite_id_test_table values ('2', '2', '2', 'test2');
insert composite_id_test_table values ('3', '3', '3', 'test3');


