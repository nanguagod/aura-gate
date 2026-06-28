package com.auragate.ai.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * PGVector 向量数据库独立数据源配置
 * <p>
 * 与 RBAC 模块的 MySQL 数据源隔离，避免配置冲突
 */
@Configuration
public class PgVectorDataSourceConfig {

    @Value("${spring.datasource.pgvector.url}")
    private String url;

    @Value("${spring.datasource.pgvector.username}")
    private String username;

    @Value("${spring.datasource.pgvector.password}")
    private String password;

    @Value("${spring.datasource.pgvector.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource pgVectorDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("pgvector-pool");
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public VectorStore pgVectorVectorStore(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate,
                                           EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .build();
    }
}
