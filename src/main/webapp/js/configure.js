Event.observe(window, 'load', function(event) {
    Event.observe($$('form[name=config]')[0], 'submit', checkForm);
});

function checkForm(event) {
    var element = Event.element(event);
    var initialHourOfDay = $$('input[name=_.initialHourOfDay]')[0];
    var period = $$('input[name=_.period]')[0];
    var volumeSize = $$('input[name=volumeSize]')[0];

    if(!(validatePositiveNum(initialHourOfDay.value)) || initialHourOfDay.value > 24) {
        Event.stop(event);
        setDivOpacity('msg1',0);
        $('msg1').style.display="block";
        $('msg1').style.color="red";
        $('msg1').innerHTML = "<strong>Error! Initial backup hour of the day is not correct, please correct.</strong>";
        appear('msg1');
    }
    else {
        setDivOpacity('msg1',0);
    }
    if(!(validatePositiveNum(period.value))) {
        Event.stop(event);
        setDivOpacity('msg2',0);
        $('msg2').style.display="block";
        $('msg2').style.color="red";
        $('msg2').innerHTML = "<strong>Error! Period value is not correct, please correct.</strong>";
        appear('msg2');
    }
    else {
        setDivOpacity('msg2',0);
    }
    if(volumeSize != null) {
        if(!validatePositiveNum(volumeSize.value)) {
            Event.stop(event);
            setDivOpacity('msg3',0);
            $('msg3').style.display="block";
            $('msg3').style.color="red";
            $('msg3').innerHTML = "<strong>Error! Entered split volume threshold is not correct, please correct.</strong>";
            appear('msg3');
        }
        else {
            setDivOpacity('msg3',0);
        }
    }


}

function setDivOpacity(divId, level) {
    $(divId).style.opacity = level;
    $(divId).style.MozOpacity = level;
    $(divId).style.KhtmlOpacity = level;
    $(divId).style.filter = "alpha(opacity=" + (level * 100) + ");";
}

function appear(divId) {
    for (i = 0; i <= 1; i += (1 / 20)) {
        setTimeout("setDivOpacity(" + divId + "," + (i) + ")", i * 1000);
    }
}

//function will return false if value is not numerical or not positive
function validatePositiveNum(value) {
    var number = parseInt(value);
    if(isNaN(number)) {
        return false;
    }
    else {
        if(number > 0) {
            //if parseInt will return number but input was not a number, for example "3fg" -> validation will fail
            if(value.toString() != number.toString()) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }
}




