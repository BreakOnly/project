package com.jrmf.domain;

import java.util.List;

public class ChannelAreas {

    private String code;

    private String name;

    private List<ChannelAreas> children;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChannelAreas> getChildren() {
        return children;
    }

    public void setChildren(List<ChannelAreas> children) {
        this.children = children;
    }
}