package com.chat.kit.myutil_token.system.util;

public class JwtProperties {
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 5 * 60;               // 액세스 토큰 5분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 6 * 60 * 60;     // 리프레시 토큰 6시간
    public static final long REMEMBER_ME_REFRESH_TOKEN_EXPIRE_TIME = 30L * 24 * 60 * 60; //기억하기 리프레시 토큰 30일

    public static final Integer COOKIE_EXPIRE_TIME = 7 * 24 * 60 * 60; // 쿠키 7일

    public static final String USER_NAME = "username";
    public static final String NICK_NAME = "nickname";


    public static final Integer BEGIN_INDEX = 7;
    public static final String CATEGORY = "category";
    public static final String ALGORITHM = "alg";
    public static final String HS256 = "HS256";
    public static final String AUTHORITIES = "authorities";
    public static final String ROLE = "role";
    public static final String MEMBER_ID = "memberId";

    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String LARGE_REFRESH_TOKEN = "largeRefreshToken";

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

}
