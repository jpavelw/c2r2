<#include "header.ftl">
    <div class="jumbotron repo-info-div">
        <h2 class="text-align-center">Metrics</h2>
        <div id="options-metrics">
            <#assign link = "/repository/${owner}/${repository}/contributors">
            <a class="btn btn-success" href="${link}" role="button">Contributors</a>
            <#assign link = "/repository/${owner}/${repository}/releases">
            <a class="btn btn-info" href="${link}" role="button">Releases</a>
        </div>
    </div>
    <div id="overlay"> <div id="loading-div"></div> </div>
    <script> window.onload = function(){ load_metrics(); } </script>
    
    <#if errormessage??>
        <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
    <#else>
    
    </#if>
<#include "footer.ftl">