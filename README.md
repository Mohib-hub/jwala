# _About_

Jwala is a web application that provides management for a group of Tomcat servers. Jwala is capable of creating and persisting definitions of Group instances, and exposes a RESTful interface to do so. The definition of a Group includes Web Apps, JVMs, Web Servers, and Resources.

Once defined, a Group may also be managed through Jwala’s REST API, to perform management operations on the group. The management operations are listed under Component Responsibilities below. The primary user of Jwala will be the enterprise package manager application, which will interact with Jwala using the REST API.

Jwala utilizes the defined file system structure and SSH agents on each machine to manage running Tomcat instances on remote Windows Servers. Jwala utilizes the STP application deployment model to know how to request current status from each Tomcat instance and HTTPD instance. Jwala is also able to update each instance as changes are made to the configuration, and allows maintenance operations to be executed from a central console.


Sections in this readme that are marked with _italics_ require review and action when utilizing the template. Additional sections
may be added.

Generally speaking, the LICENSE.txt file, Contributing, and License sections should be left alone. The
CONTRIBUTORS.md, CONTRIBUTING.md, and NOTICE.txt should be left in place but may need to be enhanced
with additional details, and signed with initial contributor names.

This usage section should be removed or replaced with details around the usage of your project. As a library and/or an
independent functional piece of software.

# _Building_

Jwala can be build by using gradle. to build the project run the gradle build command. This runs the build and test tasks. 

# _Availability_

Artifacts or running software associated with this project and where to access them is generally added here.

In the open source community provided pre-built artifacts for your project can greatly assist in adoption and building
good will.

# _Conventions_

Define testing, code conventions, and code formatting standards for the project. If no such standards are desired, please remove
this section.

# _Communication_

Define how consumers and contributors should communicate with others on the project. This may include information from mailing
lists to where bugs are logged. Github issues are a great way to track bugs and enhancements with the project. You may think
about conventions that you'd like issues that are logged to follow. For example, noting an enhancement vs a bug.

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

# LICENSE

Copyright <%= currentYear %> Cerner Innovation, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

&nbsp;&nbsp;&nbsp;&nbsp;http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.