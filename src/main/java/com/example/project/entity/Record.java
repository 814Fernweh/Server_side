package com.example.project.entity;

import java.util.Date;

public class Record {
    private Integer rid;

    private Integer eId;

    private String name;

    private Integer dId;

    private String checkinDate;  // 类型从Date改成String了
    
    private Date arriveTime;

    private Date leaveTime;
//    private Integer count;

    private String arriveResult;
    private String leaveResult;
    private String result;

    public Integer getRid() {
        return rid;
    }

    public void setRid(Integer rid) {
        this.rid = rid;
    }

    public Integer geteId() {
        return eId;
    }

    public void seteId(Integer eId) {
        this.eId = eId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }


    public Integer getdId() {
        return dId;
    }

    public void setdId(Integer dId) {
        this.dId = dId;
    }

    public String getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(String checkinDate) {
        this.checkinDate = checkinDate;
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Date arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Date getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Date leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getAResult() {
        return arriveResult;
    }

    public void setAResult(String result) {
        this.arriveResult = result == null ? null : result.trim();
    }

    public String getLResult() {
        return leaveResult;
    }

    public void setLResult(String result) {
        this.leaveResult = result == null ? null : result.trim();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }
}