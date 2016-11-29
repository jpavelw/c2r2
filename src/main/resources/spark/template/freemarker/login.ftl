<#include "header.ftl">
          <div class="jumbotron login-div">
            <form action="/user/login" method="post">
                <h2 class="text-align-center">Login</h2>
                <div class="form-group">
                    <label>Username</label>
                    <input class="form-control" type="text" name="username" autofocus placeholder="Ex. awesome-user123" required autocomplete="off">
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input class="form-control" type="password" name="password" placeholder="Your super secret password" required>
                </div>
                <p style="text-align:center !important;">
                    <button class="btn btn-default" type="submit">
                        <span class="glyphicon glyphicon-log-in" aria-hidden="true"></span> Sign in
                    </button>
                </p>
                <h4 class="text-align-center"><small><a href="#">Forgot Password</a></small></h4>
                <p class="text-align-center">New user?</p>
                <p class="text-align-center">
                    <a href="/github/togithub" class="btn btn-lg btn-social btn-github">
                        <span class="fa fa-github"></span> Sign up with GitHub
                    </a>
                </p>
            </form>
        </div>
        <#if errormessage??>
            <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
        </#if>
<#include "footer.ftl">