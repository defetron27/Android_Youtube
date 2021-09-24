package com.max.def.youtubeapp.Model;

public class VideoModel
{
    private String video_url;
    private String thumb_url;

    public VideoModel(String video_url, String thumb_url) {
        this.video_url = video_url;
        this.thumb_url = thumb_url;
    }

    public VideoModel() {
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }
}
