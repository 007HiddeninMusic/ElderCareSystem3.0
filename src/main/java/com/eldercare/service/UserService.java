package com.eldercare.service;

import com.eldercare.dao.UserDao;
import com.eldercare.dao.impl.UserDaoImpl;
import com.eldercare.model.User;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用户业务逻辑类：处理登录、用户添加/查询等核心业务
 * 单例模式：确保全局唯一实例，避免重复创建
 */
public class UserService {
    // 1. 单例模式实现（饿汉式，线程安全）
    private static final UserService INSTANCE = new UserService();
    // 数据存储key（与DataStorageUtil对应，存储用户列表）
    private static final String USER_DATA_KEY = "users";
    // 预留数据库接口：关联空实现，后续可替换为真实数据库实现
    private final UserDao userDao = new UserDaoImpl();

    // 私有构造：禁止外部new实例
    private UserService() {}

    // 全局获取实例方法
    public static UserService getInstance() {
        return INSTANCE;
    }

    /**
     * 用户登录：校验账号密码，返回登录成功的用户对象
     * @param userId 账号
     * @param password 密码
     * @return 登录成功的User对象；失败返回null
     */
    public User login(String userId, String password) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(userId, "账号");
        InputValidator.validateNotEmpty(password, "密码");
        if (!InputValidator.isUserIdValid(userId)) {
            throw new IllegalArgumentException("账号格式非法！请输入4-20位字母/数字/下划线");
        }

        // 2. 读取所有用户
        List<User> userList = getUserList();
        if (userList.isEmpty()) {
            throw new RuntimeException("暂无用户数据，请联系管理员初始化账号");
        }

        // 3. 校验账号密码（遍历匹配，简化逻辑）
        for (User user : userList) {
            if (user.getUserId().equals(userId.trim()) && user.getPassword().equals(password)) {
                System.out.println("[UserService] 用户登录成功：" + userId + "（角色：" + user.getRole() + "）");
                return user;
            }
        }

