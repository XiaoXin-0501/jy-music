package com.jy.bo;

import lombok.Data;

@Data
public class SongStateBo {
    private Long songId;
    private int playCount;
    private int collectCount;
}
