package org.mvnsearch.spring.boot.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mybatis
 *
 * @author linux_china
 */
@ConfigurationProperties(prefix = "spring.mybatis")
public class MybatisProperties {
    /**
     * Config file path
     */
    private String config;
    /**
     * scan package for mapper class
     */
    private String scanBasePackage;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getScanBasePackage() {
        return scanBasePackage;
    }

    public void setScanBasePackage(String scanBasePackage) {
        this.scanBasePackage = scanBasePackage;
    }
}
