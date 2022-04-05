package com.example.project.entity;

import java.util.Date;

public class Regulation {
    private Integer reid;

    private Date onwork;

    private Date offwork;

    public Integer getReid() {
        return reid;
    }

    public void setReid(Integer reid) {
        this.reid = reid;
    }

    public Date getOnwork() {
        return onwork;
    }

    public void setOnwork(Date onwork) {
        this.onwork = onwork;
    }

    public Date getOffwork() {
        return offwork;
    }

    public void setOffwork(Date offwork) {
        this.offwork = offwork;
    }
}