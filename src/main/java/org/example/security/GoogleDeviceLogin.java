package org.example.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

public class GoogleDeviceLogin {
    private static final String DEVICE_CODE_URL = "https://oauth2.googleapis.com/device/code";
    private static final String TOKEN_URL       = "https://oauth2.googleapis.com/token";
    private static final String TOKENINFO_URL   = "https://oauth2.googleapis.com/tokeninfo";

    private final String clientId;
    private final String clientSecret;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public GoogleDeviceLogin(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static class DeviceStep {
        public final String deviceCode, userCode, verificationUrl, verificationUrlComplete;
        public final int interval, expiresIn;
        DeviceStep(String dc, String uc, String v, String vc, int i, int e) {
            deviceCode = dc; userCode = uc; verificationUrl = v; verificationUrlComplete = vc;
            interval = i; expiresIn = e;
        }
    }
    public static class TokenResult {
        public final String idToken, accessToken;
        TokenResult(String id, String at) { idToken = id; accessToken = at; }
    }

    //pedir device_code/user_code
    public DeviceStep start(String scope) throws Exception {
        String body = "client_id=" + encode(clientId, UTF_8) +
                "&scope="     + encode(scope, UTF_8);
        HttpRequest req = HttpRequest.newBuilder(URI.create(DEVICE_CODE_URL))
                .header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) throw new RuntimeException("Device code error: " + res.body());

        JsonNode j = om.readTree(res.body());
        return new DeviceStep(
                j.get("device_code").asText(),
                j.get("user_code").asText(),
                (j.has("verification_url") ? j.get("verification_url").asText() : j.get("verification_uri").asText()),
                j.has("verification_url_complete") ? j.get("verification_url_complete").asText() : null,
                j.get("interval").asInt(5),
                j.get("expires_in").asInt()
        );
    }

    //poll al token endpoint hasta obtener id_token
    public TokenResult pollForTokens(String deviceCode, int intervalSeconds) throws Exception {
        long deadline = Instant.now().plusSeconds(900).getEpochSecond(); // backup 15 min
        while (Instant.now().getEpochSecond() < deadline) {
            StringBuilder sb = new StringBuilder()
                    .append("client_id=").append(encode(clientId, UTF_8))
                    .append("&device_code=").append(encode(deviceCode, UTF_8))
                    .append("&grant_type=").append(encode("urn:ietf:params:oauth:grant-type:device_code", UTF_8));
            if (clientSecret != null && !clientSecret.isBlank())
                sb.append("&client_secret=").append(encode(clientSecret, UTF_8));

            HttpRequest req = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(sb.toString())).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                JsonNode j = om.readTree(res.body());
                String idToken = j.has("id_token") ? j.get("id_token").asText() : null;
                String accessToken = j.has("access_token") ? j.get("access_token").asText() : null;
                if (idToken != null) return new TokenResult(idToken, accessToken);
            } else {
                JsonNode err = om.readTree(res.body());
                String code = err.has("error") ? err.get("error").asText() : ("HTTP " + res.statusCode());
                if ("authorization_pending".equals(code)) {
                    Thread.sleep(intervalSeconds * 1000L);
                    continue;
                } else if ("slow_down".equals(code)) {
                    intervalSeconds += 5;
                    Thread.sleep(intervalSeconds * 1000L);
                    continue;
                } else {
                    throw new RuntimeException("Token error: " + code + " - " + res.body());
                }
            }
        }
        throw new RuntimeException("Timeout esperando autorización de Google.");
    }

    //validación  llamando a tokeninfo
    public JsonNode validateIdToken(String idToken) throws Exception {
        URI uri = URI.create(TOKENINFO_URL + "?id_token=" + encode(idToken, UTF_8));
        HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new RuntimeException("tokeninfo error: " + res.body());
        return om.readTree(res.body());
    }
}
