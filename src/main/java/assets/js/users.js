/**
 * Created by craft on 9/07/2016.
 */

$('#usersTable').confirmation({
    selector: '[data-toggle="confirmation"]',
    href: function (elem) {
        return $(elem).attr('href');
    }
});

$(document).ready(function () {
    $('#usersTable').DataTable({
        "ordering": false,
        "processing": true,
        "serverSide": true,
        "ajax": "/user/displayusers"
    });
});

//Capture modal event and popular with user info
$('#updateRolesModal').on('show.bs.modal', function (event) {
    var itemClicked = $(event.relatedTarget)
    var recipient = itemClicked.data('username')
    var userid = itemClicked.data('userid')
    $('#userId').text(userid)
    var modal = $(this)

    //Clear existing modal
    modal.find('.modal-title').text('Loading...')
    modal.find('.modal-body').html('')

    $.ajax({
        url: "/user/displayuserroles/" + encodeURIComponent(userid),
        cache: false,
        beforeSend: function () {
            $('#loadingDiv').show();
        },
        complete: function () {
            $('#loadingDiv').hide();
        },
        success: function (data) {
            modal.find('.modal-title').text('Update roles for ' + recipient)
            modal.find('.modal-body').html(data)
        },
        error: function (xhr, status, errorThro) {
            modal.find('.modal-title').text('Role data not found')
            modal.find('.modal-body').text(xhr.responseText)
        }
    });

});

$('#saveRolesUpdate').on('click', function (event) {

    var checkedValues = []

    if ($('#member').prop('checked')) {
        checkedValues.push('member')
    }
    if ($('#contributor').prop('checked')) {
        checkedValues.push('contributor')
    }
    if ($('#publisher').prop('checked')) {
        checkedValues.push('publisher')
    }

    $.ajax({
        url: "/user/updateroles/" + encodeURIComponent($('#userId').text()) + "/" + encodeURIComponent(checkedValues),
        cache: false,
        beforeSend: function () {
            $('#loadingText').show()
            $('#savingRoles').text('Updating...')
            $('#loadingDiv').show()
        },
        complete: function () {
            window.setTimeout(function(){$('#loadingText').hide()}, 500)
        },
        success: function (data) {
            $('#savingRoles').text('Saved')
            $('#loadingDiv').hide();
            $('#usersTable').DataTable().ajax.reload()
            window.setTimeout(function(){
                    $('#updateRolesModal').modal('toggle')},
                1200)
        },
        error: function (xhr, status, errorThro) {

        }
    });
});