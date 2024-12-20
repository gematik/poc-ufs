# Copyright (c) 2023 gematik - Gesellschaft für Telematikanwendungen der Gesundheitskarte mbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Docker Rules from https://wiki.gematik.de/display/DEV/Docker+Rules

FROM gcr.io/distroless/java21-debian12:latest

LABEL de.gematik.vendor="gematik GmbH"
LABEL maintainer="software-development@gematik.de"
LABEL de.gematik.app="UFS-Mock"
LABEL de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/Testtools/ufs-mock"

# Define the exposed port or range of ports for the service
EXPOSE 443

# Default USERID and GROUPID
ARG USERID=10000
ARG GROUPID=10000

# Run as User (not root)
USER $USERID:$GROUPID

# Copy wget from builder to enable HEALTHCHECK
COPY --from=busybox:1.36.1 /bin/wget /bin/wget

# Defining default Healthcheck e.g. when run without docker-compose or without healthcheck definition in it
HEALTHCHECK --interval=15s --timeout=10s --start-period=15s \
   CMD ["wget", "--quiet", "--tries=1", "--output-document", "-", "http://localhost:8201/health"]

# Copy the resource to the destination folder and assign permissions
COPY --chown=$USERID:$GROUPID target/*.jar /app/app.jar

# Default Working directory
WORKDIR /app

# Command to be executed.
ENTRYPOINT ["java","-jar","app.jar"]