        // 4. 登录失败
        throw new IllegalArgumentException("账号或密码错误，请重新输入");
    }

    /**
     * 添加用户（如新增家属、护工账号）
     * @param user 待添加的用户对象（需指定角色、密码）
     */
    public void addUser(User user) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(user.getUserId(), "账号");
        InputValidator.validateNotEmpty(user.getPassword(), "密码");
        InputValidator.validateUserId(user.getUserId()); // 校验账号格式
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("用户角色不能为空（可选：admin/elder/family/caregiver）");
        }

        // 2. 校验账号唯一性（避免重复添加）
        List<User> userList = getUserList();
        for (User existUser : userList) {
            if (existUser.getUserId().equals(user.getUserId().trim())) {
                throw new IllegalArgumentException("账号已存在：" + user.getUserId() + "，请更换账号");
            }
        }

        // 3. 生成用户ID（可选：若用自定义账号则无需此步，此处兼容两种场景）
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId(IdGenerator.generateUserId());
        }

        // 4. 保存用户数据到本地文件
        userList.add(user);
        DataStorageUtil.saveData(USER_DATA_KEY, userList);
        System.out.println("[UserService] 用户添加成功：" + user.getUserId() + "（角色：" + user.getRole() + "）");

        // 5. 预留数据库操作：调用UserDao空实现，标记数据库插入位置
        userDao.insertUser(user);
    }

    /**
     * 查询所有用户（用于管理员管理账号）
     * @return 用户列表（无数据返回空列表）
     */
    public List<User> getAllUsers() throws IOException, ClassNotFoundException {
        List<User> userList = getUserList();
        System.out.println("[UserService] 查询到用户总数：" + userList.size());
        return userList;
    }

    public void registerUser(String userId, String password, String role)
            throws IOException, ClassNotFoundException {
        // 1. 验证输入非空
        InputValidator.validateNotEmpty(userId, "账号");
        InputValidator.validateNotEmpty(password, "密码");

        // 2. 验证角色是否合法（适配你的四类角色）
        if (!"admin".equals(role) &&
                !"elder".equals(role) &&
                !"family".equals(role) &&
                !"caregiver".equals(role)) {
            throw new IllegalArgumentException("角色必须是 'admin'、'elder'、'family' 或 'caregiver'");
        }

        // 3. 验证账号格式
        if (!InputValidator.isUserIdValid(userId)) {
            throw new IllegalArgumentException("账号格式非法（3-20位字母数字）");
        }

        // 4. 检查账号是否已存在
        List<User> users = getUserList();
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                throw new IllegalArgumentException("账号已存在");
            }
        }

        // 5. 创建新用户并保存
        users.add(new User(userId, password, role));
        saveUserList(users);
    }

    private void saveUserList(List<User> userList) throws IOException {
        // 确保 data 目录存在
        File dir = new File("src/main/resources/data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "users.ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(userList);
        }
    }
    /**
     * 私有辅助方法：统一读取用户列表（兼容空数据场景）
     */
    @SuppressWarnings("unchecked")
    private List<User> getUserList() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(USER_DATA_KEY);
        return data == null ? new ArrayList<>() : (List<User>) data;
    }

    /**
     * 更新用户密码
     * @param userId 用户账号
     * @param newPassword 新密码（未加密）
     */
    public void updateUserPassword(String userId, String newPassword) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(userId, "账号");
        InputValidator.validateNotEmpty(newPassword, "新密码");
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        // 2. 查找用户并更新密码
        List<User> userList = getUserList();
        boolean found = false;
        for (User user : userList) {
            if (user.getUserId().equals(userId.trim())) {
                user.setPassword(newPassword); // User类已实现密码长度校验
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("用户不存在：" + userId);
        }

        // 3. 保存更新后的用户列表
        saveUserList(userList);
        System.out.println("[UserService] 用户密码更新成功：" + userId);

        // 4. 预留数据库操作
        userDao.updateUserPassword(userId, newPassword);
    }

    /**
     * 更新用户角色
     * @param userId 用户账号
     * @param newRole 新角色（admin/elder/family/caregiver）
     */
    public void updateUserRole(String userId, String newRole) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(userId, "账号");
        InputValidator.validateNotEmpty(newRole, "角色");
        if (!"admin".equals(newRole) && !"elder".equals(newRole) &&
                !"family".equals(newRole) && !"caregiver".equals(newRole)) {
            throw new IllegalArgumentException("角色必须是 admin/elder/family/caregiver");
        }

        // 2. 查找用户并更新角色
        List<User> userList = getUserList();
        boolean found = false;
        for (User user : userList) {
            if (user.getUserId().equals(userId.trim())) {
                user.setRole(newRole); // User类已实现角色合法性校验
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("用户不存在：" + userId);
        }

        // 3. 保存更新后的用户列表
        saveUserList(userList);
        System.out.println("[UserService] 用户角色更新成功：" + userId + " -> " + newRole);
    }

    /**
     * 根据用户ID查询用户信息
     * @param userId 用户账号（唯一标识）
     * @return 匹配的用户对象；无匹配时返回null
     */
    public User getUserById(String userId) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(userId, "用户ID");
        if (!InputValidator.isUserIdValid(userId)) {
            throw new IllegalArgumentException("账号格式非法！请输入4-20位字母/数字/下划线");
        }

        // 2. 查找用户
        List<User> userList = getUserList();
        for (User user : userList) {
            if (user.getUserId().equals(userId.trim())) {
                System.out.println("[UserService] 查询到用户：" + userId + "（角色：" + user.getRole() + "）");
                return user;
            }
        }

        // 3. 未找到用户
        System.out.println("[UserService] 未查询到用户：" + userId);
        return null;
    }

    /**
     * 删除指定ID的用户
     * @param userId 用户账号（唯一标识）
     * @throws IOException IO异常（文件操作失败）
     * @throws ClassNotFoundException 类加载异常（反序列化失败）
     * @throws IllegalArgumentException 用户不存在/参数非法
     */
    public void deleteUser(String userId) throws IOException, ClassNotFoundException {
        // 1. 输入合法性校验
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空！");
        }
        String targetUserId = userId.trim();

        // 2. 获取现有用户列表
        List<User> userList = getUserList(); // 复用原有获取用户列表的方法
        boolean isDeleted = false;

        // 3. 遍历删除指定用户（迭代器避免并发修改异常）
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (targetUserId.equals(user.getUserId())) {
                iterator.remove(); // 安全删除元素
                isDeleted = true;
                break; // 唯一ID，找到后直接退出
            }
        }

        // 4. 未找到用户则抛异常
        if (!isDeleted) {
            throw new IllegalArgumentException("删除失败：用户ID【" + targetUserId + "】不存在！");
        }

        // 5. 保存删除后的用户列表（持久化）
        saveUserList(userList); // 复用原有保存用户列表的方法
        System.out.println("[UserService] 成功删除用户：" + targetUserId);

        // 6. 预留数据库操作（若已对接DAO层）
        if (userDao != null) {
            userDao.deleteUserById(targetUserId); // 需确保UserDao中有此方法
        }
    }
}