FROM ubuntu:22.04

RUN apt-get update && apt-get -y upgrade
RUN apt-get install --assume-yes --no-install-recommends ca-certificates build-essential cmake git
RUN apt-get install --assume-yes --no-install-recommends libboost-regex-dev libsqlite3-dev
RUN apt-get install --assume-yes --no-install-recommends texlive-latex-extra texlive-fonts-recommended texlive-latex-recommended texlive-fonts-extra
RUN apt-get install --assume-yes --no-install-recommends libtbb-dev libxxhash-dev

# Build sqlplot-tools
RUN git clone https://github.com/bingmann/sqlplot-tools.git /opt/sqlplot-tools
RUN mkdir /opt/sqlplot-tools/build
WORKDIR /opt/sqlplot-tools/build
RUN cmake -DCMAKE_BUILD_TYPE=Release -DWITH_POSTGRESQL=OFF -DWITH_MYSQL=OFF ..
RUN cmake --build . -j 8

# Build C++ experiments
COPY ./cpp /opt/mmphf/cpp
RUN mkdir /opt/mmphf/cpp/build
WORKDIR /opt/mmphf/cpp/build
RUN cmake -DCMAKE_BUILD_TYPE=Release ..
RUN cmake --build . -j 8

# Build Java experiments
RUN apt-get install --assume-yes --no-install-recommends openjdk-17-jdk ant maven
RUN apt-get install --assume-yes --no-install-recommends ivy
RUN ln -s -T /usr/share/java/ivy.jar /usr/share/ant/lib/ivy.jar
COPY ./java /opt/mmphf/java
WORKDIR /opt/mmphf/java
RUN rm -rf /opt/mmphf/java/extlib/sux4j/jars /opt/mmphf/java/extlib/sux4j/dist /opt/mmphf/java/extlib/sux4j/build
RUN sed --in-place "s#<exclude org=\"org.apache.commons\" name=\"commons-lang3\"/>##g" /opt/mmphf/java/extlib/sux4j/ivy.xml
RUN mvn package

# Actual benchmark
CMD bash /opt/dockerVolume/normal-distribution.sh
