package com.bean.lostandfound.server;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.bean.lostandfound.config.OssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class OssService {

    @Autowired
    private OssConfig ossConfig;

    /**
     * 上传文件到OSS
     * @param file 文件
     * @param folder 文件夹路径（如：avatar/、lostfound/）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replaceAll("-", "")
                    + "_" + originalFilename;

            // 完整路径
            String objectName = folder + fileName;

            // 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    inputStream
            );

                // 上传文件
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 返回文件访问URL
            return ossConfig.getDomain() + "/" + objectName;
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除OSS上的文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        // 从URL中提取objectName
        String objectName = fileUrl.replace(ossConfig.getDomain() + "/", "");

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );

        try {
            // 删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
