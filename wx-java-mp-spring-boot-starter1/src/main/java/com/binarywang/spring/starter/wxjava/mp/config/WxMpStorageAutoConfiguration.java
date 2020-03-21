package com.binarywang.spring.starter.wxjava.mp.config;

import com.binarywang.spring.starter.wxjava.mp.properties.RedisProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 微信公众号存储策略自动配置.
 *
 * @author someone
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WxMpStorageAutoConfiguration {
  private final WxMpProperties properties;

  @Autowired(required = false)
  private JedisPool jedisPool;

  @Bean
  @ConditionalOnMissingBean(WxMpConfigStorage.class)
  public WxMpConfigStorage wxMpInMemoryConfigStorage() {
    log.info("========================周帆调试properties：{}",properties);
    WxMpProperties.ConfigStorage storage = properties.getConfigStorage();
    WxMpProperties.StorageType type = storage.getType();

    if (type == WxMpProperties.StorageType.redis) {
      return getWxMpInRedisConfigStorage();
    }
    return getWxMpInMemoryConfigStorage();
  }

  private WxMpDefaultConfigImpl getWxMpInMemoryConfigStorage() {
    WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
    setWxMpInfo(config);
    return config;
  }

  private WxMpRedisConfigImpl getWxMpInRedisConfigStorage() {
    JedisPool poolToUse = jedisPool;
    if (poolToUse == null) {
      poolToUse = getJedisPool();
    }
    WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(poolToUse);
    setWxMpInfo(config);
    return config;
  }

  private void setWxMpInfo(WxMpDefaultConfigImpl config) {

    if (CollectionUtils.isEmpty(properties.getConfigs())) throw new RuntimeException("必须添加相关配置（wx.mp.configs），注意别配错了！");
    config.setAppId(properties.getConfigs().get(0).getAppId());
    config.setSecret(properties.getConfigs().get(0).getSecret());
    config.setToken(properties.getConfigs().get(0).getToken());
    config.setAesKey(properties.getConfigs().get(0).getAesKey());
  }

  private JedisPool getJedisPool() {
    WxMpProperties.ConfigStorage storage = properties.getConfigStorage();
    RedisProperties redis = storage.getRedis();

    JedisPoolConfig config = new JedisPoolConfig();
    if (redis.getMaxActive() != null) {
      config.setMaxTotal(redis.getMaxActive());
    }
    if (redis.getMaxIdle() != null) {
      config.setMaxIdle(redis.getMaxIdle());
    }
    if (redis.getMaxWaitMillis() != null) {
      config.setMaxWaitMillis(redis.getMaxWaitMillis());
    }
    if (redis.getMinIdle() != null) {
      config.setMinIdle(redis.getMinIdle());
    }
    config.setTestOnBorrow(true);
    config.setTestWhileIdle(true);

    JedisPool pool = new JedisPool(config, redis.getHost(), redis.getPort(),
      redis.getTimeout(), redis.getPassword(), redis.getDatabase());
    return pool;
  }
}
