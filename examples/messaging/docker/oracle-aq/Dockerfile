#
# Copyright (c) 2020, 2024 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM container-registry.oracle.com/database/express:latest

ENV "ORACLE_PWD"="frank"
ENV "ORACLE_ALLOW_REMOTE"="true"
ENV "ORACLE_SID"="XE"
ENV "PORT"="1521"
COPY ["init.sql","/opt/oracle/scripts/startup/"]
EXPOSE 1521