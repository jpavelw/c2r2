$(document).ready(function($) {
    $('#repoLink').click(function(){
        $('#repoLink').removeClass( 'disable-color' );
        $('.repoDetailInfo').addClass( 'disable-color' );
        $('#sourceChoice').val('link');
    });

    $('.repoDetailInfo').click(function(){
        $('#repoLink').addClass( 'disable-color' );
        $('.repoDetailInfo').removeClass( 'disable-color' );
        $('#sourceChoice').val('detailed');
    });

});

function load_metrics_contrib(){
    var ctx = $("#metrics-contributors");
    var path = window.location.pathname;
    var vars = path.split("/")
    var _link = "/api/" + vars[2] + "/" + vars[3] + "/contributors";

    $.ajax({
        type: "GET",
        dataType: "json",
        url: _link,
        success: function(data) {
            if(data.statusCode == 200){
                var entries = data.login.length;
                var backgroundColor = [];
                var borderColor = [];

                for (i = 0; i < entries; i++) {
                    r = Math.floor(Math.random() * 200);
                    g = Math.floor(Math.random() * 200);
                    b = Math.floor(Math.random() * 200);
                    c = 'rgba(' + r + ', ' + g + ', ' + b + ', 0.2)';
                    b = 'rgba(' + (r+20) + ', ' + (g+20) + ', ' + (b+20) + ', 1)';
                    backgroundColor.push(c);
                    borderColor.push(b);
                };

                var myChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.login,
                    datasets: [{
                        data: data.contributions,
                        backgroundColor: backgroundColor,
                        borderColor: borderColor,
                        borderWidth: 1
                    }]
                },
                options: {
                    hover: { mode: 'label' },
                    legend: {
                        display: false,
                    },
                    title: {
                        display: true,
                        text: '# of commits'
                    },
                    scales: {
                        xAxes: [{
                            display: false
                        }]
                    },
                    scaleShowVerticalLines: false
                }
            });

            }	else {
                alert("error not 200 - " + data.message);
            }
        }, error: function(data) {
            alert("error - " + data.message);
        }
    });
}