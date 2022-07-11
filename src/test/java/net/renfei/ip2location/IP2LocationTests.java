package net.renfei.ip2location;

import java.math.BigInteger;
import java.util.List;

public class IP2LocationTests {
    public static void main(String[] args) {
        ipToolTest();
//        test();
    }

    private static void test() {
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

    private static void ipToolTest() {
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
