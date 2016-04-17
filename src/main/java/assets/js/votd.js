$(document).ready(function() {
//	$('.open-startDate').click(function(event) {
//		event.preventDefault();
//		$('#startDate').click();
//	});
//	$('#startDate input').click(function(event){
//		   $('#startDate ').datepicker();
//		});
//	$('#startDate').datepicker();
});

function test() {

	var availableTags = [ "ActionScript", "AppleScript", "Asp", "BASIC", "C",
			"C++", "Clojure", "COBOL", "ColdFusion", "Erlang", "Fortran",
			"Groovy", "Haskell", "Java", "JavaScript", "Lisp", "Perl", "PHP",
			"Python", "Ruby", "Scala", "Scheme" ];

	$(".autocomplete").autocomplete({
		source : availableTags
	});
}
