package com.outerspace.codecfish;

public class Utils {
    //
    // for Twitch, URL is the concatenation of:
    // URl:                             rtmp://live-mia.twitch.tv/app/
    // Primary Stream Key:              live_271676523_jCzzbLKXtd6Hw2x4fBrSgluNZ6eDrG
    //
    // You can get the primary stream key by creating a Twitch Account, open the Dashboard
    // in the left menu, under settings, open Channel. the Primary Stream Key is at the top.

    public static final String APPLICATION_NAME = "PhosphorusOxideCarbonate";
    public static final String CLIENT_ID = "7zicui9q0gpvgfmd0zjuds98x9q3l9";
    public static final String REDIRECT_API = "http://localhost:8000";
    public static final String AUTHENTICATION_RESPONSE_TYPE = "token";
    public static final String AUTHENTICATION_SCOPE = "user:read:broadcast";

    public static final String TEXT_HTML_MIMETYPE = "text/html";
    public static final String ENCODING = "base64";

    public static final String RTMP_BASE_URL = "rtmp://live-mia.twitch.tv/";
    public static final String RTMP_END_POINT = "app/";
    public static final String STREAM_KEY = "live_271676523_jCzzbLKXtd6Hw2x4fBrSgluNZ6eDrG";

    public static String getRtmpUrl(String streamKey)  {
        return RTMP_BASE_URL + RTMP_END_POINT + streamKey;
    }

    public static String getRtmpUrl() {
        return getRtmpUrl(STREAM_KEY);
    }

    public static String getTwitchAuthenticationUrl() {
        String url = "https://id.twitch.tv/oauth2/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&response_type=<response_type>&scope=<scopes>";
        return url.replace("<client_id>", CLIENT_ID)
                .replace("<redirect_uri>", REDIRECT_API)
                .replace("<response_type>", AUTHENTICATION_RESPONSE_TYPE)
                .replace("<scopes>", AUTHENTICATION_SCOPE);
    }
}


