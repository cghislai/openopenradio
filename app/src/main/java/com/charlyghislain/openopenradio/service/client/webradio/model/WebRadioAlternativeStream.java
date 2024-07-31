package com.charlyghislain.openopenradio.service.client.webradio.model;

public class WebRadioAlternativeStream {
    private String StreamUri;
    private String Codec;
    private Integer Bitrate;

    public String getStreamUri() {
        return StreamUri;
    }

    public void setStreamUri(String streamUri) {
        StreamUri = streamUri;
    }

    public String getCodec() {
        return Codec;
    }

    public void setCodec(String codec) {
        Codec = codec;
    }

    public Integer getBitrate() {
        return Bitrate;
    }

    public void setBitrate(Integer bitrate) {
        Bitrate = bitrate;
    }
}
