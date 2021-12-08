[简体中文](./README_zh.md) | [English](./README.md)

![./doc/logo.png](./doc/logo.png)

> This site or product includes IP2Location LITE data available from http://www.ip2location.com

# IP2Location for Java

The repository code is based on [ip2location repository](https://github.com/ip2location/ip2location-java).

## Download

The update cycle of database bin file is once a month.

Database bin file:

- [Releases](https://github.com/renfei/ip2location/releases)

Or download the zip file in the warehouse and decompress it by yourself.

- [IP2LOCATION-LITE-DB11.BIN.ZIP](./IP2LOCATION-LITE-DB11.BIN.ZIP)
- [IP2LOCATION-LITE-DB11.IPV6.BIN.ZIP](./IP2LOCATION-LITE-DB11.IPV6.BIN.ZIP)

## Installing in maven

```xml

<dependency>
    <groupId>net.renfei</groupId>
    <artifactId>ip2location</artifactId>
    <version>1.0.2</version>
</dependency>
```

Compared with the official client, I removed the ip2locationweb service and the dependency on other libraries.

## Usage

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