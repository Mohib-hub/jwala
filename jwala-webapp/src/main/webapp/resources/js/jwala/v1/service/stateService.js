var stateService = function() {

    var clientId = Date.now();

    var constructIdParameters = function(type, ids) {
        if (ids === undefined) {
            return "";
        } else {
            var paramIds = {};
            paramIds[type] = ids;
            return "?" + $.param(paramIds);
        }
    };

    var addClientIdParameter = function(clientId, params) {
        params.clientId = clientId;
    };

    var createPollingParameters = function(clientId) {
        var params = {};
        addClientIdParameter(clientId, params);
        return "?" + $.param(params);
    };

    return {
        getCurrentJvmStates : function(ids) {
            return serviceFoundation.promisedGet("v1.0/jvms/states/current" + constructIdParameters("jvmId", ids),
                                                 "json", true);
        },
        getNextStates: function() {
            return serviceFoundation.promisedGet("v1.0/states/next" + createPollingParameters(clientId),"json");
        },
        requestForJvmStates: function(groupName) {
            return serviceFoundation.promisedGet("v1.0/states/" + groupName + "/jvm","json");
        }
    };
}();