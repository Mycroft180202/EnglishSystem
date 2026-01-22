package Util;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class VietQrUtil {
    private VietQrUtil() {}

    public static final class Config {
        public final String bankId;
        public final String accountNo;
        public final String accountName;
        public final String template;

        public Config(String bankId, String accountNo, String accountName, String template) {
            this.bankId = bankId;
            this.accountNo = accountNo;
            this.accountName = accountName;
            this.template = template;
        }
    }

    public static Config loadFromSystemProperties() {
        String bankId = System.getProperty("VIETQR_BANK_ID", "MB");
        String accountNo = System.getProperty("VIETQR_ACCOUNT_NO", "0907793180202");
        String accountName = System.getProperty("VIETQR_ACCOUNT_NAME", "HOANG NGUYEN NHAT");
        String template = System.getProperty("VIETQR_TEMPLATE", "compact2");
        return new Config(bankId, accountNo, accountName, template);
    }

    public static String buildImageUrl(Config cfg, BigDecimal amount, String addInfo) {
        if (cfg == null) cfg = loadFromSystemProperties();
        String base = "https://img.vietqr.io/image/" + cfg.bankId + "-" + cfg.accountNo + "-" + cfg.template + ".png";
        String qs = "amount=" + url(amount == null ? "" : amount.toPlainString())
                + "&addInfo=" + url(addInfo == null ? "" : addInfo)
                + "&accountName=" + url(cfg.accountName == null ? "" : cfg.accountName);
        return base + "?" + qs;
    }

    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

