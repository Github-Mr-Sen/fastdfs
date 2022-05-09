# 记一次docker安装fastDFS



[fastdfs的介绍](https://developer.aliyun.com/article/315215)

## 一  拉取镜像

```shell
docker pull delron/fastdfs 

```

## 二 启动fastdfs

### 启动两个服务

> 首先说一下fastDFS 的架构问题：
>
> fastDFS包括两个容器：
>
> 	1. tracker容器，用来追踪文件的，起到调度作用。我们可以理解成文件的索引
> 	1. storage容器，用来存储文件的。

```shell
## 启动tracker容器
docker run -dti --network=host --name tracker -v /upload/fdfs/tracker:/var/fdfs -v /etc/localtime:/etc/localtime delron/fastdfs tracker

## 启动storage 容器
ocker run -dti  --network=host --name storage -e TRACKER_SERVER=192.168.227.100:22122 -v /upload/fdfs/storage:/var/fdfs  -v /etc/localtime:/etc/localtime  delron/fastdfs storage

```

> 以上两个容器的启动，注意参数和挂在的目录，
>
> 其中，storage要需要tracker_server 的参数，该参数指向tracker容器的服务地址。tracker 的默认端口是22122，storage的默认端口是8888。

### 修改storage配置文件

```shell
#进入容器
docker exec -it storage bash  

#进入目录
cd /etc/fdfs/   

#编辑文件
vi storage.conf 
## 如下图所示，在配置文件的最后一行可以改端口
```

![image-20220421201535827](https://i0.hdslb.com/bfs/album/4de6dbc70599de70e503ddf4c5868e9026b72ecd.png)

### 修改nginx的配置文件

*** 这里我们添加一个配置文件***

```json
server {
    listen       8888;
    server_name  localhost;

    #charset koi8-r;
    #access_log  /var/log/nginx/log/host.access.log  main;
    location /static {
      root /usr/share/nginx/html;

    }

    location ~/group[0-9]/ {
      ngx_fastdfs_module;
    }

    #error_page  404              /404.html;

    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

![image-20220421201928430](https://i0.hdslb.com/bfs/album/841b0feea2f349e02941c1f2c5463045eacff318.png)

修改完文件需要重启nginx

## 三 上传一个文件测试一下

```shell
docker exec -it storage bash

cd /var/fdfs

echo hello hello fastdfs 许珂>a.txt

## 把文件存到fastdfs的命令。后面是有/etc/fdfs/client.conf （配置文件的地址）
/usr/bin/fdfs_upload_file /etc/fdfs/client.conf a.txt

# 上传成功后会返回一个地址，我们可以用这个地址就可以访问到文件
```

![image-20220421202509080](https://i0.hdslb.com/bfs/album/4a2317ca5ff5051021da1412a96c41618d6983e1.png)

访问格式：http://ip:port/文件地址，例如下面的例子

http://192.168.227.100:8888/group1/M00/00/00/wKjjZGJhRFWAG2P-AAAAFdBqtMs178.txt

<img src="https://i0.hdslb.com/bfs/album/ec6835e5b9b6f0f6665a043bee63a6490db6f04a.png" alt="image-20220421202747049"/>

至此，安装告一段落。截下来开始用java来操作。



## 四 java 操作

[官方sdk](https://github.com/happyfish100/fastdfs-client-java)

#### 原生







#### springboot

#### 配置文件

```yaml
server:
  port: 5200

ip: 192.168.227.100

fdfs:
  so-timeout: 1501
  connect-timeout: 601
  thumb-image:
    width: 150
    height: 150
  tracker-list:
    - ${ip}:22122
  web-server-url: http://${ip}:8888/
```

> 注意：
>
> 配置文件上我们定义了一个ip的变量，然后再tracker-list和web-server-url中引用了它

```java
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

@Component
public class FastDFSClient {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
        return getResAccessUrl(storePath);
    }

    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream (file);
        StorePath storePath = storageClient.uploadFile(inputStream,file.length(), FilenameUtils.getExtension(file.getName()),null);
        return getResAccessUrl(storePath);
    }

    /**
     * 将一段字符串生成一个文件上传
     * @param content 文件内容
     * @param fileExtension
     * @return
     */
    public String uploadFile(String content, String fileExtension) {
        byte[] buff = content.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream stream = new ByteArrayInputStream(buff);
        StorePath storePath = storageClient.uploadFile(stream,buff.length, fileExtension,null);
        return getResAccessUrl(storePath);
    }

    /**
     * 封装图片完整URL地址
      */
    private String getResAccessUrl(StorePath storePath) {
        String fileUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
        return fileUrl;
    }

    /**
     * 删除文件
     * @param fileUrl 文件访问地址
     * @return
     */
    public void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }
        try {
            StorePath storePath = StorePath.parseFromUrl(fileUrl);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (FdfsUnsupportStorePathException e) {
            System.out.println(e.getMessage());
            /** TODO 只是测试，所以未使用，logger，正式环境请修改打印方式 **/
        }
    }
    /**
     * 下载文件
     *
     * @param fileUrl 文件URL
     * @return 文件字节
     * @throws IOException
     */
    public byte[] downloadFile(String fileUrl) throws IOException {
        String group = fileUrl.substring(0, fileUrl.indexOf("/"));
        String path = fileUrl.substring(fileUrl.indexOf("/") + 1);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = storageClient.downloadFile(group, path, downloadByteArray);
        return bytes;
    }

}

```

