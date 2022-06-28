package com.jiaxuan.controller;


import com.jiaxuan.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class commonController {

    //从yml文件中读取
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)  {
        //file是一个临时文件，需要转存到指定位置，否则完成本次请求后临时文件会删除
        log.info(file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf(".")); //.jpg

        //使用uuid重新生成文件名，放置文件覆盖,将uuid文件名和截取的源文件名后缀进行拼接
        String fileName = UUID.randomUUID().toString() + substring;

        //判读路径文件是否存在，不存在创建
        File dir = new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();
        }


        //将文件存到制定位置
        try {
            file.transferTo(new File(basePath,fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        try {
            //通过输入流读取文件内容
            fileInputStream= new FileInputStream(new File(basePath,name));
            //通过输出流将文件写回浏览器
            outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
                fileInputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }



    }

}
