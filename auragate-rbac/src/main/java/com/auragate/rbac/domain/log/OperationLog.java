package com.auragate.rbac.domain.log;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 操作日志 ES Document
 */
@Data
@Document(indexName = "auragate_operation_log")
public class OperationLog {

    @Id
    private String id;

    /** 模块名称 */
    @Field(type = FieldType.Keyword)
    private String module;

    /** 操作类型 */
    @Field(type = FieldType.Keyword)
    private String operationType;

    /** 操作描述 */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    /** 请求URL */
    @Field(type = FieldType.Keyword)
    private String requestUrl;

    /** HTTP请求方式 */
    @Field(type = FieldType.Keyword)
    private String requestMethod;

    /** 控制器方法 */
    @Field(type = FieldType.Keyword)
    private String method;

    /** 请求参数 */
    @Field(type = FieldType.Text)
    private String requestParams;

    /** 请求IP */
    @Field(type = FieldType.Keyword)
    private String ip;

    /** 用户代理 */
    @Field(type = FieldType.Keyword)
    private String userAgent;

    /** 操作人ID */
    @Field(type = FieldType.Long)
    private Long operatorId;

    /** 操作人用户名 */
    @Field(type = FieldType.Keyword)
    private String operatorName;

    /** 执行状态（0正常 1异常） */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 错误消息 */
    @Field(type = FieldType.Text)
    private String errorMsg;

    /** 执行耗时（ms） */
    @Field(type = FieldType.Long)
    private Long costTime;

    /** 操作时间 */
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private String operateTime;
}
