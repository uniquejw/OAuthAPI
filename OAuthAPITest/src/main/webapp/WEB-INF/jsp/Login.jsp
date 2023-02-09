<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.security.SecureRandom" %>
<%@ page import="java.math.BigInteger" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65" crossorigin="anonymous">
    <meta name="viewport" content="width=device-width,initial-scale=1">
</head>
<body>
<div class="container">
    <div class="row" style="margin-bottom: 30vh"></div>
    <div class="row">
        <div class="col-3"></div>
        <div class="col-6">
            <hr>
        </div>
        <div class="col-3"></div>
    </div>
    <div class="row">
        <div class="col"></div>
        <div class="col" style="width: 20vw">
            <div class="mb-3">
                <label for="InputId" class="form-label">Id</label>
                <input type="email" class="form-control" id="InputId" placeholder="id">
            </div>
            <div class="mb-3">
                <label for="InputPw" class="form-label">Pw</label>
                <input type="email" class="form-control" id="InputPw" placeholder="pw">
            </div>
        </div>
        <div class="col"></div>
    </div>
    <div class="row">
        <div class="col-3"></div>
        <div class="col-6">
            <hr>
        </div>
        <div class="col-3"></div>
    </div>
    <div class="row">
        <div class="col"></div>
        <div class="col">
            <div class="row">
                <div class="d-flex justify-content-center">
                <a href="/login/getKakaoAuthUrl" >
                    <img style="height: 40px; width: 208px; margin-bottom: 10px" src="../../img/kakao_login_medium_wide.png"/>
                </a>
                </div>
            </div>
            <div class="row">
                <div class="d-flex justify-content-center">
                <a href="/redirect"><img style="height: 40px; width: 208px; margin-bottom: 10px" src="../../img/btnG.png"/></a>
                </div>
            </div>
            <div class="row">
                <script src="https://accounts.google.com/gsi/client" async defer></script>
                <div id="g_id_onload"
                     data-client_id="140816176832-kfeh08iqecrtjce369l8tdj318s1u3pe.apps.googleusercontent.com"
                     data-login_uri="http://localhost:8080/login/getGoogleAuthUrl"
                     data-auto_prompt="false">
                </div>
                <div class="g_id_signin d-flex justify-content-center"
                     data-type="standard"
                     data-size="large"
                     data-theme="outline"
                     data-text="sign_in_with"
                     data-shape="rectangular"
                     data-logo_alignment="center"
                >
                </div>
            </div>
        </div>
        <div class="col">
            <div class="row">
            </div>
        </div>
    </div>
</div>
</body>
<script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
</body>
</html>