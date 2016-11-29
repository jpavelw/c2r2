<#include "header.ftl">
        <div class="jumbotron signup-div">
            <form action="/user/signup" method="post">
                <h2 class="text-align-center">Sign up</h2>
                <div class="form-group">
                    <label>Username</label>
                    <input class="form-control" type="text" name="username" autofocus placeholder="Ex. awesome-user123" required autocomplete="off" value="${username}">
                </div>
                <div class="form-group">
                    <label>Name</label>
                    <input class="form-control" type="text" name="name" placeholder="Your awesome name" required value="${name}">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input class="form-control" type="email" name="email" placeholder="Your awesome email" required value="${email}">
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input class="form-control" type="password" name="password" placeholder="Your super secret password" required>
                </div>
                <div class="form-group">
                    <label>Confirm Password</label>
                    <input class="form-control" type="password" name="confirm" placeholder="Confirm your super secret password" required>
                </div>
                <p style="text-align:center !important;">
                    <button class="btn btn-default" type="submit">
                        <span class="glyphicon glyphicon-log-in" aria-hidden="true"></span> Sign up
                    </button>
                </p>
            </form>
            </div>
            <#if errormessage??>
                <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
            </#if>
<#include "footer.ftl">