Feature: Add, Edit and Delete a Web Server

Scenario: Add Web Server

    Given I logged in
    And I am in the Configuration tab
    And I created a media with the following parameters:
            |mediaName      |apache-httpd-2.4.20    |
            |mediaType      |Apache HTTPD           |
            |archiveFilename|apache-httpd-2.4.20.zip|
            |remoteDir      |media.remote.dir       |
    And I created a group with the name "GROUP_FOR_ADD_WEBSERVER_TEST"
    And I am in the web server tab
    When I click the add web server button
    And I see the web server add dialog
    And I fill in the "Web Server Name" field with "WEBSERVER_X"
    And I fill in the "Host Name" field with "host1"
    And I fill in the "HTTP Port" field with "80"
    And I fill in the "HTTPS Port" field with "443"
    And I select the "Status Path" field
    And I select the "Apache HTTPD" field "apache-httpd-2.4.20"
    And I select the group "GROUP_FOR_ADD_WEBSERVER_TEST"
    And I click the add web server dialog ok button
    Then I see "WEBSERVER_X" in the webserver table