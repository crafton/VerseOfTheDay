/**
 * Created by craft on 25/03/2016.
 */

/**Scripts for create VoTD view**/
//Retrieve the verses
$("#verifyVerseButton").click(function () {
    $.ajax({
        //  url: "/votd/getverse/" + $("#verseField").val(),
        url: "/votd/getverse",
        cache: false,
        success: function (data) {
            $("#verseRetrieved").html(data);
            $("#isValidButton").show()
        },
        error: function (xhr, status, errorThrown) {
            $("#verseretrieved").html("<strong>" + xhr.responseText + "</strong> ");
            $("#isValidButton").hide()
        }
    });
});
//Show a nice loader when verses are being retrieved
jQuery.ajaxSetup({
    beforeSend: function () {
        $('#loadingDiv').show();
    },
    complete: function () {
        $('#loadingDiv').hide();
    },
    success: function () {
    }
});

/**End scripts for create VoTD view**/