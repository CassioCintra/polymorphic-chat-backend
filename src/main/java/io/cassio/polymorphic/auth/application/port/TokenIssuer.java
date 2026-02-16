package io.cassio.polymorphic.auth.application.port;

public interface TokenIssuer {

    String issueAccess(String uuid, String jti);
    String issueRefresh(String uuid, String jti);

}
