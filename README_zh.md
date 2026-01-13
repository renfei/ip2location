[简体中文](./README_zh.md) | [English](./README.md)

![./doc/logo.png](./doc/logo.png)

> 此网站或产品包含IP2Location LITE数据，可从 http://www.ip2location.com 获取

# IP2Location Java 客户端

此代码仓库基于 [ip2location repository](https://github.com/ip2location/ip2location-java)。

案例： [https://www.renfei.net/kitbox/ip](https://www.renfei.net/kitbox/ip)

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
    <version>1.2.6</version>
</dependency>
```

与官方客户端相比，我移除了IP2LocationWebService，移除了对其他库的依赖。

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

            // 这是用BIN文件初始化
            loc.Open(binfile, true);

            //这是用二进制数组初始化
            // Path binpath = Paths.get(binfile);
            // byte[] binFileBytes = Files.readAllBytes(binpath);
            // loc.Open(binFileBytes);

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

### 数据库字段

| 名称            | 类型                        | 描述                        |
|---------------|---------------------------|---------------------------|
| ip_from       | INT (10) / DECIMAL (39,0) | 第一个IP地址显示netblock。        |
| ip_to         | INT (10) / DECIMAL (39,0) | 上一个IP地址显示netblock。        |
| country_code  | CHAR(2)                   | 基于ISO 3166的两个字符的国家/地区代码。  |
| country_name  | VARCHAR(64)               | 基于ISO 3166的国家/地区名称。       |
| region_name   | VARCHAR(128)              | 地区或州名称。                   |
| city_name     | VARCHAR(128)              | 城市名。                      |
| latitude      | DOUBLE                    | 城市纬度。如果城市未知，则默认为首都纬度。     |
| longitude     | DOUBLE                    | 城市经度。如果城市未知，则默认为首都城市经度。   |
| zip_code      | VARCHAR(30)               | 邮编/邮政编码。                  |
| time_zone     | VARCHAR(8)                | UTC时区（支持DST）。             |

## IPTOOLS 工具类

## 方法

下面是此类支持的方法。

| 方法名称                                                       | 描述                                 |
|------------------------------------------------------------|------------------------------------|
| public boolean IsIPv4(String IPAddress)                    | 如果字符串是 IPv4 地址，则返回 true。否则为 false。 |
| public boolean IsIPv6(String IPAddress)                    | 如果字符串是 IPv6 地址，则返回 true。否则为 false。 |
| public BigInteger IPv4ToDecimal(String IPAddress)          | IPv4 地址转 IP 十进制编码。                 |
| public BigInteger IPv6ToDecimal(String IPAddress)          | IPv6 地址转 IP 十进制编码。                 |
| public String DecimalToIPv4(BigInteger IPNum)              | IP 十进制编码转 IPv4 地址。                 |
| public String DecimalToIPv6(BigInteger IPNum)              | IP 十进制编码转 IPv6 地址。                 |
| public String CompressIPv6(String IPAddress)               | 返回 IPv6 地址压缩格式。                    |
| public String ExpandIPv6(String IPAddress)                 | 返回 IPv6 地址扩展格式。                    |
| public List<String> IPv4ToCIDR(String IPFrom, String IPTo) | 从提供的 IPv4 范围返回 CIDR 列表。            |
| public List<String> IPv6ToCIDR(String IPFrom, String IPTo) | 从提供的 IPv6 范围返回 CIDR 列表。            |
| public String[] CIDRToIPv4(String CIDR)                    | 从提供的 CIDR 返回 IPv4 范围。              |
| public String[] CIDRToIPv6(String CIDR)                    | 从提供的 CIDR 返回 IPv6 范围。              |

## 用法

```java
import com.ip2location.*;

import java.math.BigInteger;
import java.util.*;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        try {
            IPTools tools = new IPTools();

            System.out.println(tools.IsIPv4("60.54.166.38"));
            System.out.println(tools.IsIPv6("2600:1f18:45b0:5b00:f5d8:4183:7710:ceec"));
            System.out.println(tools.IPv4ToDecimal("60.54.166.38"));
            System.out.println(tools.IPv6ToDecimal("2600:118:450:5b00:f5d8:4183:7710:ceec"));
            System.out.println(tools.DecimalToIPv4(new BigInteger("1010214438")));
            System.out.println(tools.DecimalToIPv6(new BigInteger("50510686025047391022278667396705210092")));
            System.out.println(tools.CompressIPv6("0000:0000:0000:0035:0000:FFFF:0000:0000"));
            System.out.println(tools.ExpandIPv6("500:6001:FE:35:0:FFFF::"));
            List<String> stuff = tools.IPv4ToCIDR("10.0.0.0", "10.10.2.255");
            stuff.forEach(System.out::println);
            List<String> stuff2 = tools.IPv6ToCIDR("2001:4860:4860:0000:0000:0000:0000:8888", "2001:4860:4860:0000:eeee:ffff:ffff:ffff");
            stuff2.forEach(System.out::println);
            String[] stuff3 = tools.CIDRToIPv4("10.123.80.0/12");
            System.out.println(stuff3[0]);
            System.out.println(stuff3[1]);
            String[] stuff4 = tools.CIDRToIPv6("2002:1234::abcd:ffff:c0a8:101/62");
            System.out.println(stuff4[0]);
            System.out.println(stuff4[1]);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace(System.out);
        }
    }
}
```

## 镜像

* [https://github.com/renfei/ip2location](https://github.com/renfei/ip2location)
* [https://gitlab.com/renfei/ip2location](https://gitlab.com/renfei/ip2location)
* [https://jihulab.com/renfei/ip2location](https://jihulab.com/renfei/ip2location)
* [https://gitee.com/rnf/ip2location](https://gitee.com/rnf/ip2location)

### 关于数据库Bin文件只在 GitHub 发布的说明

码云（Gitee）发行功能附件大小有限制：

> 单个附件不能超过 100M（GVP 项目200M），每个仓库总附件不可超过 1G（推荐项目不可超过 5G；GVP 项目不可超过 20G）。
> 附件总容量统计包括仓库附件和发行版附件。