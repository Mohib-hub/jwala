package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class WebServerNameRule extends ValidNameRule {

    public WebServerNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_WEBSERVER_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid WebServer Name : \"" + name + "\"";
    }

    /**
     * Checks for null
     * Checks for an empty string +only spaces
     * checks for a string with last character space
     * checks for a string with first character space
     * checks that the string only contains alphanumeric+period+dash+underscore+space
     *
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return name != null && !"".equals(name.trim()) &&
                !name.matches(".*[\\s]$") && !name.matches("^[\\s].*") &&
                name.matches("[A-Za-z0-9._\\s-]+");
    }

}
