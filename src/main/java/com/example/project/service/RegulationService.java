package com.example.project.service;

import com.example.project.entity.Regulation;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RegulationService {
    int updateByPrimaryKey(Regulation record);
    Regulation selectByPrimaryKey(Integer reid);
}
