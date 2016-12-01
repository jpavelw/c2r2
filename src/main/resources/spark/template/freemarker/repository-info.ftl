<#include "header.ftl">
    <div class="jumbotron repo-info-div">
        <h2 class="text-align-center">Metrics</h2>

        <div class="row options-metrics">
            <#assign link = "/repository/${owner}/${repository}/releases">
            <div class="col-md-4 text-align-center"><a href="${link}"><img width="140" src="/img/github.png" alt="Repository metrics" class="img-rounded"></a></div>
            <#assign link = "/repository/${owner}/${repository}/contributors">
            <div class="col-md-4 text-align-center"><a href="${link}"><img width="140" src="/img/contributors.png" alt="Contributors metrics" class="img-rounded"></a></div>
            <#assign link = "/repository/${owner}/${repository}/sourcecode">
            <div class="col-md-4 text-align-center"><a href="${link}"><img width="140" src="/img/surcecode.png" alt="Source Code metrics" class="img-rounded"></a></div>
        </div>
        <div class="row options-metrics">
            <div class="col-md-4 text-align-center">Repository metrics</div>
            <div class="col-md-4 text-align-center">Contributors metrics</div>
            <div class="col-md-4 text-align-center">Source Code metrics</div>
        </div>
    </div>

    <#if url??>
        <div class="panel panel-default repo-panel">
            <div class="panel-heading">
                <h3 class="panel-title">Panel title</h3>
            </div>
            <div class="panel-body">
                <table class="table table-striped">
                    <tr>
                        <th>Creation Date</th>
                        <td>${date}</td>
                    </tr>
                    <tr>
                        <th>Description</th>
                        <td>${description}</td>
                    </tr>
                    <tr>
                        <th>Repo URL</th>
                        <td><a href="${url}">${url}</a></td>
                    </tr>
                    <tr>
                        <th>Owner</th>
                        <td>${owner}</td>
                    </tr>
                </table>
            </div>
        </div>
    </#if>

    <div id="overlay"> <div id="loading-div"></div> </div>
    <script> window.onload = function(){ load_metrics(); } </script>
    
    <#if errormessage??>
        <p class="bg-danger bs-callout-danger custom-alert"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> ${errormessage}</p>
    </#if>
<#include "footer.ftl">