package com.eldercare.service;

import com.eldercare.dao.ElderDao;
import com.eldercare.dao.impl.ElderDaoImpl;
import com.eldercare.model.Elder;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 老人业务逻辑类：处理老人信息CRUD、关联数据联动
 */
public class ElderService {
    // 单例模式
    private static final ElderService INSTANCE = new ElderService();
    private static final String ELDER_DATA_KEY = "elders"; // 本地存储key
    // 关联其他service（处理数据联动）
    private final HealthRecordService healthRecordService = HealthRecordService.getInstance();
    private final ServiceRequestService requestService = ServiceRequestService.getInstance();
    // 预留数据库接口
    private final ElderDao elderDao = new ElderDaoImpl();

    protected ElderService() {}
    public static ElderService getInstance() {
        return INSTANCE;
    }

    /**
     * 添加老人信息
     * @param elder 老人对象（需包含姓名、年龄、手机号等核心信息）
     */
    public void addElder(Elder elder) throws IOException, ClassNotFoundException {
        // 1. 输入校验（调用util工具，避免重复代码）
        InputValidator.validateNotEmpty(elder.getName(), "老人姓名");
        InputValidator.validateAge(elder.getAge());
        InputValidator.validatePhone(elder.getPhone());
        // 健康状况允许为空，默认“未填写”
        if (!InputValidator.isNotEmpty(elder.getHealthStatus())) {
            elder.setHealthStatus("未填写");
        }

        // 2. 生成唯一老人ID
        elder.setElderId(IdGenerator.generateElderId());

        // 3. 读取已有老人列表并添加
        List<Elder> elderList = getElderList();
        elderList.add(elder);
        DataStorageUtil.saveData(ELDER_DATA_KEY, elderList);
        System.out.println("[ElderService] 老人添加成功：" + elder.getName() + "（ID：" + elder.getElderId() + "）");

        // 4. 预留数据库操作：插入老人信息到数据库
        elderDao.insertElder(elder);
    }

    /**
     * 查询所有老人（支持按姓名模糊查询）
     * @param nameKeyword 姓名关键词（可为null，查询所有）
     * @return 匹配的老人列表
     */
    public List<Elder> queryElders(String nameKeyword) throws IOException, ClassNotFoundException {
        List<Elder> elderList = getElderList();
        // 模糊查询（忽略大小写）
        if (InputValidator.isNotEmpty(nameKeyword)) {
            String keyword = nameKeyword.trim().toLowerCase();
            elderList = elderList.stream()
                    .filter(elder -> elder.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }
        System.out.println("[ElderService] 查询到老人数量：" + elderList.size() + "（关键词：" + (nameKeyword == null ? "无" : nameKeyword) + "）");
        return elderList;
    }

    /**
     * 根据ID查询老人（用于关联健康记录、服务申请）
     * @param elderId 老人唯一ID
     * @return 匹配的老人对象；无匹配返回null
     */
    public Elder getElderById(String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(elderId, "老人ID");
        List<Elder> elderList = getElderList();
        // 遍历匹配ID
        for (Elder elder : elderList) {
            if (elder.getElderId().equals(elderId.trim())) {
                return elder;
            }
        }
        System.out.println("[ElderService] 未查询到老人：" + elderId);
        return null;
    }

    /**
     * 根据用户ID查询老人（用于用户登录后关联老人信息）
     * @param userId 用户唯一ID
     * @return 匹配的老人对象；无匹配返回null
     */
    public Elder getElderByUserId(String userId) throws IOException, ClassNotFoundException {
        System.out.println("[Debug-ElderService] 开始查询用户ID: '" + userId + "' 的老人信息");
        
        // 检查参数
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("[Debug-ElderService] 参数错误：用户ID为空");
            return null;
        }
        
        userId = userId.trim();
        System.out.println("[Debug-ElderService] 处理后的用户ID: '" + userId + "'");
        
        List<Elder> elderList = getElderList();
        System.out.println("[Debug-ElderService] 当前老人列表大小: " + elderList.size());
        
        // 遍历匹配用户ID
        for (Elder elder : elderList) {
            System.out.println("[Debug-ElderService] 检查老人: ID='" + elder.getElderId() + "', UserID='" + elder.getUserId() + "'");
            
            if (elder.getUserId() != null && elder.getUserId().equals(userId)) {
                System.out.println("[Debug-ElderService] 找到匹配的老人: " + elder.getName());
                return elder;
            }
        }
        
        System.out.println("[Debug-ElderService] 未查询到关联用户ID的老人：" + userId);
        return null;
    }

    /**
     * 删除老人（同步删除关联的健康记录、服务申请）
     * @param elderId 老人ID
     * @return true：删除成功；false：老人不存在
     */
    public boolean deleteElder(String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(elderId, "老人ID");
        List<Elder> elderList = getElderList();
        // 检查老人是否存在
        Elder targetElder = getElderById(elderId);
        if (targetElder == null) {
            return false;
        }

        // 1. 删除关联数据（健康记录、服务申请）
        healthRecordService.deleteRecordsByElderId(elderId);
        requestService.deleteRequestsByElderId(elderId);

        // 2. 删除老人本身
        elderList.removeIf(elder -> elder.getElderId().equals(elderId.trim()));
        DataStorageUtil.saveData(ELDER_DATA_KEY, elderList);
        System.out.println("[ElderService] 老人删除成功：" + targetElder.getName() + "（ID：" + elderId + "）");

        // 3. 预留数据库操作：后续可添加“删除数据库老人记录”的逻辑
        return true;
    }

    /**
     * 更新老人信息
     * @param elder 更新后的老人对象
     */
    public void updateElder(Elder elder) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(elder.getName(), "老人姓名");
        InputValidator.validateAge(elder.getAge());
        InputValidator.validatePhone(elder.getPhone());
        
        // 2. 检查老人是否存在
        Elder existingElder = getElderById(elder.getElderId());
        if (existingElder == null) {
            throw new IllegalArgumentException("老人不存在，无法更新");
        }
        
        // 3. 读取所有老人信息
        List<Elder> elderList = getElderList();
        
        // 4. 找到并更新老人信息
        for (int i = 0; i < elderList.size(); i++) {
            if (elderList.get(i).getElderId().equals(elder.getElderId())) {
                elderList.set(i, elder);
                break;
            }
        }
        
        // 5. 保存到数据存储
        DataStorageUtil.saveData(ELDER_DATA_KEY, elderList);
        System.out.println("[ElderService] 老人信息更新成功：" + elder.getElderId());
        
        // 6. 预留数据库操作：后续可添加“更新数据库老人记录”的逻辑
        elderDao.updateElder(elder);
    }
    
    /**
     * 私有辅助方法：统一读取老人列表
     */
    @SuppressWarnings("unchecked")
    private List<Elder> getElderList() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(ELDER_DATA_KEY);
        return data == null ? new ArrayList<>() : (List<Elder>) data;
    }
}