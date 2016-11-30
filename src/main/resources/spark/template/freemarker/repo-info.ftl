<#include "header.ftl">
          <div class="jumbotron repo-div">
            <form action="/repository/add" method="post">
                <h2 class="text-align-center">Repository Information</h2>
                <div class="form-group">
                    <label>Username</label>
                    <input class="form-control repoDetailInfo" type="text" name="username" autofocus placeholder="GitHub username">
                </div>
                <div class="form-group">
                    <label>Repository</label>
                    <input class="form-control repoDetailInfo" type="text" name="repository" placeholder="Repository name">
                </div>
                <p class="text-align-center">or</p>
                <div class="form-group">
                    <label>Repository Link</label>
                    <input id="repoLink" class="form-control disable-color" type="text" name="link" placeholder="Link to the GitHub repository. Ex. github/username/repository">
                </div>
                <p style="text-align:center !important;">
                    <button class="btn btn-default" type="submit">
                        <span class="glyphicon glyphicon-tasks" aria-hidden="true"></span> Analyze
                    </button>
                </p>
                <input type="hidden" name="choice" id="sourceChoice" value="detailed"/>
            </form>
        </div>
        <#if errormessage??>
            <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
        </#if>
<#include "footer.ftl">