package com.oauth.oauthapitest.controller;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

/*
 1. 네이버 버튼을 누르면 /login/getKakaoAuthUrl로 가서 url로 redirect 한다
 2. url에서 redirect 주소로 redirect_uri=http://localhost:8080/login/oauth_kakao를 지정했기 때문에 처리 후 해당 주소로 redirect 된다.
 3-1. /login/oauth_kakao에서의 순서는 먼저 getAccessTokne로직을 통해 Token을 발급 받는다.
 3-2. 발급받은 토큰을 UserInfo 로직에 넣고 UserInfo를 받아 JSP에 넘긴다(넘길 값이 있을 때만 선택)
 3-3. 띄우고 싶은 Page를 Return 한다.
*/

@Controller
public class NaverController {

    @RequestMapping(value = "/redirect")
    public String redirect(HttpServletRequest request) throws UnsupportedEncodingException {
        HttpSession session = request.getSession();
        String clientId = "94NPD3qYbyrYI0aElBQa";//애플리케이션 클라이언트 아이디값";
        String redirectURI = URLEncoder.encode("http://localhost:8080/NaverLogin", "UTF-8");
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString();
        String apiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectURI + "&state=" + state;
        out.println(state);
        session.setAttribute("state", state);

        return "redirect:" + apiURL;
    }

    @RequestMapping(value = "/NaverLogin")
    public String NaverLogin(HttpServletRequest request, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String accessToken = this.getAccessToken(code, state);

        String userInfo =  this.GetUserInfo(accessToken);
        out.println(userInfo);
        JSONObject jsonObject = new JSONObject(userInfo);
        String name = jsonObject.getJSONObject("response").getString("name");

        redirectAttributes.addAttribute("name", name);
        return "redirect:/directApiJSON";
    }

    public String getAccessToken (String authorize_code, String state) throws UnsupportedEncodingException {
        String clientId = "94NPD3qYbyrYI0aElBQa";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "FOG51QIERW";//애플리케이션 클라이언트 시크릿값";
        String redirectURI = URLEncoder.encode("http://localhost:8080/directApiJSON", "UTF-8");
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code";

        try {
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //  URL연결은 입출력에 사용 될 수 있고, POST 혹은 PUT 요청을 하려면 setDoOutput을 true로 설정해야함.
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //	POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("&client_id="+clientId);  //본인이 발급받은 key
            sb.append("&client_secret="+clientSecret);  //본인이 발급받은 SecretKey
            sb.append("&redirect_uri="+redirectURI);     // 본인이 설정해 놓은 경로
            sb.append("&code=" + authorize_code);
            sb.append("&state=" + state);
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

    public String GetUserInfo(String access_token) {
        String token = "access_token"; // 네이버 로그인 접근 토큰;
        String header = "Bearer " + access_token; // Bearer 다음에 공백 추가


        String apiURL = "https://openapi.naver.com/v1/nid/me";


        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        out.println(requestHeaders);
        String responseBody = get(apiURL,requestHeaders);


        return responseBody;
    }


    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }


            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }


    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }


    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);


        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();


            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }


            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

}