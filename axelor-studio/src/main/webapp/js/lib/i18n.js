(function() {

    "use strict";

    var bundle = (window._t || {}).bundle || {};

    if(axelor.sanitize){
        for (const [key, value] of Object.entries(bundle)) {
            bundle[key] = axelor.sanitize(value);
        }
    }

    function gettext(key) {
        var message = bundle[key] || bundle[(key||'').trim()] || key;
        if (message && arguments.length > 1) {
            for(var i = 1 ; i < arguments.length ; i++) {
                var placeholder = new RegExp('\\{' + (i-1) + '\\}', 'g');
                var value = arguments[i];
                message = message.replace(placeholder, value);
            }
        }
        return message;
    }

    this._t = gettext;

}).call(this);