package org.mvnsearch.spring.boot.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * mybatis auto mapper configuration
 *
 * @author linux_china
 */
@Configuration
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class MybatisMapperConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnProperty(value = "spring.mybatis.scan-base-package")
    @ConditionalOnMissingBean(MapperScannerConfigurer.class)
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scanner = new MapperScannerConfigurer();
        Environment env = applicationContext.getEnvironment();
        String scanBasePackage = env.getProperty("spring.mybatis.scan-base-package");
        scanner.setBasePackage(scanBasePackage);
        scanner.setAnnotationClass(Mapper.class);
        return scanner;
    }
}
