<#include "header.ftl">
    <#if errormessage??>
        <div class="bs-callout bs-callout-danger">
            <h4>${errormessage}</h4> 
            <p>${detailedmessage}</p>
        </div>
    <#else>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#div-number-methods" aria-controls="number-methods" role="tab" data-toggle="tab"># of methods</a></li>
            <li role="presentation"><a href="#div-avg-number-methods" aria-controls="avg-methods" role="tab" data-toggle="tab">avg # methods per class</a></li>
            <li role="presentation"><a href="#div-avg-number-fields" aria-controls="avg-fields" role="tab" data-toggle="tab">avg # fields per class</a></li>
            <li role="presentation"><a href="#div-number-fields" aria-controls="number-fields" role="tab" data-toggle="tab"># fields</a></li>
            <li role="presentation"><a href="#div-number-files" aria-controls="number-files" role="tab" data-toggle="tab"># files</a></li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane fade in active" id="div-number-methods"><canvas id="number-methods"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="div-avg-number-methods"><canvas id="avg-number-methods"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="div-avg-number-fields"><canvas id="avg-number-fields"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="div-number-fields"><canvas id="number-fields"></canvas></div>
            <div role="tabpanel" class="tab-pane fade" id="div-number-files"><canvas id="number-files"></canvas></div>
        </div>

        <div id="overlay"> <div id="loading-div"></div> </div>
        <script> window.onload = function(){ show_metrics_source_code(); } </script>
    </#if>
<#include "footer.ftl">