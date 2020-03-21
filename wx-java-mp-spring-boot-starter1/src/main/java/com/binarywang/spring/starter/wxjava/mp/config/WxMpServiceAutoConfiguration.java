package com.binarywang.spring.starter.wxjava.mp.config;

import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号相关服务自动注册.
 *
 * @author someone
 */
@Configuration
@RequiredArgsConstructor
public class WxMpServiceAutoConfiguration {
  private final WxMpProperties properties;
  @Autowired
  private ApplicationContext ctx;

  @Bean
  @ConditionalOnMissingBean
  public WxMpService wxMpService() {
    // 代码里 getConfigs()处报错的同学，请注意仔细阅读项目说明，你的IDE需要引入lombok插件！！！！
    final List<WxMpProperties.MpConfig> configs = this.properties.getConfigs();

    if (configs == null) {
      throw new RuntimeException("必须添加相关配置（wx.mp.configs），注意别配错了！");
    }

    WxMpService service = new WxMpServiceImpl();
    Map<String, WxMpConfigStorage> configStorages = new HashMap<>();
    for (WxMpProperties.MpConfig config : configs) {
      WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
      configStorage.setAppId(config.getAppId());
      configStorage.setSecret(config.getSecret());
      configStorage.setToken(config.getToken());
      configStorage.setAesKey(config.getAesKey());
      configStorages.put(config.getAppId(), configStorage);
    }
    service.setMultiConfigStorages(configStorages);
    return service;
  }

  @ConditionalOnBean(WxMpService.class)
  public Object registerWxMpSubService(WxMpService wxMpService) {
    ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) ctx.getAutowireCapableBeanFactory();
    factory.registerSingleton("wxMpKefuService", wxMpService.getKefuService());
    factory.registerSingleton("wxMpMaterialService", wxMpService.getMaterialService());
    factory.registerSingleton("wxMpMenuService", wxMpService.getMenuService());
    factory.registerSingleton("wxMpUserService", wxMpService.getUserService());
    factory.registerSingleton("wxMpUserTagService", wxMpService.getUserTagService());
    factory.registerSingleton("wxMpQrcodeService", wxMpService.getQrcodeService());
    factory.registerSingleton("wxMpCardService", wxMpService.getCardService());
    factory.registerSingleton("wxMpDataCubeService", wxMpService.getDataCubeService());
    factory.registerSingleton("wxMpUserBlacklistService", wxMpService.getBlackListService());
    factory.registerSingleton("wxMpStoreService", wxMpService.getStoreService());
    factory.registerSingleton("wxMpTemplateMsgService", wxMpService.getTemplateMsgService());
    factory.registerSingleton("wxMpSubscribeMsgService", wxMpService.getSubscribeMsgService());
    factory.registerSingleton("wxMpDeviceService", wxMpService.getDeviceService());
    factory.registerSingleton("wxMpShakeService", wxMpService.getShakeService());
    factory.registerSingleton("wxMpMemberCardService", wxMpService.getMemberCardService());
    factory.registerSingleton("wxMpMassMessageService", wxMpService.getMassMessageService());
    return Boolean.TRUE;
  }

}
