package com.ssafy.backend.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(value = {"com.ssafy.backend.user.model.mapper"})
public class DagakDatabaseConfig {

    @Bean(name="dagakDataSource")
    @ConfigurationProperties(prefix="spring.dagak.datasource")
    public DataSource dagakDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name="dagakSqlSessionFactory")
    public SqlSessionFactory myTripSessionFactory(@Qualifier("dagakDataSource") DataSource db1DataSource, ApplicationContext applicationContext) throws Exception{
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(db1DataSource);
        sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mybatis/mappers/*.xml")); //쿼리작성용 mapper.xml위치 설정.
        return sessionFactory.getObject();
    }

    //	@Primary
    @Bean(name="dagakSqlSessionTemplate")
    public SqlSessionTemplate myTripSqlSessionTemplate(@Qualifier("dagakSqlSessionFactory")SqlSessionFactory myTripSqlSessionFactory) throws Exception{
        return new SqlSessionTemplate(myTripSqlSessionFactory);
    }

    //	@Primary
    @Bean(name = "myTriptransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("dagakDataSource") DataSource db1DataSource) {
        return new DataSourceTransactionManager(db1DataSource);
    }


}
