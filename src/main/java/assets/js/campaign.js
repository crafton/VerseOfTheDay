$(document).ready(function(){
	$('#campaignListTbl').DataTable({});
	
 	$(".startDatePicker").datetimepicker({
 		format : "M dd, yyyy hh:ii",
 		todayHighlight: true,
 		autoclose: true,
 		startDate : new Date()
 	}).on('outOfRange', function(){
 		alert('Please select start date greater than current date and time.');	
 	});
 	
 	$(".endDatePicker").datetimepicker({
 		format : "M dd, yyyy hh:ii",
 		todayHighlight: true,
 		autoclose: true,
 		startDate : new Date()
 	}).on('changeDate', function(){
 		checkSelectedDateRange();
 	});
 	
 	$('#themes').multiselect({
        disableIfEmpty: true,
        enableCaseInsensitiveFiltering: true,
        includeSelectAllOption: true,
        numberDisplayed: 6,
        nonSelectedText: 'Select a theme!',
        maxHeight: 400
	});
 	
});

function checkSelectedDateRange(){
 		var stDate = new Date($(".startDatePicker").val()).getTime();
 		var enDate = new Date($(".endDatePicker").val()).getTime();

 		if(!stDate || !enDate){
 			return false;
 		}
 		if(stDate > enDate)
		{
			alert('Please select end date greater than Start Date.'+$(".startDatePicker").val());
			return false;
		}else{
		  	var c = 24*60*60*1000;
            var diffDays = Math.round(Math.abs((stDate -enDate)/(c)));
            $('#campaignDays').val(diffDays);
        }
}

function onFormSubmit(){
	var selectedOptions = $('#themes option:selected');
	if (selectedOptions.length < 1) {
		alert('Please select at least one theme.');
		return false;
	}
	return checkSelectedDateRange();
}
