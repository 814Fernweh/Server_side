SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for record
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
                          `rid` int(11) NOT NULL AUTO_INCREMENT,
--                           员工工号
                          `e_id` int(11),
                          foreign key(e_id) references employee(eid),

                          `e_name` varchar(50) ,

--                          所在部门编号
                          `d_id` int(11) NOT NULL,
                          foreign key(d_id) references department(did),
--                             考勤日期  格式改了 需要更新这个表！！！
                          `checkinDate` char(50) DEFAULT NULL,
    --     考勤结果分上下午结果
--  查询的时候再判断  1=上班前打卡（上午正常） 2=下班后打卡（下午正常）
--       3=上班时间之后打卡（迟到）  4=下班之前打开（早退）
--                             上班时间
                          `arriveTime` timestamp DEFAULT NULL,
--                           下班时间
--                           上班打卡，只保留第一次打卡时间； 再次打卡时不更新；
--                             下班打卡，多次打卡时更新为最后一次。(如果考虑加班情况 可能就会有重复下班打卡情况）
                          `leaveTime` timestamp DEFAULT NULL,

--                             考勤结果
                          `arriveResult` char(20) DEFAULT NULL,
                          `leaveResult` char(20) DEFAULT NULL,

                          `result` char(20) DEFAULT NULL,

                          PRIMARY KEY (`rid`)

) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
