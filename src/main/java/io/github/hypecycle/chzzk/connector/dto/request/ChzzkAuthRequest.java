package io.github.hypecycle.chzzk.connector.dto.request;

public record ChzzkAuthRequest(
        String ver,
        int cmd,
        String svcid,
        String cid,
        int tid,
        AuthRequestBody bdy
) {

    public record AuthRequestBody(
            String uid,
            int devType,
            String devName,
            String accTkn,
            String libVer,
            String locale,
            String osVer,
            String timezone,
            String auth
    ) {}
}
