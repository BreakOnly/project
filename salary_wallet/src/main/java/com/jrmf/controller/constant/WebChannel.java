package com.jrmf.controller.constant;

public enum WebChannel {
    A(1, "来自网页"),
    B(1, ""),
    C(1, ""),
    D(1, "");
    private Integer id;
    private String channelName;

    WebChannel(Integer id, String channelName) {
        this.id = id;
        this.channelName = channelName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public static WebChannel codeOf(Integer id) {
        for (WebChannel webChannel : values()) {
            if (webChannel.getId().equals(id)) {
                return webChannel;
            }
        }
        return null;
    }
}
