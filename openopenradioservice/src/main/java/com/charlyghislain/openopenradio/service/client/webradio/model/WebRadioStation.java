package com.charlyghislain.openopenradio.service.client.webradio.model;

import java.util.List;
import java.util.Map;

public class WebRadioStation {

    private transient String id;
    private List<String> Genre;
    private String Name;
    private String Image;
    private String Homepage;
    private String Country;
    private String State;
    private String Region;
    private List<String> Languages;
    private String Description;
    private String Codec;
    private Integer BitRate;
    private Integer Added;
    private Integer LastModified;
    private String StreamUri;
    private Map<String, WebRadioAlternativeStream> alternativeStreams;
    private List<String> allCodecs;
    private List<Integer> allBitrates;
    private Integer highestBitrate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getGenre() {
        return Genre;
    }

    public void setGenre(List<String> genre) {
        Genre = genre;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getHomepage() {
        return Homepage;
    }

    public void setHomepage(String homepage) {
        Homepage = homepage;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public List<String> getLanguages() {
        return Languages;
    }

    public void setLanguages(List<String> languages) {
        Languages = languages;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getCodec() {
        return Codec;
    }

    public void setCodec(String codec) {
        Codec = codec;
    }

    public Integer getBitRate() {
        return BitRate;
    }

    public void setBitRate(Integer bitRate) {
        BitRate = bitRate;
    }

    public Integer getAdded() {
        return Added;
    }

    public void setAdded(Integer added) {
        Added = added;
    }

    public Integer getLastModified() {
        return LastModified;
    }

    public void setLastModified(Integer lastModified) {
        LastModified = lastModified;
    }

    public String getStreamUri() {
        return StreamUri;
    }

    public void setStreamUri(String streamUri) {
        StreamUri = streamUri;
    }

    public Map<String, WebRadioAlternativeStream> getAlternativeStreams() {
        return alternativeStreams;
    }

    public void setAlternativeStreams(Map<String, WebRadioAlternativeStream> alternativeStreams) {
        this.alternativeStreams = alternativeStreams;
    }

    public List<String> getAllCodecs() {
        return allCodecs;
    }

    public void setAllCodecs(List<String> allCodecs) {
        this.allCodecs = allCodecs;
    }

    public List<Integer> getAllBitrates() {
        return allBitrates;
    }

    public void setAllBitrates(List<Integer> allBitrates) {
        this.allBitrates = allBitrates;
    }

    public Integer getHighestBitrate() {
        return highestBitrate;
    }

    public void setHighestBitrate(Integer highestBitrate) {
        this.highestBitrate = highestBitrate;
    }
}
