spring-boot-start-tair
===================================
Spring boot starter MyBatis。

### 如何使用

* 在Spring Boot项目的pom.xml中添加以下依赖:

          <dependency>
                     <groupId>com.mvnsearch.spring.boot</groupId>
                     <artifactId>spring-boot-starter-mybatis</artifactId>
                     <version>1.0.0-SNAPSHOT</version>
          </dependency>

* 在Spring Boot的application.properties文件中添加tair对应的Uri，如下:
                    
          spring.mybatis.config=/mybatis-config.xml

* 创建mapper对应的factory bean,代码如下: 

          @Component
          public class CityMapperFactoryBean extends MapperFactoryBean<CityMapper> {
          
              public CityMapperFactoryBean() {
                  setMapperInterface(CityMapper.class);
              }
          
              @Autowired
              public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
                  super.setSqlSessionFactory(sqlSessionFactory);
              }
          
              public Class<CityMapper> getObjectType() {
                  return CityMapper.class;
              }
          
          }
          
* 接下来在你的代码中直接使用对应的Mapper就可以啦: 
        
            @Autowired
            private CityMapper cityMapper;
            .....

### spring-boot-start-mybatis提供的服务

* org.apache.ibatis.session.SqlSessionFactory: SQL Session Factory
* org.mybatis.spring.SqlSessionTemplate: SQL Session Template

### mybatis endpoint
提供mybatis运行期的参数:

1. 所有的SQL语句
2. SQL语句运行期metrics

### 参考文档

* Mybatis文档: http://blog.mybatis.org/p/products.html
