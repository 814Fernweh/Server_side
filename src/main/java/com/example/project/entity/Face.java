package com.example.project.entity;

import com.mysql.cj.jdbc.Blob;

public class Face {
    private Integer fid;

    private Integer eId;

//    private Blob feature;
    private byte[] feature;

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public Integer geteId() {
        return eId;
    }

    public void seteId(Integer eId) {
        this.eId = eId;
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }
}