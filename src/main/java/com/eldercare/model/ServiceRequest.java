package com.eldercare.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 服务申请模型类
 * 功能：对应老人或家属提交的照护、咨询等服务需求，包含申请信息和状态管理
 */
public class ServiceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // 申请唯一ID，格式如SERVICE_20251201_001
    private String requestId;
    // 关联的老人ID，与Elder.elderId对应
    private String elderId;
    // 服务类型，如"日常照护""健康咨询""康复辅助"
    private String serviceType;
    // 申请内容，详细需求描述
    private String content;
    // 申请时间，默认当前时间
    private Date requestTime;
    // 状态：待处理/已完成/已取消
    private String status;

    // 无参构造方法，默认时间为当前，状态为"待处理"
    public ServiceRequest() {
        this.requestTime = new Date();
        this.status = "待处理";
    }

    // 全参构造方法
    public ServiceRequest(String requestId, String elderId, String serviceType, String content, Date requestTime, String status) {
        this.requestId = requestId;
        this.setElderId(elderId);
        this.setServiceType(serviceType);
        this.setContent(content);
        this.requestTime = (requestTime == null) ? new Date() : requestTime;
        this.setStatus(status);
    }

    // Getter和Setter方法
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            this.requestId = requestId.trim();
        } else {
            throw new IllegalArgumentException("服务申请ID不能为空");
        }
    }

    public String getElderId() {
        return elderId;
    }

    public void setElderId(String elderId) {
        if (elderId != null && !elderId.trim().isEmpty()) {
            this.elderId = elderId.trim();
        } else {
            throw new IllegalArgumentException("必须关联老人ID");
        }
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        // 服务类型非空校验
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            this.serviceType = serviceType.trim();
        } else {
            throw new IllegalArgumentException("服务类型不能为空");
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        // 申请内容长度校验（至少10字，确保需求清晰）
        if (content != null && content.trim().length() >= 10) {
            this.content = content.trim();
        } else {
            throw new IllegalArgumentException("申请内容不能少于10个字");
        }
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = (requestTime == null) ? new Date() : requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        // 状态合法性校验（仅允许3种状态）
        if (status != null && (
                "待处理".equals(status) ||
                        "已完成".equals(status) ||
                        "已取消".equals(status)
        )) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("状态必须是待处理、已完成或已取消");
        }
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "requestId='" + requestId + '\'' +
                ", elderId='" + elderId + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", requestTime=" + requestTime +
                ", status='" + status + '\'' +
                '}'; // 内容字段可能较长，不打印
    }
}