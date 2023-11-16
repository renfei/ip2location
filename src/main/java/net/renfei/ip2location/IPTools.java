package net.renfei.ip2location;

import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPTools {
    private static final BigInteger MAX_IPV4_RANGE = new BigInteger("4294967295");
    private static final BigInteger MAX_IPV6_RANGE = new BigInteger("340282366920938463463374607431768211455");
    private static final Pattern PATTERN = Pattern.compile("^(0:){2,}");
    private static final Pattern PATTERN2 = Pattern.compile(":(0:){2,}");
    private static final Pattern PATTERN3 = Pattern.compile("(:0){2,}$");
    private static final Pattern BIN_PATTERN_FULL = Pattern.compile("^([01]{8}){16}$");
    private static final Pattern BIN_PATTERN = Pattern.compile("([01]{8})");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^[0-9]{1,2}$");
    private static final Pattern PREFIX_PATTERN2 = Pattern.compile("^[0-9]{1,3}$");

    public IPTools() {

    }

    /**
     * This function checks if the string contains an IPv4 address.
     *
     * @param IPAddress IP Address to check
     * @return Boolean
     */
    public boolean IsIPv4(String IPAddress) {
        boolean result;
        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            result = ia instanceof Inet4Address;
        } catch (UnknownHostException ex) {
            result = false;
        }
        return result;
    }

    /**
     * This function checks if the string contains an IPv6 address.
     *
     * @param IPAddress IP Address to check
     * @return Boolean
     */
    public boolean IsIPv6(String IPAddress) {
        boolean result;
        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            result = ia instanceof Inet6Address;
        } catch (UnknownHostException ex) {
            result = false;
        }
        return result;
    }

    /**
     * This function converts IPv4 to IP number.
     *
     * @param IPAddress IP Address you wish to convert
     * @return BigInteger
     */
    public BigInteger IPv4ToDecimal(String IPAddress) {
        BigInteger result;

        if (!IsIPv4(IPAddress)) {
            return null;
        }

        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            final byte[] byteArr = ia.getAddress();
            result = new BigInteger(1, byteArr);
        } catch (UnknownHostException ex) {
            result = null;
        }
        return result;
    }

    /**
     * This function converts IPv6 to IP number.
     *
     * @param IPAddress IP Address you wish to convert
     * @return BigInteger
     */
    public BigInteger IPv6ToDecimal(String IPAddress) {
        BigInteger result;

        if (!IsIPv6(IPAddress)) {
            return null;
        }

        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            final byte[] byteArr = ia.getAddress();
            result = new BigInteger(1, byteArr);
        } catch (UnknownHostException ex) {
            result = null;
        }
        return result;
    }

    /**
     * This function converts IP number to IPv4.
     *
     * @param IPNum IP number you wish to convert
     * @return String
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public String DecimalToIPv4(BigInteger IPNum) throws UnknownHostException {
        if (IPNum.compareTo(BigInteger.ZERO) < 0 || IPNum.compareTo(MAX_IPV4_RANGE) > 0) {
            return null;
        }
        byte[] byteArr = IPNum.toByteArray();

        if (byteArr.length > 4) {
            // strip sign byte
            byteArr = Arrays.copyOfRange(byteArr, byteArr.length - 4, byteArr.length);
        } else if (byteArr.length < 4) {
            byte[] pad = new byte[4 - byteArr.length]; // byte array with default zero values
            byte[] tmp = Arrays.copyOf(pad, pad.length + byteArr.length);
            System.arraycopy(byteArr, 0, tmp, pad.length, byteArr.length);
            byteArr = tmp;
        }

        final InetAddress ia = InetAddress.getByAddress(byteArr);
        return ia.getHostAddress();
    }

    /**
     * This function converts IP number to IPv6.
     *
     * @param IPNum IP number you wish to convert
     * @return String
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public String DecimalToIPv6(BigInteger IPNum) throws UnknownHostException {
        if (IPNum.compareTo(BigInteger.ZERO) < 0 || IPNum.compareTo(MAX_IPV6_RANGE) > 0) {
            return null;
        }
        byte[] byteArr = IPNum.toByteArray();

        if (byteArr.length > 16) {
            byteArr = Arrays.copyOfRange(byteArr, byteArr.length - 16, byteArr.length);
        } else if (byteArr.length < 16) {
            byte[] pad = new byte[16 - byteArr.length]; // byte array with default zero values
            byte[] tmp = Arrays.copyOf(pad, pad.length + byteArr.length);
            System.arraycopy(byteArr, 0, tmp, pad.length, byteArr.length);
            byteArr = tmp;
        }

        final InetAddress ia = InetAddress.getByAddress(byteArr);
        return ia.getHostAddress();
    }

    /**
     * This function returns the compressed form of the IPv6.
     *
     * @param IPAddress IP Address you wish to compress
     * @return String
     */
    public String CompressIPv6(String IPAddress) {
        if (!IsIPv6(IPAddress)) {
            return null;
        }
        String result;

        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            result = ia.getHostAddress();

            // compress the zeroes
            if (PATTERN.matcher(result).find()) {
                result = result.replaceFirst(PATTERN.toString(), "::");
            } else if (PATTERN2.matcher(result).find()) {
                result = result.replaceFirst(PATTERN2.toString(), "::");
            } else if (PATTERN3.matcher(result).find()) {
                result = result.replaceFirst(PATTERN3.toString(), "::");
            }
            result = result.replaceFirst("::0$", "::"); // special case

        } catch (UnknownHostException ex) {
            result = null;
        }
        return result;
    }

    /**
     * This function returns the expanded form of the IPv6.
     *
     * @param IPAddress IP Address you wish to expand
     * @return String
     */
    public String ExpandIPv6(String IPAddress) {
        if (!IsIPv6(IPAddress)) {
            return null;
        }
        String result;

        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            final byte[] byteArr = ia.getAddress();
            final String[] strArr = new String[byteArr.length];
            int x;

            for (x = 0; x < byteArr.length; x++) {
                strArr[x] = String.format("%02x", byteArr[x]);
            }
            result = String.join("", strArr);
            result = result.replaceAll("(.{4})", "$1:");
            result = result.substring(0, result.length() - 1);

        } catch (UnknownHostException ex) {
            result = null;
        }
        return result;
    }

    /**
     * This function returns the CIDR for an IPv4 range.
     *
     * @param IPFrom Starting IP of the range
     * @param IPTo   Ending IP of the range
     * @return List of strings
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public List<String> IPv4ToCIDR(String IPFrom, String IPTo) throws UnknownHostException {
        if (!IsIPv4(IPFrom) || !IsIPv4(IPTo)) {
            return null;
        }
        long startIP = IPv4ToDecimal(IPFrom).longValueExact();
        long endIP = IPv4ToDecimal(IPTo).longValueExact();

        List<String> result = new ArrayList<String>();

        while (endIP >= startIP) {
            long maxSize = 32;

            while (maxSize > 0) {
                long mask = (long) Math.pow(2, 32) - (long) Math.pow(2, 32 - (maxSize - 1));
                long maskBase = startIP & mask;

                if (maskBase != startIP) {
                    break;
                }

                maxSize -= 1;
            }

            double x = Math.log(endIP - startIP + 1) / Math.log(2);
            long maxDiff = 32L - (long) Math.floor(x);

            if (maxSize < maxDiff) {
                maxSize = maxDiff;
            }

            String ip = DecimalToIPv4(new BigInteger(String.valueOf(startIP)));
            result.add(ip + "/" + maxSize);
            startIP += Math.pow(2, 32 - maxSize);
        }
        return result;
    }

    private String IPToBinary(String IPAddress) {
        if (!IsIPv6(IPAddress)) {
            return null;
        }
        String result;

        try {
            final InetAddress ia = InetAddress.getByName(IPAddress);
            final byte[] byteArr = ia.getAddress();
            final String[] strArr = new String[byteArr.length];
            int x;

            for (x = 0; x < byteArr.length; x++) {
                strArr[x] = String.format("%8s", Integer.toBinaryString(byteArr[x] & 0xFF)).replace(' ', '0');
            }
            result = String.join("", strArr);
        } catch (UnknownHostException ex) {
            result = null;
        }
        return result;
    }

    private String BinaryToIP(String Binary) throws UnknownHostException {
        if (!BIN_PATTERN_FULL.matcher(Binary).matches()) {
            return null;
        }
        Matcher m = BIN_PATTERN.matcher(Binary);

        byte[] byteArr = new byte[16];
        String part;
        int x = 0;
        while (m.find()) {
            part = m.group(1);
            byteArr[x] = (byte) Integer.parseInt(part, 2); // parse as int first to bypass overflow issue
            x++;
        }

        final InetAddress ia = InetAddress.getByAddress(byteArr);
        return ia.getHostAddress();
    }

    /**
     * This function returns the CIDR for an IPv6 range.
     *
     * @param IPFrom Starting IP of the range
     * @param IPTo   Ending IP of the range
     * @return List of strings
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public List<String> IPv6ToCIDR(String IPFrom, String IPTo) throws UnknownHostException {
        if (!IsIPv6(IPFrom) || !IsIPv6(IPTo)) {
            return null;
        }

        String ipFromBin = IPToBinary(IPFrom);
        String ipToBin = IPToBinary(IPTo);

        if (ipFromBin == null || ipToBin == null) {
            return null;
        }

        List<String> result = new ArrayList<String>();
        int networkSize = 0;
        int shift = 0;
        String unpadded;
        String padded;
        Map<String, Integer> networks = new TreeMap<>();
        int n;

        List<Integer> values;

        if (ipFromBin.compareTo(ipToBin) == 0) {
            result.add(IPFrom + "/128");
            return result;
        }

        if (ipFromBin.compareTo(ipToBin) > 0) {
            String tmp = ipFromBin;
            ipFromBin = ipToBin;
            ipToBin = tmp;
        }

        do {
            if (ipFromBin.charAt(ipFromBin.length() - 1) == '1') {
                unpadded = ipFromBin.substring(networkSize, 128);
                padded = String.format("%-128s", unpadded).replace(' ', '0'); // pad right
                networks.put(padded, 128 - networkSize);
                n = ipFromBin.lastIndexOf('0');
                ipFromBin = (n == 0 ? "" : ipFromBin.substring(0, n)) + "1";
                ipFromBin = String.format("%-128s", ipFromBin).replace(' ', '0'); // pad right
            }

            if (ipToBin.charAt(ipToBin.length() - 1) == '0') {
                unpadded = ipToBin.substring(networkSize, 128);
                padded = String.format("%-128s", unpadded).replace(' ', '0'); // pad right
                networks.put(padded, 128 - networkSize);
                n = ipToBin.lastIndexOf('1');
                ipToBin = (n == 0 ? "" : ipToBin.substring(0, n)) + "0";
                ipToBin = String.format("%-128s", ipToBin).replace(' ', '1'); // pad right
            }

            if (ipToBin.compareTo(ipFromBin) < 0) {
                continue;
            }

            values = Arrays.asList(ipFromBin.lastIndexOf('0'), ipToBin.lastIndexOf('1'));
            shift = 128 - Collections.max(values);

            unpadded = ipFromBin.substring(0, 128 - shift);
            ipFromBin = String.format("%128s", unpadded).replace(' ', '0');
            unpadded = ipToBin.substring(0, 128 - shift);
            ipToBin = String.format("%128s", unpadded).replace(' ', '0');

            networkSize += shift;

            if (ipFromBin.compareTo(ipToBin) == 0) {
                unpadded = ipFromBin.substring(networkSize, 128);
                padded = String.format("%-128s", unpadded).replace(' ', '0'); // pad right
                networks.put(padded, 128 - networkSize);
            }

        } while (ipFromBin.compareTo(ipToBin) < 0);

        for (Map.Entry<String, Integer> entry : networks.entrySet()) {
            result.add(CompressIPv6(BinaryToIP(entry.getKey())) + "/" + entry.getValue());
        }

        return result;
    }

    /**
     * This function returns the IPv4 range for a CIDR.
     *
     * @param CIDR CIDR address to convert to range
     * @return Array of strings
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public String[] CIDRToIPv4(String CIDR) throws UnknownHostException {
        if (!CIDR.contains("/")) {
            return null;
        }

        String ip;
        int prefix;
        String[] arr = CIDR.split("/");
        String ipStart;
        String ipEnd;
        long ipStartLong;
        long ipEndLong;
        long total;

        if (arr.length != 2 || !IsIPv4(arr[0]) || !PREFIX_PATTERN.matcher(arr[1]).matches() || Integer.parseInt(arr[1]) > 32) {
            return null;
        }

        ip = arr[0];
        prefix = Integer.parseInt(arr[1]);

        ipStartLong = IPv4ToDecimal(ip).longValueExact();
        ipStartLong = ipStartLong & (-1L << (32 - prefix));
        ipStart = DecimalToIPv4(new BigInteger(String.valueOf(ipStartLong)));

        total = 1L << (32 - prefix);

        ipEndLong = ipStartLong + total - 1;

        if (ipEndLong > 4294967295L) {
            ipEndLong = 4294967295L;
        }

        ipEnd = DecimalToIPv4(new BigInteger(String.valueOf(ipEndLong)));

        return new String[]{ipStart, ipEnd};
    }

    /**
     * This function returns the IPv6 range for a CIDR.
     *
     * @param CIDR CIDR address to convert to range
     * @return Array of strings
     * @throws UnknownHostException If unable to convert byte array to IP address
     */
    public String[] CIDRToIPv6(String CIDR) throws UnknownHostException {
        if (!CIDR.contains("/")) {
            return null;
        }

        String ip;
        int prefix;
        String[] arr = CIDR.split("/");

        if (arr.length != 2 || !IsIPv6(arr[0]) || !PREFIX_PATTERN2.matcher(arr[1]).matches() || Integer.parseInt(arr[1]) > 128) {
            return null;
        }

        ip = arr[0];
        prefix = Integer.parseInt(arr[1]);

        String[] parts = ExpandIPv6(ip).split("\\:");

        String bitStart = StringUtils.repeat('1', prefix) + StringUtils.repeat('0', 128 - prefix);
        String bitEnd = StringUtils.repeat('0', prefix) + StringUtils.repeat('1', 128 - prefix);

        int chunkSize = 16;

        String[] floors = bitStart.split("(?<=\\G.{" + chunkSize + "})");
        String[] ceilings = bitEnd.split("(?<=\\G.{" + chunkSize + "})");

        List<String> startIP = new ArrayList(8);
        List<String> endIP = new ArrayList(8);

        for (int x = 0; x < 8; x++) {
            startIP.add(Integer.toHexString(Integer.parseInt(parts[x], 16) & Integer.parseInt(floors[x], 2)));
            endIP.add(Integer.toHexString(Integer.parseInt(parts[x], 16) | Integer.parseInt(ceilings[x], 2)));
        }

        String hexStartAddress = ExpandIPv6(StringUtils.join(startIP, ":"));
        String hexEndAddress = ExpandIPv6(StringUtils.join(endIP, ":"));

        return new String[]{hexStartAddress, hexEndAddress};
    }
}
