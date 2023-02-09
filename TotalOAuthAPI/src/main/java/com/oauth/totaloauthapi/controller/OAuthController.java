package com.oauth.totaloauthapi.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;

import static java.lang.System.out;

@Controller
public class OAuthController {
    String basicURL = "";
    String basicAuthURL = "authorize";
    String basicTokenURL = "token";
    String clientId = "";
    String redirectURI = "";
    String response_type = "code";
    String reqUrl = "";

    @GetMapping("/login/{type}")
    public String getAuthCode(HttpServletRequest request, @PathVariable String type, Model model) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if(type.equals("kakao")){
            basicURL="https://kauth.kakao.com/oauth/";
            clientId = "68885454f9552b677de69c7e28b15662";
            redirectURI = "http://localhost:8080/OAuth";
            sb.append(basicURL);
            sb.append(basicAuthURL+"?");
            sb.append("client_id=" + clientId);
            sb.append("&redirect_uri=" + redirectURI);
            sb.append("&response_type=" + response_type);
            reqUrl=sb.toString();

            return "redirect:" + reqUrl;
        }else if(type.equals("naver")){
            basicURL = "https://nid.naver.com/oauth2.0/";
            clientId = "94NPD3qYbyrYI0aElBQa";//애플리케이션 클라이언트 아이디값";
            redirectURI = URLEncoder.encode("http://localhost:8080/OAuth", "UTF-8");
            SecureRandom random = new SecureRandom();
            String state = new BigInteger(130, random).toString();
            sb.append(basicURL + basicAuthURL + "?");
            sb.append("client_id=" + clientId);
            sb.append("&redirect_uri=" + redirectURI);
            sb.append("&response_type=" + response_type);
            sb.append("&state=" + state);

            reqUrl= sb.toString();
            HttpSession session = request.getSession();
            session.setAttribute("state", state);

            return "redirect:" + reqUrl;
        }else if(type.equals("google")){
            basicURL = "https://accounts.google.com";
            clientId = "[구글 아이디]";
            redirectURI = "http://localhost:8080/OAuth";

            sb.append(basicAuthURL);
            sb.append("/o/oauth2/v2/auth?client_id=" + clientId);
            sb.append("&redirect_uri=" + redirectURI);
            sb.append("&response_type=" + response_type);
            sb.append("&scope=email%20profile%20openid&access_type=offline");
            reqUrl= sb.toString();

            return "redirect:" + reqUrl;
        }else {
            return "jw";
        }
    }

    @RequestMapping(value = "/OAuth")
    public String oauthKakao(
            @RequestParam(value = "code", required = false) String code
            , @RequestParam(value = "state", required = false, defaultValue = "kakao") String state
            , RedirectAttributes redirectAttributes) throws Exception {
        out.println(state);
        String accessToken = this.getAccessToken(code,state);
        out.println("access Token" + accessToken);
        HashMap<String, Object> userInfo;
        String name = "";
        if(state.equals("kakao")) {
            userInfo = getUserInfo(accessToken, "https://kapi.kakao.com/v2/user/me", "kakao");
            JSONObject jsonObject = new JSONObject(userInfo);
            name = jsonObject.getString("nickname");
        }else{
            userInfo = getUserInfo(accessToken, "https://openapi.naver.com/v1/nid/me", "naver");
            JSONObject jsonObject = new JSONObject(userInfo);
            name = jsonObject.getString("name");
            name = name.replace("\"", "");
        }

        redirectAttributes.addAttribute("name", name);
        return "redirect:/directApiJSON";
    }

    public String getAccessToken (String authorize_code, String state) throws UnsupportedEncodingException {
        out.println("getAccessToken 진입");
        String reqURL="";
        String clientSecret = "";
        if(state.equals("kakao")) {
            reqURL = basicURL+basicTokenURL;
        }else {
            clientSecret = "FOG51QIERW";//애플리케이션 클라이언트 시크릿값";
            reqURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code";
        }
        String access_Token = "";
        String refresh_Token = "";

        try {
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //  URL연결은 입출력에 사용 될 수 있고, POST 혹은 PUT 요청을 하려면 setDoOutput을 true로 설정해야함.
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //	POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            out.println("sb 진입");
            if(state.equals("kakao")) {
                sb.append("grant_type=authorization_code");
                sb.append("&client_id="+clientId);  //본인이 발급받은 key
                sb.append("&redirect_uri="+redirectURI);     // 본인이 설정해 놓은 경로
                sb.append("&code=" + authorize_code);
            }else {
                sb.append("&state=" + state);
                sb.append("&client_id=" + clientId);  //본인이 발급받은 key
                sb.append("&client_secret=" + clientSecret);  //본인이 발급받은 SecretKey
                sb.append("&redirect_uri=" + redirectURI);     // 본인이 설정해 놓은 경로
                sb.append("&code=" + authorize_code);
            }
            out.println(sb);
            bw.write(sb.toString());
            bw.flush();

            //    결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //    요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //    Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token : " + access_Token);
            System.out.println("refresh_token : " + refresh_Token);

            br.close();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return access_Token;
    }
    public HashMap<String, Object> getUserInfo (String access_Token, String reqURL, String type) {

        //    요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> userInfo = new HashMap<String, Object>();
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            //    요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + access_Token);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);
            if (type.equals("kakao")){

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
            userInfo.put("accessToken", access_Token);
            userInfo.put("nickname", nickname);

            } else if (type.equals("naver")){

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            String name = String.valueOf(element.getAsJsonObject().get("response").getAsJsonObject().get("name"));
            userInfo.put("name", name);

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return userInfo;
    }
}
