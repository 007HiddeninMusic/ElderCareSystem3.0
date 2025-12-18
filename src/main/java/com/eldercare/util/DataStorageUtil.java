package com.eldercare.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 数据存储工具类
 * 功能：基于本地文件序列化实现数据持久化
 * 支持对象保存、读取、删除，自动处理目录创建、流关闭，兼容空数据
 */
public class DataStorageUtil {
    // 数据存储根路径（项目resources/data目录，确保开发环境可直接访问）
    private static final String BASE_DATA_PATH = "src/main/resources/data/";

    /**
     * 保存数据到本地文件（序列化）
     * @param key 数据标识，如"elders"对应老人列表，生成文件elders.ser
     * @param data 要保存的对象，必须实现Serializable接口
     * @throws IOException 当文件写入失败（如权限不足）时抛出
     */
    public static void saveData(String key, Object data) throws IOException {
        // 1. 参数合法性校验
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("数据标识【key】不能为空");
        }
        if (data == null) {
            throw new IllegalArgumentException("要保存的数据不能为null");
        }
        if (!(data instanceof Serializable)) {
            throw new IllegalArgumentException("数据对象未实现Serializable接口，无法序列化");
        }

        // 2. 确保存储目录存在（不存在则自动创建）
        Files.createDirectories(Paths.get(BASE_DATA_PATH));

        // 3. 序列化对象到文件（try-with-resources自动关闭流，避免资源泄露）
        String filePath = BASE_DATA_PATH + key.trim() + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filePath)
        )) {
            oos.writeObject(data);
            System.out.println("[DataStorageUtil] 数据保存成功：" + filePath);
        }
    }

    /**
     * 从本地文件读取数据（反序列化）
     * @param key 数据标识，与保存时的key对应，如"users"读取用户列表
     * @return 反序列化后的对象，需自行强转为目标类型，无数据时返回null
     * @throws IOException 当文件读取失败（如文件损坏）时抛出
     * @throws ClassNotFoundException 当序列化类不存在时抛出
     */
    public static Object getData(String key) throws IOException, ClassNotFoundException {
        // 1. 参数合法性校验
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("数据标识【key】不能为空");
        }

        // 2. 构建文件路径，判断文件是否存在
        String filePath = BASE_DATA_PATH + key.trim() + ".ser";
        File dataFile = new File(filePath);
        if (!dataFile.exists()) {
            System.out.println("[DataStorageUtil] 数据文件不存在：" + filePath + "，返回null");
            return null;
        }

        // 3. 反序列化读取对象
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(dataFile)
        )) {
            Object data = ois.readObject();
            System.out.println("[DataStorageUtil] 数据读取成功：" + filePath + "，数据类型：" + data.getClass().getSimpleName());
            return data;
        }
    }

    /**
     * 删除指定key的数据文件（用于数据清理、重置场景）
     * @param key 数据标识
     * @return true-删除成功，false-文件不存在或删除失败
     */
    public static boolean deleteData(String key) {
        if (key == null || key.trim().isEmpty()) {
            System.err.println("[DataStorageUtil] 数据标识【key】不能为空，删除失败");
            return false;
        }

        String filePath = BASE_DATA_PATH + key.trim() + ".ser";
        File dataFile = new File(filePath);
        if (dataFile.exists()) {
            boolean isDeleted = dataFile.delete();
            System.out.println("[DataStorageUtil] 数据文件" + (isDeleted ? "删除成功" : "删除失败") + "：" + filePath);
            return isDeleted;
        } else {
            System.out.println("[DataStorageUtil] 数据文件不存在：" + filePath + "，无需删除");
            return false;
        }
    }
}