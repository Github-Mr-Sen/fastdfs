package com.lovexk.fastdfs;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest(classes = FastdfsApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public  class FastdfsApplicationTests{
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private FdfsWebServer fdfsWebServer;
    @Test
    public void contextLoads() {
    }

    // D:\face\1.jpg
    @Test
    public void uploadFile() throws IOException {
        // File file = new File("D:\\face\\gyy001.jpg");
        File file = new File("D:\\face\\pet\\6.jpg");
        FileInputStream inputStream = new FileInputStream (file);
        StorePath storePath = storageClient.uploadFile(inputStream,file.length(), FilenameUtils.getExtension(file.getName()),null);

        String fullPath = storePath.getFullPath();  //group1/M00/00/00/wKjjZGJjlleAKbSsABBf2HQcEcQ271.jpg
        String path = storePath.getPath();
        System.out.println("path: ".concat(path));  //M00/00/00/wKjjZGJjlleAKbSsABBf2HQcEcQ271.jpg
        String group = storePath.getGroup();
        System.out.println("group: ".concat(group)); //group1
        System.out.println("fullPath: ".concat(fullPath));
        String webServerUrl = fdfsWebServer.getWebServerUrl();//http://192.168.227.100:8888/
        System.out.println(webServerUrl);


        System.out.println(webServerUrl + fullPath); //http://192.168.227.100:8888/group1/M00/00/00/wKjjZGJjlleAKbSsABBf2HQcEcQ271.jpg
        // return fullPath;
        // return getResAccessUrl(storePath);
    }

    @Test
    public void del() throws IOException {
        // storageClient.deleteFile();
        //http://192.168.227.100:8888/group1/M00/00/00/wKjjZGJjkuuATZrGAAKQfaBCBBQ503.jpg

        storageClient.deleteFile("group1","M00/00/00/wKjjZGJjkfmAXZGRAAbxbZwqOFU633.jpg");

    }

}
