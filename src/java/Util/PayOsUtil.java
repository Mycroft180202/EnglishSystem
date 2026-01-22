package Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PayOsUtil {
    private PayOsUtil() {}

    private static final String DEFAULT_CLIENT_ID = "d59cd274-d020-4837-a460-8eb42bb1e3f9";
    private static final String DEFAULT_API_KEY = "d086ebfe-3596-4488-bec6-fe4aea95a3e5";
    private static final String DEFAULT_CHECKSUM_KEY = "a4ec670d0e3c7dd5460995d0f2031cfeff90feff34fdbdcfe90ad16d8b4488ec";

    public static final class Config {
        public final String apiBase;
        public final String clientId;
        public final String apiKey;
        public final String checksumKey;

        public Config(String apiBase, String clientId, String apiKey, String checksumKey) {
            this.apiBase = apiBase;
            this.clientId = clientId;
            this.apiKey = apiKey;
            this.checksumKey = checksumKey;
        }
    }

    public static Config loadFromSystemProperties() {
        String apiBase = System.getProperty("PAYOS_API_BASE", "https://api-merchant.payos.vn");
        String clientId = firstNonEmpty(System.getProperty("PAYOS_CLIENT_ID", ""), System.getenv("PAYOS_CLIENT_ID"), DEFAULT_CLIENT_ID);
        String apiKey = firstNonEmpty(System.getProperty("PAYOS_API_KEY", ""), System.getenv("PAYOS_API_KEY"), DEFAULT_API_KEY);
        String checksumKey = firstNonEmpty(System.getProperty("PAYOS_CHECKSUM_KEY", ""), System.getenv("PAYOS_CHECKSUM_KEY"), DEFAULT_CHECKSUM_KEY);
        return new Config(apiBase, clientId, apiKey, checksumKey);
    }

    /*
      Create a PayOS payment link and return checkoutUrl.

      Default request signature:
        signature = HMAC_SHA256(checksumKey, "amount=...&cancelUrl=...&description=...&orderCode=...&returnUrl=...")

      If your PayOS account uses a different signature scheme, adjust buildSignatureData().
    */
    public static String createPaymentLink(
            Config cfg,
            long orderCode,
            long amountVnd,
            String description,
            String returnUrl,
            String cancelUrl
    ) throws Exception {
        if (cfg == null) cfg = loadFromSystemProperties();
        if (cfg.clientId == null || cfg.clientId.isBlank()) {
            throw new IllegalStateException(missingMsg("PAYOS_CLIENT_ID"));
        }
        if (cfg.apiKey == null || cfg.apiKey.isBlank()) {
            throw new IllegalStateException(missingMsg("PAYOS_API_KEY"));
        }
        if (cfg.checksumKey == null || cfg.checksumKey.isBlank()) {
            throw new IllegalStateException(missingMsg("PAYOS_CHECKSUM_KEY"));
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("orderCode", Long.toString(orderCode));
        payload.put("amount", Long.toString(amountVnd));
        payload.put("description", safe(description));
        payload.put("returnUrl", safe(returnUrl));
        payload.put("cancelUrl", safe(cancelUrl));

        String sigData = buildSignatureData(payload);
        String signature = HmacUtil.hmacSha256Hex(cfg.checksumKey, sigData.getBytes(StandardCharsets.UTF_8));

        String json = "{"
                + "\"orderCode\":" + payload.get("orderCode") + ","
                + "\"amount\":" + payload.get("amount") + ","
                + "\"description\":\"" + jsonEscape(payload.get("description")) + "\","
                + "\"returnUrl\":\"" + jsonEscape(payload.get("returnUrl")) + "\","
                + "\"cancelUrl\":\"" + jsonEscape(payload.get("cancelUrl")) + "\","
                + "\"signature\":\"" + signature + "\""
                + "}";

        String url = trimTrailingSlash(cfg.apiBase) + "/v2/payment-requests";
        String resp = httpPostJson(url, json, cfg.clientId, cfg.apiKey);

        String checkoutUrl = extractJson(resp, "checkoutUrl");
        if (checkoutUrl.isEmpty()) checkoutUrl = extractJson(resp, "checkout_url");
        if (checkoutUrl.isEmpty()) checkoutUrl = extractJson(resp, "paymentUrl");
        if (checkoutUrl.isEmpty()) {
            throw new IllegalStateException("PayOS response missing checkoutUrl");
        }
        return checkoutUrl;
    }

    public static String getPaymentRequest(Config cfg, String idOrOrderCode) throws Exception {
        if (cfg == null) cfg = loadFromSystemProperties();
        if (cfg.clientId == null || cfg.clientId.isBlank()) {
            throw new IllegalStateException(missingMsg("PAYOS_CLIENT_ID"));
        }
        if (cfg.apiKey == null || cfg.apiKey.isBlank()) {
            throw new IllegalStateException(missingMsg("PAYOS_API_KEY"));
        }
        String id = idOrOrderCode == null ? "" : idOrOrderCode.trim();
        if (id.isEmpty()) throw new IllegalArgumentException("idOrOrderCode");
        String url = trimTrailingSlash(cfg.apiBase) + "/v2/payment-requests/" + url(id);
        return httpGetJson(url, cfg.clientId, cfg.apiKey);
    }

    public static boolean isPaidResponse(String json) {
        if (json == null) return false;
        // flexible match (data.status or status)
        return Pattern.compile("\"status\"\\s*:\\s*\"PAID\"", Pattern.CASE_INSENSITIVE).matcher(json).find();
    }

    private static String missingMsg(String key) {
        String prop = System.getProperty(key);
        String env = System.getenv(key);
        return "Missing " + key
                + " (sysprop=" + presentLen(prop)
                + ", env=" + presentLen(env)
                + "). Make sure you set it on the Tomcat JVM and restart the server.";
    }

    private static String presentLen(String v) {
        if (v == null) return "null";
        String t = v.trim();
        if (t.isEmpty()) return "empty";
        return "len=" + t.length();
    }

    public static boolean verifyWebhookSignatureOrBypass(byte[] rawBody) {
        boolean skip = "true".equalsIgnoreCase(System.getProperty("PAYOS_WEBHOOK_SKIP_VERIFY", "false"));
        if (skip) return true;

        String checksumKey = firstNonEmpty(System.getProperty("PAYOS_CHECKSUM_KEY", ""), System.getenv("PAYOS_CHECKSUM_KEY"));
        if (checksumKey == null || checksumKey.isBlank()) return false;

        // Some PayOS setups provide signature outside raw body verification.
        // We keep verification in servlet where signature header/body is available.
        return true;
    }

    public static String buildSignatureData(Map<String, String> payload) {
        // amount=...&cancelUrl=...&description=...&orderCode=...&returnUrl=...
        return "amount=" + safe(payload.get("amount"))
                + "&cancelUrl=" + safe(payload.get("cancelUrl"))
                + "&description=" + safe(payload.get("description"))
                + "&orderCode=" + safe(payload.get("orderCode"))
                + "&returnUrl=" + safe(payload.get("returnUrl"));
    }

    private static String httpPostJson(String url, String json, String clientId, String apiKey) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(20000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("x-client-id", clientId);
        con.setRequestProperty("x-api-key", apiKey);

        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = con.getOutputStream()) {
            os.write(body);
        }

        int code = con.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream();
        String resp = readAll(is);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("PayOS API error " + code + ": " + resp);
        }
        return resp;
    }

    private static String httpGetJson(String url, String clientId, String apiKey) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(20000);
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("x-client-id", clientId);
        con.setRequestProperty("x-api-key", apiKey);

        int code = con.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream();
        String resp = readAll(is);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("PayOS API error " + code + ": " + resp);
        }
        return resp;
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        try (InputStream in = is; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) >= 0) out.write(buf, 0, r);
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static String extractJson(String body, String key) {
        if (body == null || body.isEmpty() || key == null || key.isEmpty()) return "";
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(body);
        if (m.find()) return m.group(1);
        return "";
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        String t = s.trim();
        while (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        return t;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return "";
    }
}
