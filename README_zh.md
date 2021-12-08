[简体中文](./README_zh.md) | [English](./README.md)

![./doc/logo.png](./doc/logo.png)

> 此网站或产品包含IP2Location LITE数据，可从 http://www.ip2location.com 获取

# IP2Location Java 客户端

此代码仓库基于 [ip2location repository](https://github.com/ip2location/ip2location-java)。

## 下载

数据库 Bin 文件更新周期为一个月一次。

下载数据库 bin 文件：

- [Releases](https://github.com/renfei/ip2location/releases)

或者下载仓库中 ZIP 文件自行解压。

- [IP2LOCATION-LITE-DB11.BIN.ZIP](./IP2LOCATION-LITE-DB11.BIN.ZIP)
- [IP2LOCATION-LITE-DB11.IPV6.BIN.ZIP](./IP2LOCATION-LITE-DB11.IPV6.BIN.ZIP)

## 使用 Maven 安装客户端

```xml

<dependency>
    <groupId>net.renfei</groupId>
    <artifactId>ip2location</artifactId>
    <version>1.0.2</version>
</dependency>
```

## 用法

```java
import net.renfei.ip2location.*;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        IP2Location loc = new IP2Location();
        try {
            String ip = "8.8.8.8";
            String binfile = "/usr/data/IP2LOCATION-LITE-DB11.BIN";
            loc.Open(binfile, true);

            IPResult rec = loc.IPQuery(ip);
            if ("OK".equals(rec.getStatus())) {
                System.out.println(rec);
            } else if ("EMPTY_IP_ADDRESS".equals(rec.getStatus())) {
                System.out.println("IP address cannot be blank.");
            } else if ("INVALID_IP_ADDRESS".equals(rec.getStatus())) {
                System.out.println("Invalid IP address.");
            } else if ("MISSING_FILE".equals(rec.getStatus())) {
                System.out.println("Invalid database path.");
            } else if ("IPV6_NOT_SUPPORTED".equals(rec.getStatus())) {
                System.out.println("This BIN does not contain IPv6 data.");
            } else {
                System.out.println("Unknown error." + rec.getStatus());
            }
            System.out.println("Java Component: " + rec.getVersion());
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace(System.out);
        } finally {
            loc.Close();
        }
    }
}
```

### 关于数据库Bin文件只在 GitHub 发布的说明

码云（Gitee）发行功能附件大小有限制：

> 单个附件不能超过 100M（GVP 项目200M），每个仓库总附件不可超过 1G（推荐项目不可超过 5G；GVP 项目不可超过 20G）。
> 附件总容量统计包括仓库附件和发行版附件。