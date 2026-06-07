package com.bean.lostandfound.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class ExcelUtil {
    
    /**
     * 导出Excel文件
     * @param response HTTP响应
     * @param fileName 文件名（不含扩展名）
     * @param clazz 数据类型
     * @param data 数据列表
     * @throws IOException IO异常
     */
    public static void export(HttpServletResponse response, String fileName, Class<?> clazz, List<?> data) throws IOException {
        export(response, fileName, clazz, data, "Sheet1");
    }
    
    /**
     * 导出Excel文件（自定义Sheet名称）
     * @param response HTTP响应
     * @param fileName 文件名（不含扩展名）
     * @param clazz 数据类型
     * @param data 数据列表
     * @param sheetName Sheet名称
     * @throws IOException IO异常
     */
    public static void export(HttpServletResponse response, String fileName, Class<?> clazz, List<?> data, String sheetName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 防止中文文件名乱码
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(sheetName)
                .doWrite(data);
    }
}