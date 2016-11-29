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