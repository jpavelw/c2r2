<#include "header.ftl">

    <div class="jumbotron repos-div">
        <h2 class="text-align-center">Repositories</h2>
        <#if repositories??>
            <#list repositories as repository>
                <#assign link = "/repository/${repository.repository.owner.login}/${repository.repository.name}">
                <#assign link = link?replace("\"", "")>
                <ul class="list-group">
                    <li class="list-group-item"><a href="${link}">{ "Repository name:" ${repository.repository.name}, "Owner": ${repository.repository.owner.login} }</a></li>
                </ul>
            </#list>
        </#if>
        <div id="loading-div" class="jumbotron"></div>
        <script> window.onload = function(){ show_loading(); } </script>
    </div>
    <#if errormessage??>
        <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
    </#if>
<#include "footer.ftl">