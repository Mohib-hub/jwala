/** @jsx React.DOM */
var MainArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
                    <table>
                        <tr>
                            <td><Banner/><br/><br/></td>
                        </tr>
                        <tr>
                            <td>
                                <div id="loading" style={{display:"none"}}>
                                    <br/>
                                    <img src="public-resources/img/gears-2d.gif"/>
                                </div>
                                <MainTabs/>
                            </td>
                        </tr>
                    </table>
               </div>
    }
});

var Banner = React.createClass({
    render: function() {
        return <div>
                    <div className="logout">
                        <a href="#" onClick={this.handleLogoutClick}>Logout</a>
                    </div>
                    <img src="public-resources/img/toc-banner-960px.jpg"/>
               </div>
    },
    handleLogoutClick: function() {
        ServiceFactory.getUserService().logout();
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"Operations", content:<GroupOperations className="group-config"
                                           service={ServiceFactory.getGroupService()}
                                           jvmStateService={ServiceFactory.getJvmStateService()}
                                           stateService={ServiceFactory.getStateService()}
                                           jvmStateTimeout={tocVars.jvmStatePollTimeout}/>},
                 {title: "Configuration", content:<ConfigureTabs/>}];
        return null;
    },
    render: function() {
        return <Tabs theme="default" items={items} depth="0"/>
    }
});

var ConfigureTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"JVM", content:<JvmConfig className="jvm-config"
                                                  service={ServiceFactory.getJvmService()}/>},
                 {title:"Web Servers", content:<WebServerConfig className="webserver-config"
                                                                service={ServiceFactory.getWebServerService()}/>},
                 {title: "Web Apps", content:<WebAppConfig className="group-config"
                                                                service={ServiceFactory.getWebAppService()}
                                                                groupService={ServiceFactory.getGroupService()}/>},
                 {title: "Resources", content:""},
                 {title: "Group", content:<GroupConfig className="group-config"
                                                       service={ServiceFactory.getGroupService()}/>}];
        return null;
    },
    render: function() {
        return <Tabs theme="default" items={items} depth="1"/>
    }
});

$(document).ready(function(){
    React.renderComponent(<MainArea className="main-area"/>, document.body);
});