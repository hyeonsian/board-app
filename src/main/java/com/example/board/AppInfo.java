package com.example.board;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * "지금 이 응답은 어느 버전의 앱이 어느 서버에서 만들었는가"를 알려 주는 정보.
 * 배포가 실제로 반영됐는지, 로드밸런서가 어느 WAS 로 보냈는지 눈으로 확인하는 용도입니다.
 */
@Component
public class AppInfo {

    private final String version;
    private final String build;
    private final String commit;
    private final String hostName;
    private final Path maintenanceFile;

    public AppInfo(@Value("${app.version:dev}") String version,
                   @Value("${app.build:local}") String build,
                   @Value("${app.commit:none}") String commit,
                   @Value("${app.maintenance-file:/etc/boardapp/maintenance}") String maintenanceFile) {
        this.version = version;
        this.build = build;
        this.commit = commit;
        this.maintenanceFile = Path.of(maintenanceFile);
        this.hostName = resolveHostName();
    }

    private static String resolveHostName() {
        try {
            Path p = Path.of("/proc/sys/kernel/hostname");
            if (Files.isReadable(p)) {
                return Files.readString(p).trim();
            }
        } catch (IOException ignored) {
            // 아래 방법으로 재시도
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /** 이 파일이 있으면 /ready 가 503 을 돌려주어 로드밸런서가 이 서버를 빼게 됩니다 (무중단 배포용) */
    public boolean isDraining() {
        return Files.exists(maintenanceFile);
    }

    public String getVersion() { return version; }
    public String getBuild() { return build; }
    public String getCommit() { return commit; }
    public String getHostName() { return hostName; }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("version", version);
        m.put("build", build);
        m.put("commit", commit);
        m.put("host", hostName);
        return m;
    }
}
