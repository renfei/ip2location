[简体中文](./README_zh.md) | [English](./README.md)

![./doc/logo.png](./doc/logo.png)

> This site or product includes IP2Location LITE data available from http://www.ip2location.com

# IP2Location for Java

The repository code is based on [ip2location repository](https://github.com/ip2location/ip2location-java).

Example: [https://www.renfei.net/kitbox/ip](https://www.renfei.net/kitbox/ip)

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
    <version>1.2.1</version>
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

            // this is to initialize with a BIN file
            loc.Open(binfile, true);

            // this is to initialize with a byte array
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

### Database Fields

| Name         | Type                      | Description                                                           |
|--------------|---------------------------|-----------------------------------------------------------------------|
| ip_from      | INT (10) / DECIMAL (39,0) | First IP address show netblock.                                       |
| ip_to        | INT (10) / DECIMAL (39,0) | Last IP address show netblock.                                        |
| country_code | CHAR(2)                   | Two-character country code based on ISO 3166.                         |
| country_name | VARCHAR(64)               | Country name based on ISO 3166.                                       |
| region_name  | VARCHAR(128)              | Region or state name.                                                 |
| city_name    | VARCHAR(128)              | City name.                                                            |
| latitude     | DOUBLE                    | City latitude. Default to capital city latitude if city is unknown.   |
| longitude    | DOUBLE                    | City longitude. Default to capital city longitude if city is unknown. |
| zip_code     | VARCHAR(30)               | ZIP/Postal code.                                                      |
| time_zone    | VARCHAR(8)                | UTC time zone (with DST supported).                                   |

## IPTOOLS CLASS

## Methods

Below are the methods supported in this class.

| Method Name                                                | Description                                                       |
|------------------------------------------------------------|-------------------------------------------------------------------|
| public boolean IsIPv4(String IPAddress)                    | Returns true if string contains an IPv4 address. Otherwise false. |
| public boolean IsIPv6(String IPAddress)                    | Returns true if string contains an IPv6 address. Otherwise false. |
| public BigInteger IPv4ToDecimal(String IPAddress)          | Returns the IP number for an IPv4 address.                        |
| public BigInteger IPv6ToDecimal(String IPAddress)          | Returns the IP number for an IPv6 address.                        |
| public String DecimalToIPv4(BigInteger IPNum)              | Returns the IPv4 address for the supplied IP number.              |
| public String DecimalToIPv6(BigInteger IPNum)              | Returns the IPv6 address for the supplied IP number.              |
| public String CompressIPv6(String IPAddress)               | Returns the IPv6 address in compressed form.                      |
| public String ExpandIPv6(String IPAddress)                 | Returns the IPv6 address in expanded form.                        |
| public List<String> IPv4ToCIDR(String IPFrom, String IPTo) | Returns a list of CIDR from the supplied IPv4 range.              |
| public List<String> IPv6ToCIDR(String IPFrom, String IPTo) | Returns a list of CIDR from the supplied IPv6 range.              |
| public String[] CIDRToIPv4(String CIDR)                    | Returns the IPv4 range from the supplied CIDR.                    |
| public String[] CIDRToIPv6(String CIDR)                    | Returns the IPv6 range from the supplied CIDR.                    |

## Usage

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

## Mirror

* [https://github.com/renfei/ip2location](https://github.com/renfei/ip2location)
* [https://gitlab.com/renfei/ip2location](https://gitlab.com/renfei/ip2location)
* [https://jihulab.com/renfei/ip2location](https://jihulab.com/renfei/ip2location)
* [https://gitee.com/rnf/ip2location](https://gitee.com/rnf/ip2location)