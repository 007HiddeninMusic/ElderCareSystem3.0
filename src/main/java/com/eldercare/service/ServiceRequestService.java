package com.eldercare.service;

import com.eldercare.dao.ServiceRequestDao;
import com.eldercare.dao.impl.ServiceRequestDaoImpl;
import com.eldercare.model.Elder;
import com.eldercare.model.ServiceRequest;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务申请业务逻辑类：处理申请提交、状态更新、关联老人
 */
public class ServiceRequestService {
    private static volatile ServiceRequestService INSTANCE;
    private static final String SERVICE_DATA_KEY = "service_requests";
    private ElderService elderService;
    private final ServiceRequestDao requestDao = new ServiceRequestDaoImpl();

    protected ServiceRequestService() {
        this.elderService = ElderService.getInstance();
    }

    public static ServiceRequestService getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceRequestService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceRequestService();
                }
            }
        }
        return INSTANCE;
    }

    private ElderService getElderService() {
        if (elderService == null) {
            elderService = ElderService.getInstance();
        }
        return elderService;
    }

    /**
     * 提交服务申请
     * @param request 服务申请对象（需包含老人ID、服务类型、申请内容）
     */
    public void submitRequest(ServiceRequest request) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(request.getElderId(), "关联老人ID");
        InputValidator.validateNotEmpty(request.getServiceType(), "服务类型");
        InputValidator.validateNotEmpty(request.getContent(), "申请内容");

        // 确保elderService已初始化
        if (elderService == null) {
            elderService = ElderService.getInstance();
            if (elderService == null) {
                throw new RuntimeException("系统服务初始化失败，请重启系统");
            }
        }

        // 2. 校验老人是否存在
        Elder existElder = elderService.getElderById(request.getElderId());
        if (existElder == null) {
            throw new IllegalArgumentException("关联老人不存在（ID：" + request.getElderId() + "），无法提交服务申请");
        }

        // 3. 补全申请信息
        request.setRequestId(IdGenerator.generateServiceRequestId());
        if (request.getRequestTime() == null) {
            request.setRequestTime(new Date());
        }
        if (request.getStatus() == null || !("待处理".equals(request.getStatus()) || "已完成".equals(request.getStatus()) || "已取消".equals(request.getStatus()))) {
            request.setStatus("待处理"); // 默认状态：待处理
        }

        // 4. 保存申请
        List<ServiceRequest> requestList = getRequestList();
        requestList.add(request);
        DataStorageUtil.saveData(SERVICE_DATA_KEY, requestList);
        System.out.println("[ServiceRequestService] 服务申请提交成功：老人" + existElder.getName() + "（类型：" + request.getServiceType() + "）");

        // 5. 预留数据库操作：插入服务申请到数据库
        requestDao.insertServiceRequest(request);
    }

    /**
     * 更新服务申请状态（如待处理→已完成）
     * @param requestId 申请ID
     * @param newStatus 新状态（仅允许：待处理/已完成/已取消）
     * @return true：更新成功；false：申请不存在
     */
    public boolean updateRequestStatus(String requestId, String newStatus) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(requestId, "申请ID");
        InputValidator.validateNotEmpty(newStatus, "新状态");
        // 校验状态合法性（在model层已校验，此处二次确认）
        if (!("待处理".equals(newStatus) || "已完成".equals(newStatus) || "已取消".equals(newStatus))) {
            throw new IllegalArgumentException("状态非法！仅允许：待处理/已完成/已取消");
        }

        List<ServiceRequest> requestList = getRequestList();
        // 查找申请并更新状态
        for (ServiceRequest request : requestList) {
            if (request.getRequestId().equals(requestId.trim())) {
                String oldStatus = request.getStatus();
                request.setStatus(newStatus);
                DataStorageUtil.saveData(SERVICE_DATA_KEY, requestList);
                System.out.println("[ServiceRequestService] 申请状态更新：" + requestId + "（" + oldStatus + "→" + newStatus + "）");
                return true;
            }
        }
        return false; // 申请不存在
    }

    /**
     * 查询指定老人的服务申请
     * @param elderId 老人ID
     * @return 服务申请列表
     */
    public List<ServiceRequest> getRequestsByElderId(String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(elderId, "老人ID");
        List<ServiceRequest> requestList = getRequestList();
        return requestList.stream()
                .filter(request -> request.getElderId().equals(elderId.trim()))
                .sorted((r1, r2) -> r2.getRequestTime().compareTo(r1.getRequestTime())) // 时间倒序
                .collect(Collectors.toList());
    }

    /**
     * 内部方法：根据老人ID删除关联服务申请（供ElderService调用）
     */
    void deleteRequestsByElderId(String elderId) throws IOException, ClassNotFoundException {
        List<ServiceRequest> requestList = getRequestList();
        List<ServiceRequest> remainingRequests = requestList.stream()
                .filter(request -> !request.getElderId().equals(elderId.trim()))
                .collect(Collectors.toList());
        DataStorageUtil.saveData(SERVICE_DATA_KEY, remainingRequests);
        int deleteCount = requestList.size() - remainingRequests.size();
        System.out.println("[ServiceRequestService] 同步删除老人" + elderId + "的服务申请：" + deleteCount + "条");
    }

    /**
     * 私有辅助方法：统一读取服务申请列表
     */
    @SuppressWarnings("unchecked")
    private List<ServiceRequest> getRequestList() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(SERVICE_DATA_KEY);
        return data == null ? new ArrayList<>() : (List<ServiceRequest>) data;
    }
}