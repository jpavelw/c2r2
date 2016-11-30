<#include "header.ftl">
    <#if errormessage??>
        <div class="bs-callout bs-callout-danger">
            <h4>${errormessage}</h4> 
            <p>${detailedmessage}</p>
        </div>
    <#else>
        <canvas id="metrics-contributors"></canvas>
        <script> window.onload = function(){ load_metrics_contrib(); } </script>
    </#if>
<#include "footer.ftl">