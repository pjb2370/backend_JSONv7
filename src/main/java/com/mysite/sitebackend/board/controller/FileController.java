package com.mysite.sitebackend.board.controller;

import com.mysite.sitebackend.board.dao.FileRepostiroy;
import com.mysite.sitebackend.board.domain.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {
    private final FileRepostiroy fileRepostiroy;
    String filepath = "C:/SCsite/image";

    // 이미지 업로드
    @PostMapping("/post/image")
    public FileEntity uploadImage(HttpServletRequest request,
                                  @RequestParam(value = "file", required = false) MultipartFile[] files) {
        String FileNames = "";
        String filepath = "/image";

        String originFileName = files[0].getOriginalFilename();
        long fileSize = files[0].getSize();
        String safeFile = System.currentTimeMillis() + originFileName;

        File f1 = new File(filepath + safeFile);
        try {
            files[0].transferTo(f1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final FileEntity file = FileEntity.builder().filename(safeFile).build();
        return fileRepostiroy.save(file);
    }

    // 이미지 다운로드
    @GetMapping("/download/image")
    public List<FileEntity> downloadImage(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam(value = "filename", required = true) String filename){
        File file = new File(filepath + filename);

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ServletOutputStream sos = null;

        try{
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            sos = response.getOutputStream();

            String reFilename = "";
            // IE로 실행한 경우인지 -> IE는 따로 인코딩 작업을 거쳐야 한다.
            // request헤어에 MSIE 또는 Trident가 포함되어 있는지 확인
            boolean isMSIE = request.getHeader("user-agent").indexOf("Trident") !=-1;

            if (isMSIE){
                reFilename = URLEncoder.encode("이미지파일.jpg", "utf-8");
                reFilename = reFilename.replaceAll("\\+", "%20");
            } else {
                reFilename = new String("이미지 파일.jpg".getBytes("utf-8"), "ISO-8859-1");
            }
            response.setContentType("application/octet-stream;charset=utf-8");
            response.addHeader("Content-Disposition", "attachment;filename=\""+reFilename+"\"");
            response.setContentLength((int)file.length());

            int read = 0;
            while ((read = bis.read()) != -1){
                sos.write(read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    // 이미지 불러오기
    @GetMapping("/image")
    public List<FileEntity> findAllImages() {return fileRepostiroy.findAll();}
}
