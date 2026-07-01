package com.auragate.rbac.configure;

import com.auragate.common.constant.Constants;
import com.auragate.rbac.domain.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * token 验证处理
 * 作用: JWT令牌的生成、解析、验证，Redis存储管理
 * 相当于系统的"身份证制作和检查站"
 */
@Component
@Slf4j
public class TokenService {
    //令牌自定义标识: HTTP请求头中的携带的token字段名
    @Value("${token.header}")
    private String header;

    //令牌密钥 用于加密/解密token的字符串
    @Value("${token.secret}")
    private String secret;

    //令牌有效期(分钟)
    @Value("${token.expireTime}")
    private int expireTime;

    //JWT签名密钥: 将字符串secret转换成Key对象, 用于签名
    private SecretKey secretKey;

    //JSON处理器: 用于对象与JSON字符串的相互转换
    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // StringRedisTemplate: 使用纯 String 序列化，避免 Jackson Object 序列化导致 token 字符串反序列化失败
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 系统启动时初始化密钥
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建令牌
     * 功能: 根据用户的信息生成JWT token并存入Redis
     * 使用场景: 用户登录成功之后调用
     */
    public String createToken(LoginUser loginUser) {
        //1.获取当前时间戳
        long now = System.currentTimeMillis();

        //2.计算过期时间(将分钟转换为毫秒)
        long expirationTime = now + TimeUnit.MINUTES.toMillis(expireTime);

        //3.更新用户信息中的时间字段
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(expirationTime);

        //4.准备JWT的声明数据
        HashMap<String, Object> claims = new HashMap<>();

        //5.将LoginUser对象转换为Json字符串
        try {
            claims.put("user_key", objectMapper.writeValueAsString(loginUser));
        } catch (Exception e) {
            throw new RuntimeException("序列化用户信息失败", e);
        }

        //6.构建JWT token
        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(expirationTime))
                .signWith(secretKey)
                .compact();

        //7.将token存入Redis: auragate:token:{userId} -> token
        // 使用 StringRedisTemplate 存储纯字符串，避免 Jackson 序列化导致反序列化失败
        String redisKey = Constants.TOKEN_PREFIX + loginUser.getUserId();
        stringRedisTemplate.opsForValue().set(redisKey, token, expireTime, TimeUnit.MINUTES);

        return token;
    }

    /**
     * 获取用户身份信息
     * 功能: 从HTTP请求中提取token, 解析出用户信息, 并校验Redis黑白名单
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        //1.从请求头中获取token字符串
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            //2.解析JWT获取LoginUser
            Claims claims = parseToken(token);
            String userJson = claims.get("user_key", String.class);
            LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);

            //3.Redis黑名单校验 — 如果token在黑名单中则拒绝
            String blacklistKey = Constants.TOKEN_BLACKLIST_PREFIX + loginUser.getUserId();
            String blacklistedToken = stringRedisTemplate.opsForValue().get(blacklistKey);
            if (token.equals(blacklistedToken)) {
                log.info("Token已在黑名单中, userId={}", loginUser.getUserId());
                return null;
            }

            //4.Redis token一致性校验 — 比对传入token是否与Redis存储的一致
            String redisKey = Constants.TOKEN_PREFIX + loginUser.getUserId();
            String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedToken == null || !storedToken.equals(token)) {
                log.info("Token与Redis不一致或已过期, userId={}", loginUser.getUserId());
                return null;
            }

            return loginUser;
        } catch (Exception e) {
            log.info("解析用户信息异常=>{}", String.valueOf(e));
        }
        return null;
    }

    /**
     * 退出登录 — 清除Redis token并将其加入黑名单
     */
    public void logout(HttpServletRequest request) {
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            Claims claims = parseToken(token);
            String userJson = claims.get("user_key", String.class);
            LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);

            // 删除Redis中的token（使用 StringRedisTemplate）
            String redisKey = Constants.TOKEN_PREFIX + loginUser.getUserId();
            stringRedisTemplate.delete(redisKey);

            // 将token加入黑名单（TTL与token过期时间一致）
            String blacklistKey = Constants.TOKEN_BLACKLIST_PREFIX + loginUser.getUserId();
            stringRedisTemplate.opsForValue().set(blacklistKey, token, expireTime, TimeUnit.MINUTES);

            log.info("用户退出登录, userId={}", loginUser.getUserId());
        } catch (Exception e) {
            log.info("退出登录异常=>{}", String.valueOf(e));
        }
    }

    /**
     * 刷新Redis中token的过期时间
     */
    public void refreshToken(LoginUser loginUser) {
        String redisKey = Constants.TOKEN_PREFIX + loginUser.getUserId();
        stringRedisTemplate.expire(redisKey, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 从HTTP请求中提取token（支持 Header 和 URL query param）
     */
    public String getToken(HttpServletRequest request) {
        // 1. 先从 Authorization Header 读取
        String token = request.getHeader(header);
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        if (StringUtils.hasText(token)) {
            return token;
        }
        // 2. WebSocket 请求通过 URL query param 传递 token
        token = request.getParameter("token");
        return token;
    }

    /**
     * 解析字符串, 获取声明数据
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌有效期
     */
    public void verifyToken(LoginUser loginUser) {
        Long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= 0) {
            throw new RuntimeException("Token已过期");
        }
    }
}
