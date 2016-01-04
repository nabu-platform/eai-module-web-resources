/**
@param id The id where to draw it
@param callback the function that is called when a day is selected, the interface should be: (date)
		where date is a Date object that references the day that was chosen
@param date An optional date field that should be the default value for this calendar
*/

function Calendar(element, callback, initialDate) {
	var self = this;
	this.date = initialDate ? initialDate : new Date();
	this.callback = callback;
	this.element = element;
	
	// set the timestamp
	this.element.setAttribute("timestamp", this.date.getTime());
	
	// initialize span
	this.input = document.createElement("input");
	this.input.setAttribute("type", "text");
	this.input.setAttribute("class", "calendar_current");
	this.input.onclick = function(e) {
		self.toggleCalendar(e);
	};
	this.input.value = this.date.toLocaleDateString();
	this.element.appendChild(this.span);
	
	// add an invisible calendar to the dom
	this.calendar = document.createElement("div");
	this.calendar.setAttribute("style", "position:absolute;display:none");
	document.body.appendChild(this.calendar);
	
	this.toggleCalendar = function(event) {
		if (self.calendar.style.display == "block") {
			self.calendar.style.display = "none";
			self.input.value = new Date(parseInt(self.element.getAttribute("timestamp"))).toLocaleDateString();
		}
		else {
			self.calendar.style.display = "block";
			self.calendar.style.left = event.clientX + "px";
			// offset height so it doesn't float over the clickable part
			self.calendar.style.top = (event.clientY + 20) + "px";
			// sometimes there are problems with zindex, so set it real high
			self.calendar.style.zIndex = 1000;
		}
	};
	
	// initialize calendar content
	// create the table
	this.table = document.createElement("table");
	this.table.setAttribute("class", "calendar_popup");
	this.table.setAttribute("cellspacing", "0");
	this.table.setAttribute("cellpadding", "0");
	var header = document.createElement("tr");
	// TODO
	
	// display the month/year and the back/forth buttons
	html += "<tr><td colspan='2' align='left'><a href=\"javascript:browse_month('" + id + "', -1)\" class='calendar_browse'>&lt;&lt; Previous</a></td><td class='calendar_month_name' align='center' colspan='3'>" + month_to_string(date.getMonth()) + " " + date.getFullYear() + "</td><td align='right' colspan='2'><a class='calendar_browse' href=\"javascript:browse_month('" + id + "', 1)\">Next &gt;&gt;</a></td></tr>";
	// display the day names
	html += "<tr class='calendar_day_names'>";
	for (var i = 0; i < 7; i++)
		html += "<td width='14%' class='calendar_day_name'>" + day_to_string(i) + "</td>";
	html += "</tr>";
	// draw empty cells for the weekdays that do not fall in this month in the first week
	// get the first day of the month
	var first = first_day(date);
	var days = amount_of_days_in_month(date);

	// for every week
	for (var i = 0; i < amount_of_weeks_in_month(date); i++) {
		html += "<tr class='calendar_week'>";
		// for every day
		for (var j = 0; j < 7; j++) {
			var tmp = j + (7 * i)
			if (tmp >= day_of_week(first) && tmp < day_of_week(first) + days)
				html += "<td class='calendar_day'><a class='calendar_day' href=\"javascript:select('" + id + "', " + date.getTime() + ", " + (tmp - day_of_week(first)) + ")\">" + (tmp - day_of_week(first) + 1) + "</a></td>";
			else
				html += "<td class='calendar_empty_day'></td>";
		}
		html += "</tr>";
	}
	html += "</table>";
	
	return html;
}

var calendar_callbacks = [];

function draw_calendar(id, callback, date) {
	if (!date)
		date = new Date();
		
	// register the callback for this calendar
	calendar_callbacks[id] = callback;

	// set the timestamp for the field
	$(id).setAttribute("timestamp", date.getTime());

	// now draw the calendar field that shows the currently selected date
	$(id).innerHTML = "<span class='calendar_current_selection' style='cursor:pointer' id='calendar_" + id + "_value' onclick=\"toggle_calendar('" + id + "', event)\">" + date.toLocaleDateString() + " <b>[+]</b></span>";
	
	// draw a hidden calendar on the body (so it can move freely) which can be activated when necessary
	document.body.innerHTML += "<div style='position:absolute;display:none' id='calendar_" + id + "'></div>";
}

// if the calendar has been set on a field, you can get the calendar date with this one
function get_calendar_date(id) {
	return $(id).getAttribute("timestamp") == null ? null : new Date(parseInt($(id).getAttribute("timestamp")));
}

