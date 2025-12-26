package com.xf.chat.config;

import java.security.Principal;

/**
 * 简单的 Principal 实现
 * 用于封装用户名
 */
public class StompPrincipal implements Principal {
    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
