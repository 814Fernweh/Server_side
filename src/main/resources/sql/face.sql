SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for face
-- ----------------------------
DROP TABLE IF EXISTS `face`;
CREATE TABLE `face`
(
    `fid` int(11) NOT NULL AUTO_INCREMENT,
--   员工工号 外键
    `e_id`  int(11),
    foreign key (e_id) references employee (eid),
    `feature` Blob DEFAULT NULL,
    PRIMARY KEY (`fid`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

