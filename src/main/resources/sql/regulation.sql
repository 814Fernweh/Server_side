SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for regulation
-- ----------------------------
DROP TABLE IF EXISTS `regulation`;
CREATE TABLE `regulation` (

--         公司管理员设置好上下班时间
                              `Reid` int(11) NOT NULL AUTO_INCREMENT,
                              `onWork` time DEFAULT NULL,
                              `offWork` time DEFAULT NULL,

                              PRIMARY KEY (`Reid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
