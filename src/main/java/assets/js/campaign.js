$(function () {
	$('#startDatePicker').datetimepicker({
		format: 'LLLL',
		stepping: 60
	});
	$('#endDatePicker').datetimepicker({
		useCurrent: false, //Important! See issue #1075
		format: 'LLLL',
		stepping: 60
	});
	$("#startDatePicker").on("dp.change", function (e) {
		$('#endDatePicker').data("DateTimePicker").minDate(e.date);
	});
	$("#endDatePicker").on("dp.change", function (e) {
		$('#startDatePicker').data("DateTimePicker").maxDate(e.date);
	});
});

$(document).ready(function(){
	$('#themes').multiselect({
		disableIfEmpty: true,
		enableCaseInsensitiveFiltering: true,
		includeSelectAllOption: true,
		numberDisplayed: 6,
		nonSelectedText: 'None selected',
		maxHeight: 400
	});

	$('#campaignListTbl').DataTable({});
});
