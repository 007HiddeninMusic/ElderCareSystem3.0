package com.eldercare.dao;

import com.eldercare.model.ServiceRequest;

import java.util.List;

/**
 * 服务申请DAO接口：定义服务申请的数据库操作规范
 */
public interface ServiceRequestDao {
    /**
     * 向数据库插入一条服务申请
     * @param request 服务申请实体（包含关联老人ID、服务类型、申请内容等）
     */
    void insertServiceRequest(ServiceRequest request);

    /**
     * 根据老人ID查询其所有服务申请（按申请时间倒序）
     * @param elderId 关联的老人唯一ID
     * @return 服务申请列表（无数据时返回空列表）
     */
    List<ServiceRequest> selectRequestsByElderId(String elderId);

    /**
     * 更新服务申请的状态（如“待处理”→“已完成”）
     * @param requestId 服务申请唯一ID
     * @param newStatus 新状态（仅允许“待处理”“已完成”“已取消”）
     * @return 影响的行数（1：更新成功；0：无此申请；-1：更新失败）
     */
    int updateRequestStatus(String requestId, String newStatus);

    /**
     * 根据老人ID批量删除服务申请（关联老人删除时调用）
     * @param elderId 老人唯一ID
     * @return 影响的行数（删除的申请总数；-1：删除失败）
     */
    int deleteRequestsByElderId(String elderId);
}