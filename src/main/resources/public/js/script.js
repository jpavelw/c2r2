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

function show_loading(){
    var opts = {
        lines: 13 // The number of lines to draw
        , length: 28 // The length of each line
        , width: 14 // The line thickness
        , radius: 42 // The radius of the inner circle
        , scale: 1 // Scales overall size of the spinner
        , corners: 1 // Corner roundness (0..1)
        , color: '#000' // #rgb or #rrggbb or array of colors
        , opacity: 0.25 // Opacity of the lines
        , rotate: 0 // The rotation offset
        , direction: 1 // 1: clockwise, -1: counterclockwise
        , speed: 1 // Rounds per second
        , trail: 60 // Afterglow percentage
        , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex: 2e9 // The z-index (defaults to 2000000000)
        , className: 'spinner' // The CSS class to assign to the spinner
        , top: '50%' // Top position relative to parent
        , left: '50%' // Left position relative to parent
        , shadow: false // Whether to render a shadow
        , hwaccel: false // Whether to use hardware acceleration
        , position: 'absolute' // Element positioning
    };

    var target = document.getElementById('loading-div');
    var spinner = new Spinner(opts).spin(target);
    $("#overlay").show();
};

function load_metrics_contrib(){

    show_loading();

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
            $("#overlay").hide();
            }	else {
                $("#overlay").hide();
                alert("error not 200 - " + data.message);
            }
        }, error: function(data) {
            $("#overlay").hide();
            alert("error - " + data.message);
        }
    });
}

function show_releases(_color, data){
    var ctx = $("#metrics-releases");
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.tag_name,
            datasets: [{
                data: data.commits_per_release,
                borderWidth: 1,
                borderColor: _color + '1)',
                backgroundColor: _color + '0.2)'
            }]
        },
        options: {
            hover: { mode: 'label' },
            legend: {
                display: false,
            },
            title: {
                display: true,
                text: '# of commits per release'
            },
        }
    });
}

function show_loc_added(_color, data){
    var ctx = $("#metrics-loc-added");
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.tag_name,
            datasets: [{
                data: data.release_additions,
                borderWidth: 1,
                borderColor: _color + '1)',
                backgroundColor: _color + '0.2)'
            }]
        },
        options: {
            hover: { mode: 'label' },
            legend: {
                display: false,
            },
            title: {
                display: true,
                text: '# of lines of code added per release'
            },
        }
    });
}

function show_loc_removed(_color, data){
    var ctx = $("#metrics-loc-removed");
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.tag_name,
            datasets: [{
                data: data.release_deletions,
                borderWidth: 1,
                borderColor: _color + '1)',
                backgroundColor: _color + '0.2)'
            }]
        },
        options: {
            hover: { mode: 'label' },
            legend: {
                display: false,
            },
            title: {
                display: true,
                text: '# of lines of code removed per release'
            },
        }
    });
}

function load_metrics_releases_loc(){
    show_loading();

    var ctx = $("#metrics-releases");
    var path = window.location.pathname;
    var vars = path.split("/")
    var _link = "/api/" + vars[2] + "/" + vars[3] + "/releases";

    backgroundColors = [
        'rgba(255, 99, 132, ',
        'rgba(54, 162, 235, ',
        'rgba(255, 206, 86, ',
        'rgba(75, 192, 192, ',
        'rgba(153, 102, 255, ',
        'rgba(255, 159, 64, ',
        'rgba(81, 192, 191, '
    ];
    var index = Math.floor(Math.random() * 3);
    var _color1 = backgroundColors[index];
    var _color2 = backgroundColors[index+1];
    var _color3 = backgroundColors[index+2];

    $.ajax({
        type: "GET",
        dataType: "json",
        url: _link,
        success: function(data) {
            if(data.statusCode == 200){
                show_releases(_color1, data);
                show_loc_added(_color2, data);
                show_loc_removed(_color3, data);
                $("#overlay").hide();
            }	else {
                $("#overlay").hide();
                alert("error not 200 - " + data.message);
            }
        }, error: function(data) {
            $("#overlay").hide();
            alert("error - " + data.message);
        }
    });
}