package com.example.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    /**
     * 上传/下载根目录。
     * 可通过 application.yml 的 file.base-path 配置（相对进程工作目录，也可填绝对路径），
     * 默认 ./files/ —— 即后端需在 ai_knowledge/ 目录下启动，仓库自带的 files/ 才能被读到。
     */
    @Value("${file.base-path:./files/}")
    private String filePath;

    @Value("${fileBaseUrl:}")
    private String fileBaseUrl;

    /**
     * 归一化为绝对路径。
     * 注意：Hutool 的 FileUtil 会把相对路径按 classpath 解析（jar 运行时会解析到 BOOT-INF/classes 下），
     * 因此这里统一以「进程工作目录」为基准转成绝对路径，既保留相对路径的可移植性，又符合直觉。
     */
    @PostConstruct
    public void initFilePath() {
        File dir = new File(filePath);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), filePath);
        }
        filePath = Paths.get(dir.getAbsolutePath()).normalize().toString() + File.separator;
        log.info("文件上传/下载根目录：{}", filePath);
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            fileName = System.currentTimeMillis() + "-" + fileName;
            String realFilePath = filePath + fileName;
            // 文件存储形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(), realFilePath);
        } catch (Exception e) {
            log.error(fileName + "--文件上传失败", e);
        }
        String url = fileBaseUrl + "/files/download/" + fileName;
        return Result.success(url);
    }

    /**
     * 获取文件
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        OutputStream os;
        try {
            if (StrUtil.isNotEmpty(fileName)) {
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
                response.setContentType("application/octet-stream");
                byte[] bytes = FileUtil.readBytes(filePath + fileName);
                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            log.warn("文件下载失败：" + fileName + "，当前文件根目录 filePath=" + filePath, e);
        }
    }

}
