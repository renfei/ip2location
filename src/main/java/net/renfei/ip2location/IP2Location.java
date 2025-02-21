package net.renfei.ip2location;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class IP2Location {
    private static final Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"); // IPv4
    private static final Pattern pattern2 = Pattern.compile("^([0-9A-F]{1,4}:){6}(0[0-9]+\\.|.*?\\.0[0-9]+).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern pattern3 = Pattern.compile("^[0-9]+$");
    private static final Pattern pattern4 = Pattern.compile("^(.*:)(([0-9]+\\.){3}[0-9]+)$");
    private static final Pattern pattern5 = Pattern.compile("^.*((:[0-9A-F]{1,4}){2})$");
    private static final Pattern pattern6 = Pattern.compile("^[0:]+((:[0-9A-F]{1,4}){1,2})$", Pattern.CASE_INSENSITIVE);
    private static final BigInteger MAX_IPV4_RANGE = new BigInteger("4294967295");
    private static final BigInteger MAX_IPV6_RANGE = new BigInteger("340282366920938463463374607431768211455");
    private static final BigInteger FROM_6TO4 = new BigInteger("42545680458834377588178886921629466624");
    private static final BigInteger TO_6TO4 = new BigInteger("42550872755692912415807417417958686719");
    private static final BigInteger FROM_TEREDO = new BigInteger("42540488161975842760550356425300246528");
    private static final BigInteger TO_TEREDO = new BigInteger("42540488241204005274814694018844196863");
    private static final BigInteger LAST_32BITS = new BigInteger("4294967295");

    private static final int[] COUNTRY_POSITION = {0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
    private static final int[] REGION_POSITION = {0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private static final int[] CITY_POSITION = {0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
    private static final int[] ISP_POSITION = {0, 0, 3, 0, 5, 0, 7, 5, 7, 0, 8, 0, 9, 0, 9, 0, 9, 0, 9, 7, 9, 0, 9, 7, 9, 9, 9};
    private static final int[] LATITUDE_POSITION = {0, 0, 0, 0, 0, 5, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
    private static final int[] LONGITUDE_POSITION = {0, 0, 0, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6};
    private static final int[] DOMAIN_POSITION = {0, 0, 0, 0, 0, 0, 0, 6, 8, 0, 9, 0, 10, 0, 10, 0, 10, 0, 10, 8, 10, 0, 10, 8, 10, 10, 10};
    private static final int[] ZIPCODE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 0, 7, 7, 7, 0, 7, 0, 7, 7, 7, 0, 7, 7, 7};
    private static final int[] TIMEZONE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 7, 8, 8, 8, 7, 8, 0, 8, 8, 8, 0, 8, 8, 8};
    private static final int[] NETSPEED_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 11, 8, 11, 0, 11, 0, 11, 0, 11, 11, 11};
    private static final int[] IDDCODE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 12, 0, 12, 0, 12, 9, 12, 0, 12, 12, 12};
    private static final int[] AREACODE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 13, 0, 13, 0, 13, 10, 13, 0, 13, 13, 13};
    private static final int[] WEATHERSTATIONCODE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 14, 0, 14, 0, 14, 0, 14, 14, 14};
    private static final int[] WEATHERSTATIONNAME_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 15, 0, 15, 0, 15, 0, 15, 15, 15};
    private static final int[] MCC_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 16, 9, 16, 16, 16};
    private static final int[] MNC_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 17, 0, 17, 10, 17, 17, 17};
    private static final int[] MOBILEBRAND_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 18, 0, 18, 11, 18, 18, 18};
    private static final int[] ELEVATION_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 19, 0, 19, 19, 19};
    private static final int[] USAGETYPE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 20, 20, 20};
    private static final int[] ADDRESSTYPE_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 21};
    private static final int[] CATEGORY_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 22};
    private static final int[] DISTRICT_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23};
    private static final int[] ASN_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24};
    private static final int[] AS_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25};
    static final DecimalFormat GEO_COORDINATE_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        GEO_COORDINATE_FORMAT = new DecimalFormat("###.######", symbols);
    }

    private MetaData _MetaData = null;
    private MappedByteBuffer _IPv4Buffer = null;
    private MappedByteBuffer _IPv6Buffer = null;
    private MappedByteBuffer _MapDataBuffer = null;
    private final int[][] _IndexArrayIPv4 = new int[65536][2];
    private final int[][] _IndexArrayIPv6 = new int[65536][2];
    private long _MapDataOffset = 0;
    private int _IPv4ColumnSize = 0;
    private int _IPv6ColumnSize = 0;

    /**
     * To use memory mapped file for faster queries, set to true.
     */
    public boolean UseMemoryMappedFile = false;
    /**
     * Sets the path for the BIN database (IPv4 BIN or IPv4+IPv6 BIN).
     */
    public String IPDatabasePath = "";
    /**
     * Sets the path for the license key file.
     */
    public String IPLicensePath = "";
    private FileLike.Supplier binFile;
    private int COUNTRY_POSITION_OFFSET;
    private int REGION_POSITION_OFFSET;
    private int CITY_POSITION_OFFSET;
    private int ISP_POSITION_OFFSET;
    private int DOMAIN_POSITION_OFFSET;
    private int ZIPCODE_POSITION_OFFSET;
    private int LATITUDE_POSITION_OFFSET;
    private int LONGITUDE_POSITION_OFFSET;
    private int TIMEZONE_POSITION_OFFSET;
    private int NETSPEED_POSITION_OFFSET;
    private int IDDCODE_POSITION_OFFSET;
    private int AREACODE_POSITION_OFFSET;
    private int WEATHERSTATIONCODE_POSITION_OFFSET;
    private int WEATHERSTATIONNAME_POSITION_OFFSET;
    private int MCC_POSITION_OFFSET;
    private int MNC_POSITION_OFFSET;
    private int MOBILEBRAND_POSITION_OFFSET;
    private int ELEVATION_POSITION_OFFSET;
    private int USAGETYPE_POSITION_OFFSET;
    private int ADDRESSTYPE_POSITION_OFFSET;
    private int CATEGORY_POSITION_OFFSET;
    private int DISTRICT_POSITION_OFFSET;
    private int ASN_POSITION_OFFSET;
    private int AS_POSITION_OFFSET;
    private boolean COUNTRY_ENABLED;
    private boolean REGION_ENABLED;
    private boolean CITY_ENABLED;
    private boolean ISP_ENABLED;
    private boolean LATITUDE_ENABLED;
    private boolean LONGITUDE_ENABLED;
    private boolean DOMAIN_ENABLED;
    private boolean ZIPCODE_ENABLED;
    private boolean TIMEZONE_ENABLED;
    private boolean NETSPEED_ENABLED;
    private boolean IDDCODE_ENABLED;
    private boolean AREACODE_ENABLED;
    private boolean WEATHERSTATIONCODE_ENABLED;
    private boolean WEATHERSTATIONNAME_ENABLED;
    private boolean MCC_ENABLED;
    private boolean MNC_ENABLED;
    private boolean MOBILEBRAND_ENABLED;
    private boolean ELEVATION_ENABLED;
    private boolean USAGETYPE_ENABLED;
    private boolean ADDRESSTYPE_ENABLED;
    private boolean CATEGORY_ENABLED;
    private boolean DISTRICT_ENABLED;
    private boolean ASN_ENABLED;
    private boolean AS_ENABLED;

    public IP2Location() {

    }

    interface FileLike {

        interface Supplier {
            FileLike open() throws IOException;

            boolean isValid();
        }

        int read(byte[] buffer) throws IOException;

        int read(byte b[], int off, int len) throws IOException;

        void seek(long pos) throws IOException;

        void close() throws IOException;
    }

    /**
     * This function returns the package version.
     *
     * @return Package version
     */
    public String GetPackageVersion() {
        if (_MetaData == null) {
            return "";
        }
        if (_MetaData.getDBType() == 0) {
            return "";
        } else {
            return String.valueOf(_MetaData.getDBType());
        }
    }

    /**
     * This function returns the IP database version.
     *
     * @return IP database version
     */
    public String GetDatabaseVersion() {
        if (_MetaData == null) {
            return "";
        }
        if (_MetaData.getDBYear() == 0) {
            return "";
        } else {
            return "20" + _MetaData.getDBYear() + "." + _MetaData.getDBMonth() + "." + _MetaData.getDBDay();
        }
    }

    /**
     * This function can be used to pre-load the BIN file.
     *
     * @param DBPath The full path to the IP2Location BIN database file
     * @throws IOException If an input or output exception occurred
     */
    public void Open(String DBPath) throws IOException {
        IPDatabasePath = DBPath;
        binFile = new FileLike.Supplier() {
            public FileLike open() throws IOException {
                return new FileLike() {
                    private final RandomAccessFile aFile = new RandomAccessFile(DBPath, "r");

                    public int read(byte[] buffer) throws IOException {
                        return aFile.read(buffer);
                    }

                    public int read(byte[] b, int off, int len) throws IOException {
                        return aFile.read(b, off, len);
                    }

                    public void seek(long pos) throws IOException {
                        aFile.seek(pos);
                    }

                    public void close() throws IOException {
                        aFile.close();
                    }
                };
            }

            public boolean isValid() {
                return DBPath.length() > 0;
            }
        };

        LoadBIN();
    }

    /**
     * This function can be used to initialize the component with params and pre-load the BIN file.
     *
     * @param DBPath The full path to the IP2Location BIN database file
     * @param UseMMF Set to true to load the BIN database file into memory mapped file
     * @throws IOException If an input or output exception occurred
     */
    public void Open(String DBPath, boolean UseMMF) throws IOException {
        UseMemoryMappedFile = UseMMF;
        Open(DBPath);
    }

    public void Open(byte[] db) throws IOException {
        binFile = new FileLike.Supplier() {
            public FileLike open() {
                return new FileLike() {
                    private final ByteArrayInputStream stream = new ByteArrayInputStream(db);

                    public int read(byte[] buffer) throws IOException {
                        return stream.read(buffer);
                    }

                    public int read(byte[] b, int off, int len) {
                        return stream.read(b, off, len);
                    }

                    public void seek(long pos) {
                        stream.reset();
                        stream.skip(pos);
                    }

                    public void close() throws IOException {
                        stream.close();
                    }
                };
            }

            public boolean isValid() {
                return db.length > 0;
            }
        };
        LoadBIN();
    }

    /**
     * This function destroys the mapped bytes.
     */
    public void Close() {
        _MetaData = null;
        DestroyMappedBytes();
    }

    private void DestroyMappedBytes() {
        _IPv4Buffer = null;
        _IPv6Buffer = null;
        _MapDataBuffer = null;
    }

    private synchronized void CreateMappedBytes() throws IOException {
        try (RandomAccessFile aFile = new RandomAccessFile(IPDatabasePath, "r")) {
            final FileChannel inChannel = aFile.getChannel();
            CreateMappedBytes(inChannel);
        }
    }

    private void CreateMappedBytes(FileChannel inChannel) throws IOException {
        if (_IPv4Buffer == null) {
            final long _IPv4Bytes = (long) _IPv4ColumnSize * (long) _MetaData.getDBCount();
            long _IPv4Offset = _MetaData.getBaseAddr() - 1;
            _IPv4Buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, _IPv4Offset, _IPv4Bytes);
            _IPv4Buffer.order(ByteOrder.LITTLE_ENDIAN);
            _MapDataOffset = _IPv4Offset + _IPv4Bytes;
        }

        if (!_MetaData.getOldBIN() && _IPv6Buffer == null) {
            final long _IPv6Bytes = (long) _IPv6ColumnSize * (long) _MetaData.getDBCountIPv6();
            long _IPv6Offset = _MetaData.getBaseAddrIPv6() - 1;
            _IPv6Buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, _IPv6Offset, _IPv6Bytes);
            _IPv6Buffer.order(ByteOrder.LITTLE_ENDIAN);
            _MapDataOffset = _IPv6Offset + _IPv6Bytes;
        }

        if (_MapDataBuffer == null) {
            _MapDataBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, _MapDataOffset, inChannel.size() - _MapDataOffset);
            _MapDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    private boolean LoadBIN() throws IOException {
        boolean loadOK = false;
        FileLike aFile = null;

        try {
            if (binFile.isValid()) {
                aFile = binFile.open();
                byte[] _HeaderData = new byte[64];
                aFile.read(_HeaderData);
                ByteBuffer _HeaderBuffer = ByteBuffer.wrap(_HeaderData);
                _HeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);

                _MetaData = new MetaData();

                _MetaData.setDBType(_HeaderBuffer.get(0));
                _MetaData.setDBColumn(_HeaderBuffer.get(1));
                _MetaData.setDBYear(_HeaderBuffer.get(2));
                _MetaData.setDBMonth(_HeaderBuffer.get(3));
                _MetaData.setDBDay(_HeaderBuffer.get(4));
                _MetaData.setDBCount(_HeaderBuffer.getInt(5)); // 4 bytes
                _MetaData.setBaseAddr(_HeaderBuffer.getInt(9)); // 4 bytes
                _MetaData.setDBCountIPv6(_HeaderBuffer.getInt(13)); // 4 bytes
                _MetaData.setBaseAddrIPv6(_HeaderBuffer.getInt(17)); // 4 bytes
                _MetaData.setIndexBaseAddr(_HeaderBuffer.getInt(21)); //4 bytes
                _MetaData.setIndexBaseAddrIPv6(_HeaderBuffer.getInt(25)); //4 bytes
                _MetaData.setProductCode(_HeaderBuffer.get(29));
                // below 2 fields just read for now, not being used yet
                _MetaData.setProductType(_HeaderBuffer.get(30));
                _MetaData.setFileSize(_HeaderBuffer.getInt(31)); //4 bytes

                // check if is correct BIN (should be 1 for IP2Location BIN file), also checking for zipped file (PK being the first 2 chars)
                if ((_MetaData.getProductCode() != 1 && _MetaData.getDBYear() >= 21) || (_MetaData.getDBType() == 80 && _MetaData.getDBColumn() == 75)) // only BINs from Jan 2021 onwards have this byte set
                {
                    throw new IOException("Incorrect IP2Location BIN file format. Please make sure that you are using the latest IP2Location BIN file.");
                }

                if (_MetaData.getIndexBaseAddr() > 0) {
                    _MetaData.setIndexed(true);
                }

                if (_MetaData.getDBCountIPv6() == 0) { // old style IPv4-only BIN file
                    _MetaData.setOldBIN(true);
                } else {
                    if (_MetaData.getIndexBaseAddrIPv6() > 0) {
                        _MetaData.setIndexedIPv6(true);
                    }
                }

                final int dbcoll = _MetaData.getDBColumn();
                _IPv4ColumnSize = dbcoll << 2; // 4 bytes each column
                _IPv6ColumnSize = 16 + ((dbcoll - 1) << 2); // 4 bytes each column, except IPFrom column which is 16 bytes

                final int dbtype = _MetaData.getDBType();

                COUNTRY_POSITION_OFFSET = (COUNTRY_POSITION[dbtype] != 0) ? (COUNTRY_POSITION[dbtype] - 2) << 2 : 0;
                REGION_POSITION_OFFSET = (REGION_POSITION[dbtype] != 0) ? (REGION_POSITION[dbtype] - 2) << 2 : 0;
                CITY_POSITION_OFFSET = (CITY_POSITION[dbtype] != 0) ? (CITY_POSITION[dbtype] - 2) << 2 : 0;
                ISP_POSITION_OFFSET = (ISP_POSITION[dbtype] != 0) ? (ISP_POSITION[dbtype] - 2) << 2 : 0;
                DOMAIN_POSITION_OFFSET = (DOMAIN_POSITION[dbtype] != 0) ? (DOMAIN_POSITION[dbtype] - 2) << 2 : 0;
                ZIPCODE_POSITION_OFFSET = (ZIPCODE_POSITION[dbtype] != 0) ? (ZIPCODE_POSITION[dbtype] - 2) << 2 : 0;
                LATITUDE_POSITION_OFFSET = (LATITUDE_POSITION[dbtype] != 0) ? (LATITUDE_POSITION[dbtype] - 2) << 2 : 0;
                LONGITUDE_POSITION_OFFSET = (LONGITUDE_POSITION[dbtype] != 0) ? (LONGITUDE_POSITION[dbtype] - 2) << 2 : 0;
                TIMEZONE_POSITION_OFFSET = (TIMEZONE_POSITION[dbtype] != 0) ? (TIMEZONE_POSITION[dbtype] - 2) << 2 : 0;
                NETSPEED_POSITION_OFFSET = (NETSPEED_POSITION[dbtype] != 0) ? (NETSPEED_POSITION[dbtype] - 2) << 2 : 0;
                IDDCODE_POSITION_OFFSET = (IDDCODE_POSITION[dbtype] != 0) ? (IDDCODE_POSITION[dbtype] - 2) << 2 : 0;
                AREACODE_POSITION_OFFSET = (AREACODE_POSITION[dbtype] != 0) ? (AREACODE_POSITION[dbtype] - 2) << 2 : 0;
                WEATHERSTATIONCODE_POSITION_OFFSET = (WEATHERSTATIONCODE_POSITION[dbtype] != 0) ? (WEATHERSTATIONCODE_POSITION[dbtype] - 2) << 2 : 0;
                WEATHERSTATIONNAME_POSITION_OFFSET = (WEATHERSTATIONNAME_POSITION[dbtype] != 0) ? (WEATHERSTATIONNAME_POSITION[dbtype] - 2) << 2 : 0;
                MCC_POSITION_OFFSET = (MCC_POSITION[dbtype] != 0) ? (MCC_POSITION[dbtype] - 2) << 2 : 0;
                MNC_POSITION_OFFSET = (MNC_POSITION[dbtype] != 0) ? (MNC_POSITION[dbtype] - 2) << 2 : 0;
                MOBILEBRAND_POSITION_OFFSET = (MOBILEBRAND_POSITION[dbtype] != 0) ? (MOBILEBRAND_POSITION[dbtype] - 2) << 2 : 0;
                ELEVATION_POSITION_OFFSET = (ELEVATION_POSITION[dbtype] != 0) ? (ELEVATION_POSITION[dbtype] - 2) << 2 : 0;
                USAGETYPE_POSITION_OFFSET = (USAGETYPE_POSITION[dbtype] != 0) ? (USAGETYPE_POSITION[dbtype] - 2) << 2 : 0;
                ADDRESSTYPE_POSITION_OFFSET = (ADDRESSTYPE_POSITION[dbtype] != 0) ? (ADDRESSTYPE_POSITION[dbtype] - 2) << 2 : 0;
                CATEGORY_POSITION_OFFSET = (CATEGORY_POSITION[dbtype] != 0) ? (CATEGORY_POSITION[dbtype] - 2) << 2 : 0;
                DISTRICT_POSITION_OFFSET = (DISTRICT_POSITION[dbtype] != 0) ? (DISTRICT_POSITION[dbtype] - 2) << 2 : 0;
                ASN_POSITION_OFFSET = (ASN_POSITION[dbtype] != 0) ? (ASN_POSITION[dbtype] - 2) << 2 : 0;
                AS_POSITION_OFFSET = (AS_POSITION[dbtype] != 0) ? (AS_POSITION[dbtype] - 2) << 2 : 0;

                COUNTRY_ENABLED = (COUNTRY_POSITION[dbtype] != 0);
                REGION_ENABLED = (REGION_POSITION[dbtype] != 0);
                CITY_ENABLED = (CITY_POSITION[dbtype] != 0);
                ISP_ENABLED = (ISP_POSITION[dbtype] != 0);
                LATITUDE_ENABLED = (LATITUDE_POSITION[dbtype] != 0);
                LONGITUDE_ENABLED = (LONGITUDE_POSITION[dbtype] != 0);
                DOMAIN_ENABLED = (DOMAIN_POSITION[dbtype] != 0);
                ZIPCODE_ENABLED = (ZIPCODE_POSITION[dbtype] != 0);
                TIMEZONE_ENABLED = (TIMEZONE_POSITION[dbtype] != 0);
                NETSPEED_ENABLED = (NETSPEED_POSITION[dbtype] != 0);
                IDDCODE_ENABLED = (IDDCODE_POSITION[dbtype] != 0);
                AREACODE_ENABLED = (AREACODE_POSITION[dbtype] != 0);
                WEATHERSTATIONCODE_ENABLED = (WEATHERSTATIONCODE_POSITION[dbtype] != 0);
                WEATHERSTATIONNAME_ENABLED = (WEATHERSTATIONNAME_POSITION[dbtype] != 0);
                MCC_ENABLED = (MCC_POSITION[dbtype] != 0);
                MNC_ENABLED = (MNC_POSITION[dbtype] != 0);
                MOBILEBRAND_ENABLED = (MOBILEBRAND_POSITION[dbtype] != 0);
                ELEVATION_ENABLED = (ELEVATION_POSITION[dbtype] != 0);
                USAGETYPE_ENABLED = (USAGETYPE_POSITION[dbtype] != 0);
                ADDRESSTYPE_ENABLED = (ADDRESSTYPE_POSITION[dbtype] != 0);
                CATEGORY_ENABLED = (CATEGORY_POSITION[dbtype] != 0);
                DISTRICT_ENABLED = (DISTRICT_POSITION[dbtype] != 0);
                ASN_ENABLED = (ASN_POSITION[dbtype] != 0);
                AS_ENABLED = (AS_POSITION[dbtype] != 0);

                if (_MetaData.getIndexed()) {
                    int readLen = _IndexArrayIPv4.length;
                    if (_MetaData.getIndexedIPv6()) {
                        readLen += _IndexArrayIPv6.length;
                    }

                    byte[] _IndexData = new byte[readLen * 8]; // 4 bytes for both from row and to row
                    aFile.seek(_MetaData.getIndexBaseAddr() - 1);
                    aFile.read(_IndexData);
                    ByteBuffer _IndexBuffer = ByteBuffer.wrap(_IndexData);
                    _IndexBuffer.order(ByteOrder.LITTLE_ENDIAN);

                    int pointer = 0;

                    // read IPv4 index
                    for (int x = 0; x < _IndexArrayIPv4.length; x++) {
                        _IndexArrayIPv4[x][0] = _IndexBuffer.getInt(pointer); // 4 bytes for from row
                        _IndexArrayIPv4[x][1] = _IndexBuffer.getInt(pointer + 4); // 4 bytes for to row
                        pointer += 8;
                    }

                    if (_MetaData.getIndexedIPv6()) {
                        // read IPv6 index
                        for (int x = 0; x < _IndexArrayIPv6.length; x++) {
                            _IndexArrayIPv6[x][0] = _IndexBuffer.getInt(pointer); // 4 bytes for from row
                            _IndexArrayIPv6[x][1] = _IndexBuffer.getInt(pointer + 4); // 4 bytes for to row
                            pointer += 8;
                        }
                    }
                }

                if (UseMemoryMappedFile) {
                    CreateMappedBytes();
                } else {
                    DestroyMappedBytes();
                }
                loadOK = true;
            }
        } finally {
            if (aFile != null) {
                aFile.close();
            }
        }
        return loadOK;
    }

    /**
     * @deprecated
     */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * This function to query IP2Location data.
     *
     * @param IPAddress IP Address you wish to query
     * @return IP2Location data
     * @throws IOException If an input or output exception occurred
     */
    public IPResult IPQuery(String IPAddress) throws IOException {
        if (IPAddress != null) {
            IPAddress = IPAddress.trim();
        }
        IPResult record = new IPResult(IPAddress);
        FileLike filehandle = null;
        ByteBuffer mybuffer = null;
        ByteBuffer mydatabuffer = null;
        byte[] row;
        byte[] fullrow = null;

        try {
            if (IPAddress == null || IPAddress.length() == 0) {
                record.status = "EMPTY_IP_ADDRESS";
                return record;
            }

            BigInteger ipno;
            int indexaddr;
            int actualiptype;
            int myiptype;
            int mybaseaddr = 0;
            int mycolumnsize;
            int mybufcapacity = 0;
            BigInteger MAX_IP_RANGE;
            long rowoffset;
            long rowoffset2;
            BigInteger[] bi;
            boolean overcapacity = false;
            String[] retarr;

            try {
                bi = ip2No(IPAddress);
                myiptype = bi[0].intValue();
                ipno = bi[1];
                actualiptype = bi[2].intValue();
                if (actualiptype == 6) { // means didn't match IPv4 regex
                    retarr = expandIPV6(IPAddress, myiptype);
                    record.ip_address = retarr[0]; // return after expand IPv6 format
                    myiptype = Integer.parseInt(retarr[1]); // special cases
                }
            } catch (UnknownHostException e) {
                record.status = "INVALID_IP_ADDRESS";
                return record;
            }

            long low = 0;
            long high;
            long mid;
            long position;
            BigInteger ipfrom;
            BigInteger ipto;
            int firstcol = 4; // IP From is 4 bytes

            // Read BIN if haven't done so
            if (_MetaData == null) {
                if (!LoadBIN()) { // problems reading BIN
                    record.status = "MISSING_FILE";
                    return record;
                }
            }

            if (UseMemoryMappedFile) {
                if ((_IPv4Buffer == null) || (!_MetaData.getOldBIN() && _IPv6Buffer == null) || (_MapDataBuffer == null)) {
                    CreateMappedBytes();
                }
            } else {
                DestroyMappedBytes();
                filehandle = binFile.open();
            }

            if (myiptype == 4) { // IPv4
                MAX_IP_RANGE = MAX_IPV4_RANGE;
                high = _MetaData.getDBCount();

                if (UseMemoryMappedFile) {
                    mybuffer = _IPv4Buffer.duplicate(); // this enables this thread to maintain its own position in a multi-threaded environment
                    mybuffer.order(ByteOrder.LITTLE_ENDIAN);
                    mybufcapacity = mybuffer.capacity();
                } else {
                    mybaseaddr = _MetaData.getBaseAddr();
                }
                mycolumnsize = _IPv4ColumnSize;

                if (_MetaData.getIndexed()) {
                    indexaddr = ipno.shiftRight(16).intValue();
                    low = _IndexArrayIPv4[indexaddr][0];
                    high = _IndexArrayIPv4[indexaddr][1];
                }
            } else { // IPv6
                firstcol = 16; // IPv6 is 16 bytes

                if (_MetaData.getOldBIN()) {
                    record.status = "IPV6_NOT_SUPPORTED";
                    return record;
                }
                MAX_IP_RANGE = MAX_IPV6_RANGE;
                high = _MetaData.getDBCountIPv6();

                if (UseMemoryMappedFile) {
                    mybuffer = _IPv6Buffer.duplicate(); // this enables this thread to maintain its own position in a multi-threaded environment
                    mybuffer.order(ByteOrder.LITTLE_ENDIAN);
                    mybufcapacity = mybuffer.capacity();
                } else {
                    mybaseaddr = _MetaData.getBaseAddrIPv6();
                }
                mycolumnsize = _IPv6ColumnSize;

                if (_MetaData.getIndexedIPv6()) {
                    indexaddr = ipno.shiftRight(112).intValue();
                    low = _IndexArrayIPv6[indexaddr][0];
                    high = _IndexArrayIPv6[indexaddr][1];
                }
            }

            if (ipno.compareTo(MAX_IP_RANGE) == 0) ipno = ipno.subtract(BigInteger.ONE);

            while (low <= high) {
                mid = (low + high) / 2;
                rowoffset = mybaseaddr + (mid * mycolumnsize);
                rowoffset2 = rowoffset + mycolumnsize;

                if (UseMemoryMappedFile) {
                    // only reading the IP From fields
                    overcapacity = (rowoffset2 >= mybufcapacity);
                    ipfrom = read32or128(rowoffset, myiptype, mybuffer, filehandle);
                    ipto = (overcapacity) ? BigInteger.ZERO : read32or128(rowoffset2, myiptype, mybuffer, filehandle);
                } else {
                    // reading IP From + whole row + next IP From
                    fullrow = readRow(rowoffset, mycolumnsize + firstcol, mybuffer, filehandle);
                    ipfrom = read32or128Row(fullrow, 0, firstcol);
                    ipto = (overcapacity) ? BigInteger.ZERO : read32or128Row(fullrow, mycolumnsize, firstcol);
                }

                if (ipno.compareTo(ipfrom) >= 0 && ipno.compareTo(ipto) < 0) {

                    int rowlen = mycolumnsize - firstcol;

                    if (UseMemoryMappedFile) {
                        row = readRow(rowoffset + firstcol, rowlen, mybuffer, filehandle);
                        mydatabuffer = _MapDataBuffer.duplicate(); // this is to enable reading of a range of bytes in multi-threaded environment
                        mydatabuffer.order(ByteOrder.LITTLE_ENDIAN);
                    } else {
                        row = new byte[rowlen];
                        System.arraycopy(fullrow, firstcol, row, (int) 0, rowlen); // extract the actual row data
                    }

                    if (COUNTRY_ENABLED) {
                        position = read32Row(row, COUNTRY_POSITION_OFFSET).longValue();
                        record.country_short = readStr(position, mydatabuffer, filehandle);
                        position += 3;
                        record.country_long = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.country_short = IPResult.NOT_SUPPORTED;
                        record.country_long = IPResult.NOT_SUPPORTED;
                    }
                    if (REGION_ENABLED) {
                        position = read32Row(row, REGION_POSITION_OFFSET).longValue();
                        record.region = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.region = IPResult.NOT_SUPPORTED;
                    }
                    if (CITY_ENABLED) {
                        position = read32Row(row, CITY_POSITION_OFFSET).longValue();
                        record.city = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.city = IPResult.NOT_SUPPORTED;
                    }
                    if (ISP_ENABLED) {
                        position = read32Row(row, ISP_POSITION_OFFSET).longValue();
                        record.isp = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.isp = IPResult.NOT_SUPPORTED;
                    }
                    if (LATITUDE_ENABLED) {
                        record.latitude = Float.parseFloat(setDecimalPlaces(readFloatRow(row, LATITUDE_POSITION_OFFSET)));
                    } else {
                        record.latitude = 0.0F;
                    }
                    if (LONGITUDE_ENABLED) {
                        record.longitude = Float.parseFloat(setDecimalPlaces(readFloatRow(row, LONGITUDE_POSITION_OFFSET)));
                    } else {
                        record.longitude = 0.0F;
                    }
                    if (DOMAIN_ENABLED) {
                        position = read32Row(row, DOMAIN_POSITION_OFFSET).longValue();
                        record.domain = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.domain = IPResult.NOT_SUPPORTED;
                    }
                    if (ZIPCODE_ENABLED) {
                        position = read32Row(row, ZIPCODE_POSITION_OFFSET).longValue();
                        record.zipcode = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.zipcode = IPResult.NOT_SUPPORTED;
                    }
                    if (TIMEZONE_ENABLED) {
                        position = read32Row(row, TIMEZONE_POSITION_OFFSET).longValue();
                        record.timezone = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.timezone = IPResult.NOT_SUPPORTED;
                    }
                    if (NETSPEED_ENABLED) {
                        position = read32Row(row, NETSPEED_POSITION_OFFSET).longValue();
                        record.netspeed = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.netspeed = IPResult.NOT_SUPPORTED;
                    }
                    if (IDDCODE_ENABLED) {
                        position = read32Row(row, IDDCODE_POSITION_OFFSET).longValue();
                        record.iddcode = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.iddcode = IPResult.NOT_SUPPORTED;
                    }
                    if (AREACODE_ENABLED) {
                        position = read32Row(row, AREACODE_POSITION_OFFSET).longValue();
                        record.areacode = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.areacode = IPResult.NOT_SUPPORTED;
                    }
                    if (WEATHERSTATIONCODE_ENABLED) {
                        position = read32Row(row, WEATHERSTATIONCODE_POSITION_OFFSET).longValue();
                        record.weatherstationcode = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.weatherstationcode = IPResult.NOT_SUPPORTED;
                    }
                    if (WEATHERSTATIONNAME_ENABLED) {
                        position = read32Row(row, WEATHERSTATIONNAME_POSITION_OFFSET).longValue();
                        record.weatherstationname = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.weatherstationname = IPResult.NOT_SUPPORTED;
                    }
                    if (MCC_ENABLED) {
                        position = read32Row(row, MCC_POSITION_OFFSET).longValue();
                        record.mcc = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.mcc = IPResult.NOT_SUPPORTED;
                    }
                    if (MNC_ENABLED) {
                        position = read32Row(row, MNC_POSITION_OFFSET).longValue();
                        record.mnc = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.mnc = IPResult.NOT_SUPPORTED;
                    }
                    if (MOBILEBRAND_ENABLED) {
                        position = read32Row(row, MOBILEBRAND_POSITION_OFFSET).longValue();
                        record.mobilebrand = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.mobilebrand = IPResult.NOT_SUPPORTED;
                    }
                    if (ELEVATION_ENABLED) {
                        position = read32Row(row, ELEVATION_POSITION_OFFSET).longValue();
                        record.elevation = convertFloat(readStr(position, mydatabuffer, filehandle)); // due to value being stored as a string but output as float
                    } else {
                        record.elevation = 0.0F;
                    }
                    if (USAGETYPE_ENABLED) {
                        position = read32Row(row, USAGETYPE_POSITION_OFFSET).longValue();
                        record.usagetype = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.usagetype = IPResult.NOT_SUPPORTED;
                    }
                    if (ADDRESSTYPE_ENABLED) {
                        position = read32Row(row, ADDRESSTYPE_POSITION_OFFSET).longValue();
                        record.addresstype = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.addresstype = IPResult.NOT_SUPPORTED;
                    }
                    if (CATEGORY_ENABLED) {
                        position = read32Row(row, CATEGORY_POSITION_OFFSET).longValue();
                        record.category = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.category = IPResult.NOT_SUPPORTED;
                    }
                    if (DISTRICT_ENABLED) {
                        position = read32Row(row, DISTRICT_POSITION_OFFSET).longValue();
                        record.district = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.district = IPResult.NOT_SUPPORTED;
                    }
                    if (ASN_ENABLED) {
                        position = read32Row(row, ASN_POSITION_OFFSET).longValue();
                        record.asn = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.asn = IPResult.NOT_SUPPORTED;
                    }
                    if (AS_ENABLED) {
                        position = read32Row(row, AS_POSITION_OFFSET).longValue();
                        record.as = readStr(position, mydatabuffer, filehandle);
                    } else {
                        record.as = IPResult.NOT_SUPPORTED;
                    }
                    record.status = "OK";
                    break;
                } else {
                    if (ipno.compareTo(ipfrom) < 0) {
                        high = mid - 1;
                    } else {
                        low = mid + 1;
                    }
                }
            }
            return record;
        } finally {
            if (filehandle != null) {
                filehandle.close();
            }
        }
    }

    private String[] expandIPV6(final String myIP, final int myiptype) {
        final String tmp = "0000:0000:0000:0000:0000:";
        final String padme = "0000";
        final long hexoffset = 0xFF;
        String myIP2 = myIP.toUpperCase();
        String rettype = String.valueOf(myiptype);

        // expand ipv4-mapped ipv6
        if (myiptype == 4) {
            if (pattern4.matcher(myIP2).matches()) {
                myIP2 = myIP2.replaceAll("::", tmp);
            } else {
                Matcher mat = pattern5.matcher(myIP2);

                if (mat.matches()) {
                    String mymatch = mat.group(1);

                    String[] myarr = mymatch.replaceAll("^:+", "").replaceAll(":+$", "").split(":");

                    int len = myarr.length;
                    StringBuffer bf = new StringBuffer(32);
                    for (int x = 0; x < len; x++) {
                        String unpadded = myarr[x];
                        bf.append(padme.substring(unpadded.length())).append(unpadded); // safe padding for JDK 1.4
                    }
                    long mylong = new BigInteger(bf.toString(), 16).longValue();

                    long[] b = {0, 0, 0, 0}; // using long in place of bytes due to 2's complement signed issue

                    for (int x = 0; x < 4; x++) {
                        b[x] = mylong & hexoffset;
                        mylong = mylong >> 8;
                    }

                    myIP2 = myIP2.replaceAll(mymatch + "$", ":" + b[3] + "." + b[2] + "." + b[1] + "." + b[0]);
                    myIP2 = myIP2.replaceAll("::", tmp);
                }
            }
        } else if (myiptype == 6) {
            if (myIP2.equals("::")) {
                myIP2 = myIP2 + "0.0.0.0";
                myIP2 = myIP2.replaceAll("::", tmp + "FFFF:");
                rettype = "4";
            } else {
                // same regex as myiptype 4 but different scenario
                Matcher mat = pattern4.matcher(myIP2);
                if (mat.matches()) {
                    String v6part = mat.group(1);
                    String v4part = mat.group(2);

                    String[] v4arr = v4part.split("\\.");
                    int[] v4intarr = new int[4];

                    int len = v4intarr.length;
                    for (int x = 0; x < len; x++) {
                        v4intarr[x] = Integer.parseInt(v4arr[x]);
                    }
                    int part1 = (v4intarr[0] << 8) + v4intarr[1];
                    int part2 = (v4intarr[2] << 8) + v4intarr[3];
                    String part1hex = Integer.toHexString(part1);
                    String part2hex = Integer.toHexString(part2);

                    StringBuffer bf = new StringBuffer(v6part.length() + 9);
                    bf.append(v6part);
                    bf.append(padme.substring(part1hex.length()));
                    bf.append(part1hex);
                    bf.append(":");
                    bf.append(padme.substring(part2hex.length()));
                    bf.append(part2hex);

                    myIP2 = bf.toString().toUpperCase();

                    String[] myarr = myIP2.split("::");

                    String[] leftside = myarr[0].split(":");

                    StringBuffer bf2 = new StringBuffer(40);
                    StringBuffer bf3 = new StringBuffer(40);
                    StringBuffer bf4 = new StringBuffer(40);

                    len = leftside.length;
                    int totalsegments = 0;
                    for (int x = 0; x < len; x++) {
                        if (leftside[x].length() > 0) {
                            totalsegments++;
                            bf2.append(padme.substring(leftside[x].length()));
                            bf2.append(leftside[x]);
                            bf2.append(":");
                        }
                    }

                    if (myarr.length > 1) {
                        String[] rightside = myarr[1].split(":");

                        len = rightside.length;
                        for (int x = 0; x < len; x++) {
                            if (rightside[x].length() > 0) {
                                totalsegments++;
                                bf3.append(padme.substring(rightside[x].length()));
                                bf3.append(rightside[x]);
                                bf3.append(":");
                            }
                        }
                    }

                    int totalsegmentsleft = 8 - totalsegments;

                    if (totalsegmentsleft == 6) {
                        for (int x = 1; x < totalsegmentsleft; x++) {
                            bf4.append(padme);
                            bf4.append(":");
                        }
                        bf4.append("FFFF:");
                        bf4.append(v4part);
                        rettype = "4";
                        myIP2 = bf4.toString();
                    } else {
                        for (int x = 0; x < totalsegmentsleft; x++) {
                            bf4.append(padme);
                            bf4.append(":");
                        }
                        bf2.append(bf4).append(bf3);
                        myIP2 = bf2.toString().replaceAll(":$", "");
                    }
                } else {
                    // expand IPv4-compatible IPv6
                    Matcher mat2 = pattern6.matcher(myIP2);

                    if (mat2.matches()) {
                        String mymatch = mat2.group(1);
                        String[] myarr = mymatch.replaceAll("^:+", "").replaceAll(":+$", "").split(":");

                        int len = myarr.length;
                        StringBuffer bf = new StringBuffer(32);
                        for (int x = 0; x < len; x++) {
                            String unpadded = myarr[x];
                            bf.append(padme.substring(unpadded.length())).append(unpadded); // safe padding for JDK 1.4
                        }

                        long mylong = new BigInteger(bf.toString(), 16).longValue();

                        long[] b = {0, 0, 0, 0}; // using long in place of bytes due to 2's complement signed issue

                        for (int x = 0; x < 4; x++) {
                            b[x] = mylong & hexoffset;
                            mylong = mylong >> 8;
                        }

                        myIP2 = myIP2.replaceAll(mymatch + "$", ":" + b[3] + "." + b[2] + "." + b[1] + "." + b[0]);
                        myIP2 = myIP2.replaceAll("::", tmp + "FFFF:");
                        rettype = "4";
                    } else {
                        // should be normal IPv6 case
                        String[] myarr = myIP2.split("::");

                        String[] leftside = myarr[0].split(":");

                        StringBuffer bf2 = new StringBuffer(40);
                        StringBuffer bf3 = new StringBuffer(40);
                        StringBuffer bf4 = new StringBuffer(40);

                        int len = leftside.length;
                        int totalsegments = 0;
                        for (int x = 0; x < len; x++) {
                            if (leftside[x].length() > 0) {
                                totalsegments++;
                                bf2.append(padme.substring(leftside[x].length()));
                                bf2.append(leftside[x]);
                                bf2.append(":");
                            }
                        }

                        if (myarr.length > 1) {
                            String[] rightside = myarr[1].split(":");

                            len = rightside.length;
                            for (int x = 0; x < len; x++) {
                                if (rightside[x].length() > 0) {
                                    totalsegments++;
                                    bf3.append(padme.substring(rightside[x].length()));
                                    bf3.append(rightside[x]);
                                    bf3.append(":");
                                }
                            }
                        }

                        int totalsegmentsleft = 8 - totalsegments;

                        for (int x = 0; x < totalsegmentsleft; x++) {
                            bf4.append(padme);
                            bf4.append(":");
                        }

                        bf2.append(bf4).append(bf3);
                        myIP2 = bf2.toString().replaceAll(":$", "");
                    }
                }
            }
        }

        return new String[]{myIP2, rettype};
    }

    private float convertFloat(String mystr) {
        try {
            return Float.parseFloat(mystr);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }

    private void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private byte[] readRow(final long position, final long mylen, final ByteBuffer mybuffer, final FileLike filehandle) throws IOException {
        byte[] row = new byte[(int) mylen];
        if (UseMemoryMappedFile) {
            mybuffer.position((int) position);
            mybuffer.get(row, 0, (int) mylen);
        } else {
            filehandle.seek(position - 1);
            filehandle.read(row, (int) 0, (int) mylen);
        }
        return row;
    }

    private BigInteger read32or128Row(byte[] row, final int from, final int len) throws IOException {
        byte[] buf = new byte[len];
        System.arraycopy(row, from, buf, (int) 0, len);
        reverse(buf);
        return new BigInteger(1, buf);
    }

    private BigInteger read32or128(final long position, final int myiptype, final ByteBuffer mybuffer, final FileLike filehandle) throws IOException {
        if (myiptype == 4) {
            return read32(position, mybuffer, filehandle);
        } else if (myiptype == 6) {
            return read128(position, mybuffer, filehandle);
        }
        return BigInteger.ZERO;
    }

    private BigInteger read128(final long position, final ByteBuffer mybuffer, final FileLike filehandle) throws IOException {
        BigInteger retval;
        final int bsize = 16;
        byte[] buf = new byte[bsize];

        if (UseMemoryMappedFile) {
            mybuffer.position((int) position);
            mybuffer.get(buf, 0, bsize);
        } else {
            filehandle.seek(position - 1);
            filehandle.read(buf, 0, bsize);
        }
        reverse(buf);
        retval = new BigInteger(1, buf);
        return retval;
    }

    private BigInteger read32Row(byte[] row, final int from) {
        final int len = 4;
        byte[] buf = new byte[len];
        System.arraycopy(row, from, buf, (int) 0, len);
        reverse(buf);
        return new BigInteger(1, buf);
    }

    private BigInteger read32(final long position, final ByteBuffer mybuffer, final FileLike filehandle) throws IOException {
        if (UseMemoryMappedFile) {
            // simulate unsigned int by using long
            return BigInteger.valueOf(mybuffer.getInt((int) position) & 0xffffffffL); // use absolute offset to be thread-safe
        } else {
            final int bsize = 4;
            filehandle.seek(position - 1);
            byte[] buf = new byte[bsize];
            filehandle.read(buf, (int) 0, bsize);
            reverse(buf);
            return new BigInteger(1, buf);
        }
    }

    private String readStr(long position, final ByteBuffer mydatabuffer, final FileLike filehandle) throws IOException {
        int size = 256; // max size of string field + 1 byte for the length
        final int len;
        final byte[] data = new byte[size];
        byte[] buf;

        if (UseMemoryMappedFile) {
            position = position - _MapDataOffset; // position stored in BIN file is for full file, not just the mapped data segment, so need to minus
            try {
                mydatabuffer.position((int) position);
                if (mydatabuffer.remaining() < size) {
                    size = mydatabuffer.remaining();
                }
                mydatabuffer.get(data, 0, size);
                len = data[0];

                buf = new byte[len];
                System.arraycopy(data, 1, buf, (int) 0, len);

            } catch (NegativeArraySizeException e) {
                return null;
            }

        } else {
            filehandle.seek(position);
            try {
                filehandle.read(data, 0, size);
                len = data[0];

                buf = new byte[len];
                System.arraycopy(data, 1, buf, (int) 0, len);

            } catch (NegativeArraySizeException e) {
                return null;
            }
        }

        return new String(buf);
    }

    private float readFloatRow(byte[] row, final int from) {
        final int len = 4;
        byte[] buf = new byte[len];
        System.arraycopy(row, from, buf, (int) 0, len);
        return Float.intBitsToFloat((buf[3] & 0xff) << 24 | (buf[2] & 0xff) << 16 | (buf[1] & 0xff) << 8 | (buf[0] & 0xff)); // the AND is converting byte to unsigned byte in the form of an int
    }

    private String setDecimalPlaces(float myfloat) {
        return GEO_COORDINATE_FORMAT.format(myfloat);
    }

    private BigInteger[] ip2No(String ipstring) throws UnknownHostException {
        BigInteger a1;
        BigInteger a2;
        BigInteger a3 = new BigInteger("4");

        if (pattern.matcher(ipstring).matches()) { // should be IPv4
            a1 = new BigInteger("4");
            a2 = new BigInteger(String.valueOf(ipV4No(ipstring)));
        } else if (pattern2.matcher(ipstring).matches() || pattern3.matcher(ipstring).matches()) {
            throw new UnknownHostException();
        } else {
            a3 = new BigInteger("6");
            final InetAddress ia = InetAddress.getByName(ipstring);
            final byte[] byteArr = ia.getAddress();

            String myiptype = "0"; // BigInteger needs String in the constructor

            if (ia instanceof Inet6Address) {
                myiptype = "6";
            } else if (ia instanceof Inet4Address) { // this will run in cases of IPv4-mapped IPv6 addresses
                myiptype = "4";
            }
            a2 = new BigInteger(1, byteArr); // confirmed correct for IPv6

            if (a2.compareTo(FROM_6TO4) >= 0 && a2.compareTo(TO_6TO4) <= 0) {
                // 6to4 so need to remap to ipv4
                myiptype = "4";
                a2 = a2.shiftRight(80);
                a2 = a2.and(LAST_32BITS);
                a3 = new BigInteger("4");
            } else if (a2.compareTo(FROM_TEREDO) >= 0 && a2.compareTo(TO_TEREDO) <= 0) {
                // Teredo so need to remap to ipv4
                myiptype = "4";
                a2 = a2.not();
                a2 = a2.and(LAST_32BITS);
                a3 = new BigInteger("4");
            }

            a1 = new BigInteger(myiptype);
        }

        return new BigInteger[]{a1, a2, a3};
    }

    private long ipV4No(final String ipstring) {
        final String[] ipAddressInArray = ipstring.split("\\.");
        long result = 0;
        long ip;
        for (int x = 3; x >= 0; x--) {
            ip = Long.parseLong(ipAddressInArray[3 - x]);
            result |= ip << (x << 3);
        }
        return result;
    }
}