function toggle_calendar(id, event) {
	// IE
	if (!event)
		event = window.event;
	var date = $(id).getAttribute("timestamp") == null ? new Date() : new Date(parseInt($(id).getAttribute("timestamp")));
	// hide calendar if it is currently being shown
	if ($("calendar_" + id).style.display == "block") {
		$("calendar_" + id).style.display = "none";
		$("calendar_" + id + "_value").innerHTML = date.toLocaleDateString() + " <b>[+]</b>";
	}
	// otherwise, first draw the calendar properly, then show it
	else {
		var date = new Date(parseInt($(id).getAttribute("timestamp")));
		$("calendar_" + id).innerHTML = build_calendar(id, date);
		$("calendar_" + id).style.display = "block";
		$("calendar_" + id).style.position = "absolute";
		$("calendar_" + id).style.left = event.clientX + "px";
		// offset height so it doesn't float over the clickable part
		$("calendar_" + id).style.top = (event.clientY + 20) + "px";
		// sometimes there are problems with zindex, so set it real high
		$("calendar_" + id).style.zIndex = 1000;
		$("calendar_" + id + "_value").innerHTML = date.toLocaleDateString() + " <b>[-]</b>";
	}
}

function browse_month(id, offset) {
	// get the timestamp stored in the calendar, or a new one if none is stored
	var date = $(id).getAttribute("timestamp") == null ? new Date() : new Date(parseInt($(id).getAttribute("timestamp")));
	// create a new timestamp, use the natural overflow mechanism
	date = new Date(date.getFullYear(), date.getMonth() + offset, 1, 0, 0, 0, 0);
	// set the attribute
	$(id).setAttribute("timestamp", date.getTime());
	// redraw the calendar
	$("calendar_" + id).innerHTML = build_calendar(id, date);
}

// gets called with the id of the element that has the calendar, the timestamp of the month it is in, and the day (0-based) of the month
function select(id, timestamp, day) {
	var date = new Date(timestamp);
	date = new Date(date.getFullYear(), date.getMonth(), day + 1, 0, 0, 0, 0);
	$(id).setAttribute("timestamp", date.getTime());
	// hide calendar
	toggle_calendar(id);
	// call the callback if registered
	if (calendar_callbacks[id])
		calendar_callbacks[id](date);
}

function build_calendar(id, date) {
	var html = "<table class='calendar' cellspacing='0' cellpadding='0'>";

	// display the month/year and the back/forth buttons
	html += "<tr><td colspan='2' align='left'><a href=\"javascript:browse_month('" + id + "', -1)\" class='calendar_browse'>&lt;&lt; Previous</a></td><td class='calendar_month_name' align='center' colspan='3'>" + month_to_string(date.getMonth()) + " " + date.getFullYear() + "</td><td align='right' colspan='2'><a class='calendar_browse' href=\"javascript:browse_month('" + id + "', 1)\">Next &gt;&gt;</a></td></tr>";
	// display the day names
	html += "<tr class='calendar_day_names'>";
	for (var i = 0; i < 7; i++)
		html += "<td width='14%' class='calendar_day_name'>" + day_to_string(i) + "</td>";
	html += "</tr>";
	// draw empty cells for the weekdays that do not fall in this month in the first week
	// get the first day of the month
	var first = first_day(date);
	var days = amount_of_days_in_month(date);

	// for every week
	for (var i = 0; i < amount_of_weeks_in_month(date); i++) {
		html += "<tr class='calendar_week'>";
		// for every day
		for (var j = 0; j < 7; j++) {
			var tmp = j + (7 * i)
			if (tmp >= day_of_week(first) && tmp < day_of_week(first) + days)
				html += "<td class='calendar_day'><a class='calendar_day' href=\"javascript:select('" + id + "', " + date.getTime() + ", " + (tmp - day_of_week(first)) + ")\">" + (tmp - day_of_week(first) + 1) + "</a></td>";
			else
				html += "<td class='calendar_empty_day'></td>";
		}
		html += "</tr>";
	}
	html += "</table>";
	
	return html;
}

/**
the day starts at "sunday" for javascript, shift this to the more sane default of monday
*/
function day_of_week(date) {
	if (date.getDay() == 0)
		return 6;
	else
		return date.getDay() - 1;
}

function amount_of_days_in_month(date) {
	// overflow the date object
	var tmp = new Date(date.getFullYear(), date.getMonth(), 32, 0, 0, 0, 0);
	// substract the overflow from the original amount
	return 32 - tmp.getDate();
}

function amount_of_weeks_in_month(date) {
	var days = amount_of_days_in_month(date);
	var first = first_day(date);
	return Math.ceil((day_of_week(first) + days) / 7);
}

function first_day(date) {
	return new Date(date.getFullYear(), date.getMonth(), 1, 0, 0, 0, 0);
}

function month_to_string(month) {
	switch(month) {
		case 0: return "Jan"; break;
		case 1: return "Feb"; break;
		case 2: return "Mar"; break;
		case 3: return "Apr"; break;
		case 4: return "May"; break;
		case 5: return "Jun"; break;
		case 6: return "Jul"; break;
		case 7: return "Aug"; break;
		case 8: return "Sep"; break;
		case 9: return "Oct"; break;
		case 10: return "Nov"; break;
		case 11: return "Dec"; break;
		default: return "Unknown";
	}
}

function day_to_string(day) {
	switch(day) {
		case 0: return "Mon"; break;
		case 1: return "Tue"; break;
		case 2: return "Wed"; break;
		case 3: return "Thu"; break;
		case 4: return "Fri"; break;
		case 5: return "Sat"; break;
		case 6: return "Sun"; break;
		default: return "Unknown";
	}
}