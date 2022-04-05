SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
--     员工的这些信息 都是公司输入的  不需要员工自己注册 员工能做的只有修改手机号和密码
-- 员工用手机号和密码登录 都是默认的  公司设置好的
                            `eid` int(11) NOT NULL AUTO_INCREMENT,
                            `name` varchar(50) DEFAULT NULL,
                            `age` int(4) DEFAULT NULL,
                            `gender` int(4) DEFAULT NULL,
--                             经度longitude   纬度latitude
                            `longitude` decimal(10, 7)  DEFAULT NULL,
                            `latitude`  decimal(10, 7)  DEFAULT NULL,
--              成都 经度 104.06  纬度 30.67
                            `telephone` char(11) DEFAULT NULL,
                            `pwd` char(32) DEFAULT NULL,
                            `d_id` int(11) NOT NULL,
                            foreign key(d_id) references department(did),
                            PRIMARY KEY (`eid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
