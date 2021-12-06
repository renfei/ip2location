package net.renfei.ip2location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Http {
    public static String get(URL url) {
        try {
            System.setProperty("https.protocols", "TLSv1.2");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                return ("Failed : HTTP error code : " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringBuilder resultFromHttp = new StringBuilder();
            while ((output = br.readLine()) != null) {
                resultFromHttp.append(output).append("\n");
            }

            br.close();
            conn.disconnect();
            return resultFromHttp.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
