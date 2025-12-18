package com.eldercare.dao.impl;

import com.eldercare.dao.ServiceRequestDao;
import com.eldercare.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务申请DAO空实现：标记数据库操作位置
 */
public class ServiceRequestDaoImpl implements ServiceRequestDao {

    @Override
    public void insertServiceRequest(ServiceRequest request) {
        // 预留：插入服务申请的操作位置
        System.out.println("[DAO空实现] 准备插入服务申请（老人ID：" + request.getElderId() + "，类型：" + request.getServiceType() + "）");

        // 真实数据库操作模板（核心SQL）：
        // String sql = "INSERT INTO service_request (request_id, elder_id, service_type, content, request_time, status) " +
        //              "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public List<ServiceRequest> selectRequestsByElderId(String elderId) {
        // 预留：查询老人服务申请的操作位置
        System.out.println("[DAO空实现] 准备查询老人（ID：" + elderId + "）的所有服务申请");
        return new ArrayList<>();
    }

    @Override
    public int updateRequestStatus(String requestId, String newStatus) {
        // 预留：更新服务申请状态的操作位置
        System.out.println("[DAO空实现] 准备更新服务申请（ID：" + requestId + "）状态为：" + newStatus);

        // 真实数据库操作模板（核心SQL）：
        // String sql = "UPDATE service_request SET status = ? WHERE request_id = ?";
        return 0;
    }

    @Override
    public int deleteRequestsByElderId(String elderId) {
        // 预留：批量删除老人服务申请的操作位置
        System.out.println("[DAO空实现] 准备批量删除老人（ID：" + elderId + "）的所有服务申请");
        return 0;
    }
}