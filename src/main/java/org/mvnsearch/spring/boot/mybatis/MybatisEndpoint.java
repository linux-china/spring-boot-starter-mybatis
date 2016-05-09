package org.mvnsearch.spring.boot.mybatis;

import com.codahale.metrics.Metric;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mvnsearch.spring.boot.mybatis.metrics.MetricsInterceptor;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Mybatis endpoint
 *
 * @author linux_china
 */
public class MybatisEndpoint extends AbstractEndpoint<Map<String, Object>> {
    private SqlSessionFactory sqlSessionFactory;
    private MetricsInterceptor interceptor;
    private MybatisProperties properties;
    private List<Map<String, Object>> statements = null;

    public MybatisEndpoint(MybatisProperties properties, SqlSessionFactory sqlSessionFactory, MetricsInterceptor interceptor) {
        super("mybatis");
        this.properties = properties;
        this.sqlSessionFactory = sqlSessionFactory;
        this.interceptor = interceptor;
    }

    public String getName() {
        return "Mybatis";
    }

    public String getVersion() {
        return "1.0.1";
    }

    public List<String> getAuthors() {
        return Collections.singletonList("leijuan <jacky.chenlb@alibaba-inc.com>");
    }

    public String getDocs() {
        return "http://blog.mybatis.org/p/products.html";
    }

    public String getScm() {
        return "http://gitlab.alibaba-inc.com/spring-boot/spring-boot-starter-mybatis";
    }

    public Optional<Object> getConfig() {
        return Optional.of(properties);
    }

    public Map<String, Object> invoke() {
        Map<String, Object> info = new HashMap<>();
        info.put("config", properties.getConfig());
        info.put("statements", statements);
        info.put("metrics", interceptor.getMetrics());
        return info;
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> getMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        Map<String, Metric> temp = interceptor.getMetrics().getMetrics();
        for (Map.Entry<String, Metric> entry : temp.entrySet()) {
            String metricName = entry.getKey();
            String prefix = metricName.substring(0, metricName.lastIndexOf("."));
            String name = metricName.substring(metricName.lastIndexOf(".") + 1);
            if (!allMetrics.containsKey(prefix)) {
                allMetrics.put(prefix, new HashMap<>());
            }
            ((Map<String, Object>) allMetrics.get(prefix)).put(name, entry.getValue());
        }
        return Optional.of(allMetrics);
    }

    public Optional<Map<String, Object>> getRuntime() {
        Map<String, Object> runtime = new HashMap<>();
        //initStatements();
        runtime.put("statements", statements);
        return Optional.of(runtime);
    }

    @PostConstruct
    public void initStatements() {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        List<MappedStatement> mappedStatements = new ArrayList<>();
        Object[] objects = configuration.getMappedStatements().toArray();
        for (Object object : objects) {
            if (object instanceof MappedStatement) {
                mappedStatements.add((MappedStatement) object);
            }
        }
        this.statements = new ArrayList<>();
        Set<String> statementIds = new HashSet<>();
        for (MappedStatement mappedStatement : mappedStatements) {
            String statementId = mappedStatement.getId();
            if (!statementIds.contains(statementId) && !statementId.contains("!")) {
                Map<String, Object> statement = new HashMap<>();
                statement.put("id", statementId);
                ParameterMap parameterMap = mappedStatement.getParameterMap();
                if (parameterMap != null && parameterMap.getType() != null) {
                    statement.put("parameterMap", parameterMap.getType().getSimpleName());
                }
                List<ResultMap> resultMaps = mappedStatement.getResultMaps();
                if (!resultMaps.isEmpty()) {
                    List<String> resultMapNames = new ArrayList<>();
                    resultMaps.stream().filter(t -> !t.getType().getSimpleName().equalsIgnoreCase("void")).forEach(t -> resultMapNames.add(t.getType().getSimpleName()));
                    if (!resultMapNames.isEmpty()) {
                        statement.put("resultMaps", resultMapNames);
                    }
                }
                String sqlCommandType = mappedStatement.getSqlCommandType().toString();
                if (sqlCommandType != null) {
                    statement.put("commandType", sqlCommandType);
                    if (sqlCommandType.equalsIgnoreCase("INSERT")) {
                        KeyGenerator keygenerator = mappedStatement.getKeyGenerator();
                        if (!(keygenerator instanceof NoKeyGenerator)) {
                            statement.put("keyGenerator", keygenerator.getClass().getSimpleName());
                        }
                    }
                }
                try {
                    statement.put("sql", mappedStatement.getBoundSql(new HashMap<>()).getSql());
                } catch (Exception ignore) {
                }
                if (mappedStatement.getSqlSource() instanceof DynamicSqlSource) {
                    statement.put("dynamic", true);
                }
                statements.add(statement);
                statementIds.add(statementId);
            }
        }
    }
}
