<#include "header.ftl">
    <#if errormessage??>
        <div class="bs-callout bs-callout-danger">
            <h4>${errormessage}</h4> 
            <p>${detailedmessage}</p>
        </div>
    <#else>
        <canvas id="metrics-contributors"></canvas>
        <div id="overlay"> <div id="loading-div"></div> </div>
        <script> window.onload = function(){ show_contrib(); } </script>
    </#if>
<#include "footer.ftl">