FROM openjdk:11-jre-slim

ADD *.tar /
RUN ln -s /updatarium-cli*/ /updatarium-cli


WORKDIR /updatarium-cli
USER nobody

ENTRYPOINT ["/updatarium-cli/bin/updatarium-cli"]
CMD ["--help"]