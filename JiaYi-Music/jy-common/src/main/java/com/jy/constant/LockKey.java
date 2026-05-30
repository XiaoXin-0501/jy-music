package com.jy.constant;

import lombok.Getter;

@Getter
public enum LockKey {
    CATEGORY_LOCK("category_lock");

    private final String key;

    LockKey(String key) {
        this.key = key;
    }
}
