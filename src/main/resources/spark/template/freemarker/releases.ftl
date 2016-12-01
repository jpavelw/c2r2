<#include "header.ftl">
    <#if errormessage??>
        <div class="bs-callout bs-callout-danger">
            <h4>${errormessage}</h4> 
            <p>${detailedmessage}</p>
        </div>
    <#else>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#releases" aria-controls="releases" role="tab" data-toggle="tab">Releases</a></li>
            <li role="presentation"><a href="#loc-added" aria-controls="loc-added" role="tab" data-toggle="tab">LOC Added</a></li>
            <li role="presentation"><a href="#loc-removed" aria-controls="loc-removed" role="tab" data-toggle="tab">LOC Removed</a></li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane fade in active" id="releases"><canvas id="metrics-releases"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="loc-added"><canvas id="metrics-loc-added"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="loc-removed"><canvas id="metrics-loc-removed"></canvas></div>
        </div>

        <div id="overlay"> <div id="loading-div"></div> </div>
        <script> window.onload = function(){ show_metrics_releases(); } </script>
    </#if>
<#include "footer.ftl">